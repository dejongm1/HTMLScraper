package com.mcd.spider.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.site.Site;

import jxl.Sheet;

/**
 * 
 * @author Michael De Jong
 *
 */

public class RecordIOUtil {
	
	public static final Logger logger = Logger.getLogger(RecordIOUtil.class);
	private static final String EXT = ".xls";
    private static final String OUTPUT_DIR = "output\\";
    private static final String TRACKING_DIR = OUTPUT_DIR + "tracking\\";
	
	private String mainDocPath;
	private RecordInputUtil inputter;
	private RecordOutputUtil outputter;
	private File crawledIdFile;
    private File uncrawledIdFile;
	private Record record;
	
	public RecordIOUtil(String stateName, Record record, Site site) {
		this(stateName, record, site, Boolean.parseBoolean(System.getProperty("TestingSpider")));
	}

    public RecordIOUtil(String stateName, Record record, Site site, boolean testing) {
        if (!new File(TRACKING_DIR).exists()) {
            new File(TRACKING_DIR).mkdirs();
        }
	    if (testing) {
            if (!new File("output\\testing\\tracking\\").exists()) {
                new File("output\\testing\\tracking\\").mkdirs();
            }
	        this.crawledIdFile = new File("output\\testing\\tracking\\" + stateName + "_" + site.getName() + "_Archive.txt");
	        this.uncrawledIdFile = new File("output\\testing\\tracking\\" + stateName + "_" + site.getName() + "_Uncrawled.txt");
	        this.mainDocPath = "output\\testing\\" + stateName + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
        } else {
	        this.crawledIdFile = new File(TRACKING_DIR + stateName + "_" + site.getName() + "_Archive.txt");
	        this.uncrawledIdFile = new File(TRACKING_DIR + stateName + "_" + site.getName() + "_Uncrawled.txt");
	        this.mainDocPath = OUTPUT_DIR + stateName + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
        }
        this.record = record;
        this.outputter = new RecordOutputUtil(this, site);
        this.inputter = new RecordInputUtil(this);
    }

	public String getMainDocPath() {
		return mainDocPath;
	}

    public void setMainDocPath(String mainDocName) {
        this.mainDocPath = mainDocName;
    }

    public File getCrawledIdFile() {
        return crawledIdFile;
    }

    public File getUncrawledIdFile() {
        return uncrawledIdFile;
    }

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public RecordInputUtil getInputter() {
		return inputter;
	}

	public RecordOutputUtil getOutputter() {
		return outputter;
	}

    public static String getEXT() {
        return EXT;
    }

    public static String getOUTPUT_DIR() {
        return OUTPUT_DIR;
    }

    public RecordWorkbook mergeRecordsFromWorkbooks(File fileOne, File fileTwo) {
    	RecordWorkbook mergedResults = new RecordWorkbook();
    	Sheet[] sheetsFromFileOne = inputter.getSheets(fileOne);
    	Sheet[] sheetsFromFileTwo = inputter.getSheets(fileTwo);
    	
    	for (Sheet sheetFromOne : sheetsFromFileOne) {
    		boolean sheetNotAdded = true;
    		for (Sheet sheetFromTwo : sheetsFromFileTwo) {
    			if (sheetNotAdded && sheetFromOne.getName().equalsIgnoreCase(sheetFromTwo.getName())) {
    				logger.info("Attempting to merge sheets " + sheetFromOne.getName() + " from " + fileOne.getName() + " and " + fileTwo.getName());
    				mergedResults.addSheet(mergeRecordsFromSheets(fileOne, fileTwo, inputter.getSheetIndex(fileOne, sheetFromOne.getName()), inputter.getSheetIndex(fileTwo, sheetFromTwo.getName())));
    				sheetNotAdded = false;
    			}
    		}
    		if (sheetNotAdded) {
				//send sheet one without sheet two
				logger.info("No matching sheet was found for " + sheetFromOne.getName() + ". Only outputting files from " + fileOne.getName());
				mergedResults.addSheet(mergeRecordsFromSheets(fileOne, null, inputter.getSheetIndex(fileOne, sheetFromOne.getName()), -1));
    		}
    	}
    	
    	//add any sheets from two that were missed
    	for (Sheet sheetFromTwo : sheetsFromFileTwo) {
    		boolean matchFound = false;
        	for (Sheet sheetFromOne : sheetsFromFileOne) {
				if (sheetFromOne.getName().equalsIgnoreCase(sheetFromTwo.getName())) {
					matchFound = true;
				}
			}
        	if (!matchFound) {
        		//send sheet two without sheet one
				logger.info("No matching sheet was found for " + sheetFromTwo.getName() + ". Only outputting files from " + fileTwo.getName());
				mergedResults.addSheet(mergeRecordsFromSheets(fileTwo, null, inputter.getSheetIndex(fileTwo, sheetFromTwo.getName()), -1));
        	}
    	}
    	
    	return mergedResults;
    }

    public RecordSheet mergeRecordsFromSheets(File fileOne, File fileTwo, int sheetNumberOne, int sheetNumberTwo) {
    	RecordSheet storedRecordsOne = inputter.readRecordsFromSheet(fileOne, sheetNumberOne);
    	RecordSheet storedRecordsTwo = inputter.readRecordsFromSheet(fileTwo, sheetNumberTwo);
		
		return mergeRecordsFromSets(storedRecordsOne, storedRecordsTwo);
	}
    
    public RecordSheet mergeRecordsFromSets(RecordSheet recordSheetOne, RecordSheet recordSheetTwo) {
    	RecordSheet compiledRecordSheet = new RecordSheet();
    	RecordSheet outerSheet = recordSheetOne.isEmpty()?new RecordSheet():RecordSheet.copy(recordSheetOne);
    	RecordSheet innerSheet = recordSheetTwo.isEmpty()?new RecordSheet():RecordSheet.copy(recordSheetTwo);

		int mergedCount = 0;
		for (Record recordOne : outerSheet.getRecords()) {
			for (Record recordTwo : innerSheet.getRecords()) {
				if (recordOne.matches(recordTwo)) {
					recordOne.merge(recordTwo);
					compiledRecordSheet.addRecord(recordOne);
					recordSheetTwo.removeRecord(recordTwo);
					mergedCount++;
				} else {
					compiledRecordSheet.addRecord(recordOne);
					recordSheetOne.removeRecord(recordOne);
				}
			}
		}
		compiledRecordSheet.addAllRecords(recordSheetOne);
		compiledRecordSheet.addAllRecords(recordSheetTwo);
		logger.info(mergedCount + " records were merged into sheet " + compiledRecordSheet.getSheetName());
		logger.info(compiledRecordSheet.recordCount() + " total records as a result of the merge");
		return compiledRecordSheet;
    }
}
