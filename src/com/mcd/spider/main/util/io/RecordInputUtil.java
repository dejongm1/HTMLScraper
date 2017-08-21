package com.mcd.spider.main.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

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

    public Set<Record> readDefaultSpreadsheet() {
		return readSpreadsheet(new File(docName));
	}

    public Set<Record> readSpreadsheet(File fileToRead) {
    	Set<Record> storedRecords = new HashSet<>();
    	int foundRecordsCount = 0;
    	try {
    		logger.debug("Attempting to read previous records from " + fileToRead.getName() + " into memory");
    		if (fileToRead.exists()) {
                Workbook workbook = Workbook.getWorkbook(fileToRead);
                if (workbook!=null) {
                    Sheet mainSheet = workbook.getSheet(0);
                    //when columns are deleted, "extra rows" are added to sheet
                    foundRecordsCount = getNonEmptyRowCount(mainSheet);
                    Class<?> clazz = Record.getRecordClass(record);
                    Constructor<?> constructor = Record.getConstructorForRecord(clazz, record);
                    //starting with the first data row, read records into set
                    for (int r = 1; r<mainSheet.getRows(); r++) {
                        //loop over columnEnums for each row
                    	if (rowIsNotEmpty(mainSheet.getRow(r))) {
	                        try {
	                            Object rowRecord = constructor.newInstance();
	                            storedRecords.add(Record.readRowIntoRecord(clazz, mainSheet, rowRecord, r));
	                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
	                            logger.error("Error trying to read row into record object, row "+r, e);
	                        }
                    	}
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

    	logger.debug("Found " +  (foundRecordsCount-1) + " and retrieved " + storedRecords.size());
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
