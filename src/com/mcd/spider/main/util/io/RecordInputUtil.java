package com.mcd.spider.main.util.io;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author u569220
 *
 */
public class RecordInputUtil {

	public static final Logger logger = Logger.getLogger(RecordInputUtil.class);

	private String docName;
	private boolean offline;
	private File idFile;
	private Record record;
	private RecordIOUtil ioUtil;

	public RecordInputUtil(RecordIOUtil ioUtil) {
		this.docName = ioUtil.getDocName();
		this.record = ioUtil.getRecord();
        this.offline = System.getProperty("offline").equals("true");
        this.idFile = ioUtil.getIdFile();
        this.ioUtil = ioUtil;
	}

	public String getDocName() {
		return docName;
	}

    public Set<String> getPreviousIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        BufferedReader br = null;
        if (!offline) {
            try {
                if (!idFile.exists()) {
                    idFile.createNewFile();
                }
                br = new BufferedReader(new FileReader(idFile));
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    ids.add(sCurrentLine);
                }
            } catch (IOException e) {
                throw new IDCheckException();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ioe) {
                    logger.error("Error reading previous IDs file", ioe);
                }
            }
        }
        return ids;
    }

    public List<Set<Record>> readRecordsFromDefaultWorkbook() {
		return readRecordsFromWorkbook(new File(docName));
	}

    public List<Set<Record>> readRecordsFromWorkbook(File fileToRead) {
        List<Set<Record>> listOfRecordSets = new ArrayList<>();
    	try {
    		logger.debug("Attempting to read previous records from " + fileToRead.getName() + " into memory");
    		if (fileToRead.exists()) {
                Workbook workbook = Workbook.getWorkbook(fileToRead);
                if (workbook!=null) {
                    for (int s=0;s<workbook.getNumberOfSheets();s++) {
                        //when columns are deleted, "extra rows" are added to sheet
                        listOfRecordSets.add(readRecordsFromSheet(fileToRead, s));
                    }
                }
            }
    	} catch (FileNotFoundException e) {
    		logger.error("No record file found", e);
    	} catch (IOException | BiffException e) {
    		logger.error("Exception caught while trying to read in previous records", e);
    	} catch (Exception e) {
    		logger.error("Unexpected exception while trying to read in records, refer to backup file created", e);
    	}
    	return listOfRecordSets;
    }

    public Set<Record> readRecordsFromSheet(File fileToRead, int sheetNumber) {
        Set<Record> storedRecords = new HashSet<>();
        Class<?> clazz = Record.getRecordClass(record);
        Constructor<?> constructor = Record.getConstructorForRecord(clazz, record);
        int foundRecordsCount = 0;
        int retrievedRecordsCount = 0;
        try {
            Workbook workbook = Workbook.getWorkbook(fileToRead);
            if (workbook!=null) {
                Sheet sheetToRead = workbook.getSheet(sheetNumber);
                logger.debug("Attempting to read previous records from " + fileToRead.getName() + " into memory");
                //starting with the first data row, read records into set
                foundRecordsCount+=getNonEmptyRowCount(sheetToRead);
                for (int r = 1; r<sheetToRead.getRows(); r++) {
                    //loop over columnEnums for each row
                    if (rowIsNotEmpty(sheetToRead.getRow(r))) {
                        try {
                            Object rowRecord = constructor.newInstance();
                            storedRecords.add(Record.readRowIntoRecord(clazz, sheetToRead, rowRecord, r));
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                            logger.error("Error trying to read row into record object, row "+r, e);
                        }
                    }
                }
                retrievedRecordsCount+=storedRecords.size();
            }

        } catch (BiffException | IOException e) {
            logger.error("Exception caught reading sheet into records - " + fileToRead.getName() + " sheet " + sheetNumber);
        }
        logger.debug("Found " +  (foundRecordsCount-1) + " and retrieved " + retrievedRecordsCount);
        return storedRecords;
    }

    public int getNonEmptyRowCount(Sheet sheet) {
	    int foundRecordCount = 0;
	    for (int r=0; r<sheet.getRows(); r++) {
	        if (rowIsNotEmpty(sheet.getRow(r))) {
                foundRecordCount++;
            }
        }
	    return foundRecordCount;
    }

    private boolean rowIsNotEmpty(Cell[] row) {
    	//if both of the first two columns are empty, consider row empty
    	return row.length>0 && (row[0].getContents().length()>0 && row[1].getContents().length()>0);
    }
}
