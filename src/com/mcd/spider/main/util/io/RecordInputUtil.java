package com.mcd.spider.main.util.io;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
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

	public RecordInputUtil(RecordIOUtil ioUtil) {
		this.docName = ioUtil.getDocName();
		this.record = ioUtil.getRecord();
        this.offline = System.getProperty("offline").equals("true");
        this.idFile = ioUtil.getIdFile();
	}

	public String getDocName() {
		return docName;
	}

    public Set<String> getPreviousIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        //TODO check name as well to make sure it's the right state/site
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
                    //TODO this is getting unpopulated rows
                    foundRecordsCount = mainSheet.getRows();
                    Class<?> clazz = Record.getRecordClass(record);
                    Constructor<?> constructor = Record.getConstructorForRecord(clazz, record);
                    //starting with the first data row, read records into set
                    for (int r = 1; r<foundRecordsCount; r++) {
                        //loop over columnEnums for each row
                        try {
                            Object rowRecord = constructor.newInstance();
                            storedRecords.add(Record.readRowIntoRecord(clazz, mainSheet, rowRecord, r, record.getFieldsToOutput().size()));
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                            logger.error("Error trying to read row into record object, row "+r, e);
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

	public Set<Record> mergeRecordsFromSpreadsheets(File fileOne, File fileTwo) {
		Set<Record> storedRecordsOne = readSpreadsheet(fileOne);
		Set<Record> storedRecordsTwo = readSpreadsheet(fileTwo);
		Set<Record> compiledRecords = new HashSet<>();
		Set<Record> outerSet = storedRecordsOne;
		Set<Record> innerSet = storedRecordsTwo;
//		
		for (Record recordOne : outerSet) {
			for (Record recordTwo : innerSet) {
				if (recordOne.matches(recordTwo)) {
					recordOne.merge(recordTwo);
					compiledRecords.add(recordOne);
					storedRecordsTwo.remove(recordTwo);
				} else {
					compiledRecords.add(recordOne);
				}
			}
		}
		compiledRecords.addAll(storedRecordsTwo);		
		
		return compiledRecords;
	}
}
