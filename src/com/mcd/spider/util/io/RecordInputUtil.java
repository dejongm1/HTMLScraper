package com.mcd.spider.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.exception.IDCheckException;
import com.mcd.spider.exception.SpiderException;

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
	private File crawledIdFile;
	private File uncrawledIdFile;
	private Record record;
//	private RecordIOUtil ioUtil;

	public RecordInputUtil(RecordIOUtil ioUtil) {
		this.docName = ioUtil.getMainDocPath();
		this.record = ioUtil.getRecord();
        this.offline = System.getProperty("offline").equals("true");
        this.crawledIdFile = ioUtil.getCrawledIdFile();
        this.uncrawledIdFile = ioUtil.getUncrawledIdFile();
//        this.ioUtil = ioUtil;
	}

    public Set<String> getCrawledIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        BufferedReader br = null;
        if (!offline) {
            try {
                if (!crawledIdFile.exists()) {
                    crawledIdFile.createNewFile();
                }
                br = new BufferedReader(new FileReader(crawledIdFile));
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
                    logger.error("Error closing previous IDs file", ioe);
                }
            }
        }
        return ids;
    }

    public Set<String> getUncrawledIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        BufferedReader br = null;
        if (!offline) {
            try {
                if (uncrawledIdFile.exists()) {
                    br = new BufferedReader(new FileReader(uncrawledIdFile));
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine())!=null) {
                        ids.add(sCurrentLine);
                    }
                }
            } catch (IOException e) {
                throw new IDCheckException();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ioe) {
                    logger.error("Error closing uncrawled IDs file", ioe);
                }
            }
        }
        return ids;
    }

    public RecordWorkbook readRecordsFromDefaultWorkbook() {
		return readRecordsFromWorkbook(new File(docName));
	}

    public RecordWorkbook readRecordsFromWorkbook(File fileToRead) {
    	RecordWorkbook recordSheets = new RecordWorkbook();
    	try {
    		logger.debug("Attempting to read previous records from workbook " + fileToRead.getName() + " into memory");
    		if (fileToRead.exists()) {
                Workbook workbook = Workbook.getWorkbook(fileToRead);
                if (workbook!=null) {
                    for (int s=0;s<workbook.getNumberOfSheets();s++) {
                        //when columns are deleted, "extra rows" are sometimes added to sheet
                        recordSheets.add(readRecordsFromSheet(fileToRead, s));
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
    	return recordSheets;
    }

    public Sheet[] getSheets(File fileToRead) {
        Workbook workbook;
        Sheet[] sheets = new Sheet[0];
		try {
			workbook = Workbook.getWorkbook(fileToRead);
			sheets = workbook.getSheets();            
			
		} catch (BiffException | IOException e) {
			logger.error("Couldn't gather sheets from " + fileToRead.getName(), e);
		}
    	return sheets;
    }
    
    public int getSheetIndex(File fileToRead, String sheetName) {
		Integer sheetNumber = null;
		final Sheet[] sheets = getSheets(fileToRead);  
		if (sheets!=null) {
			for(int i=0;i<sheets.length && sheetNumber == null; i++) {
				if(sheets[i].getName().equals(sheetName)) {
					sheetNumber = i;                  
				}
			}
	    }
    	return sheetNumber;
    }
    
    public RecordSheet readRecordsFromSheet(File fileToRead, String sheetName) {
    	int sheetNumber = getSheetIndex(fileToRead, sheetName);
    	if (sheetNumber==-1) {
    		return new RecordSheet();
    	} else {
    		return readRecordsFromSheet(fileToRead, sheetNumber);
    	}
    }
    
    public RecordSheet readRecordsFromSheet(File fileToRead, int sheetNumber) {
        RecordSheet storedRecordSheet = new RecordSheet();
        Class<?> clazz = Record.getRecordClass(record);
        Constructor<?> constructor = Record.getConstructorForRecord(clazz, record);
        int foundRecordsCount = 0;
        int retrievedRecordsCount = 0;
        //don't try to readRecords if -1 is passed
        if (sheetNumber!=-1) {
	        try {
                Workbook workbook = null;
                try {
                    workbook = Workbook.getWorkbook(fileToRead);
                } catch (FileNotFoundException e) {
                    logger.error(fileToRead.getName() + " does not exist so there are no records to read in");
                }
	            if (workbook!=null) {
	                Sheet sheetToRead = workbook.getSheet(sheetNumber);
	                logger.debug("Attempting to read previous records from sheet " + sheetToRead.getName() + " into memory");
	                //starting with the first data row, read records into set
	                foundRecordsCount+=getNonEmptyRowCount(sheetToRead);
	                Object rowRecord = constructor.newInstance();
	                List<Object> columnOrder = Record.getColumnOrder(clazz, sheetToRead, rowRecord);
	                for (int r = 1; r<sheetToRead.getRows(); r++) {
	                    if (rowIsNotEmpty(sheetToRead.getRow(r))) {
	                        try {
	                            //pass in new instance of rowRecord constructor for each row to read in
	                            storedRecordSheet.add(Record.readRowIntoRecord(clazz, sheetToRead, constructor.newInstance(), r, columnOrder));
	                        } catch (IllegalArgumentException e) {
	                            logger.error("Error trying to read row into record object, row "+r, e);
	                        }
	                    }
	                }
	                retrievedRecordsCount+=storedRecordSheet.recordCount();
	            }
                logger.debug("Found " +  (foundRecordsCount-1) + " and retrieved " + retrievedRecordsCount);
	        } catch (BiffException | IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	            logger.error("Exception caught reading sheet into records - " + fileToRead.getName() + " sheet " + sheetNumber, e);
	        }
        }
        return storedRecordSheet;
    }
    
    public int getNonEmptyRowCount(Sheet sheet) {
    	//includes header row
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
