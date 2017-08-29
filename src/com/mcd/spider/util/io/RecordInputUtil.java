package com.mcd.spider.util.io;

import com.mcd.spider.entities.record.Record;
import com.mcd.spider.exception.IDCheckException;
import com.mcd.spider.exception.SpiderException;
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
	private File crawledIdFile;
	private File uncrawledIdFile;
	private Record record;
//	private RecordIOUtil ioUtil;

	public RecordInputUtil(RecordIOUtil ioUtil) {
		this.docName = ioUtil.getMainDocName();
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

    public List<Set<Record>> readRecordsFromDefaultWorkbook() {
		return readRecordsFromWorkbook(new File(docName));
	}

    public List<Set<Record>> readRecordsFromWorkbook(File fileToRead) {
        List<Set<Record>> listOfRecordSets = new ArrayList<>();
    	try {
    		logger.debug("Attempting to read previous records from workbook " + fileToRead.getName() + " into memory");
    		if (fileToRead.exists()) {
                Workbook workbook = Workbook.getWorkbook(fileToRead);
                if (workbook!=null) {
                    for (int s=0;s<workbook.getNumberOfSheets();s++) {
                        //when columns are deleted, "extra rows" are sometimes added to sheet
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

    public int getSheetIndex(File fileToRead, String sheetName) {
        Workbook workbook;
		Integer sheetNumber = null;
		try {
			workbook = Workbook.getWorkbook(fileToRead);
			final Sheet[] sheets = workbook.getSheets();            
			for(int i=0;i<sheets.length && sheetNumber == null; i++) {
				if(sheets[i].getName().equals(sheetName)) {
					sheetNumber = i;                  
				}
			}    
		} catch (BiffException | IOException e) {
			logger.error("Couldn't find a sheet named " + sheetName, e);
		}
    	return sheetNumber;
    }
    
    public Set<Record> readRecordsFromSheet(File fileToRead, String sheetName) {
    	int sheetNumber = getSheetIndex(fileToRead, sheetName);
    	if (sheetNumber==-1) {
    		return new HashSet<Record>();
    	} else {
    		return readRecordsFromSheet(fileToRead, sheetNumber);
    	}
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
                logger.debug("Attempting to read previous records from sheet " + sheetToRead.getName() + " into memory");
                //starting with the first data row, read records into set
                foundRecordsCount+=getNonEmptyRowCount(sheetToRead);
                Object rowRecord = constructor.newInstance();
                List<Object> columnOrder = Record.getColumnOrder(clazz, sheetToRead, rowRecord);
                for (int r = 1; r<sheetToRead.getRows(); r++) {
                    if (rowIsNotEmpty(sheetToRead.getRow(r))) {
                        try {
                            //pass in new instance of rowRecord contructor for each row to read in
                            storedRecords.add(Record.readUnorderedRowIntoRecord(clazz, sheetToRead, constructor.newInstance(), r, columnOrder));
                        } catch (IllegalArgumentException e) {
                            logger.error("Error trying to read row into record object, row "+r, e);
                        }
                    }
                }
                retrievedRecordsCount+=storedRecords.size();
            }

        } catch (BiffException | IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Exception caught reading sheet into records - " + fileToRead.getName() + " sheet " + sheetNumber, e);
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
