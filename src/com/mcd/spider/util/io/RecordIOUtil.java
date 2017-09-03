package com.mcd.spider.util.io;

import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.Site;
import jxl.Sheet;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author u569220
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
	
	public RecordIOUtil(State state, Record record, Site site) {
        this(state, record, site, false);
	}

    public RecordIOUtil(State state, Record record, Site site, boolean testing) {
//	    if (!new File(OUTPUT_DIR).exists()) {
//            new File(OUTPUT_DIR).mkdirs();
//        }
        if (!new File(TRACKING_DIR).exists()) {
            new File(TRACKING_DIR).mkdirs();
        }
	    if (testing) {
//            if (!new File("output\\testing\\").exists()) {
//                new File("output\\testing\\").mkdirs();
//            }
            if (!new File("output\\testing\\tracking\\").exists()) {
                new File("output\\testing\\tracking\\").mkdirs();
            }
	        this.crawledIdFile = new File("output\\testing\\tracking\\" + site.getName() + "_Archive.txt");
	        this.uncrawledIdFile = new File("output\\testing\\tracking\\" + site.getName() + "_Uncrawled.txt");
	        this.mainDocPath = "output\\testing\\" + state.getName() + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
        } else {
	        this.crawledIdFile = new File(TRACKING_DIR + site.getName() + "_Archive.txt");
	        this.uncrawledIdFile = new File(TRACKING_DIR + site.getName() + "_Uncrawled.txt");
	        this.mainDocPath = OUTPUT_DIR + state.getName() + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
        }
        this.record = record;
        this.outputter = new RecordOutputUtil(this, state);
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

    public List<Set<Record>> mergeRecordsFromWorkbooks(File fileOne, File fileTwo) {
    	List<Set<Record>> mergedResults = new ArrayList<>();
    	Sheet[] sheetsFromFileOne = inputter.getSheets(fileOne);
    	Sheet[] sheetsFromFileTwo = inputter.getSheets(fileTwo);
    	
    	for (Sheet sheetFromOne : sheetsFromFileOne) {
    		boolean sheetNotAdded = true;
    		for (Sheet sheetFromTwo : sheetsFromFileTwo) {
    			if (sheetNotAdded && sheetFromOne.getName().equalsIgnoreCase(sheetFromTwo.getName())) {
    				logger.info("Attempting to merge sheets " + sheetFromOne.getName() + " from " + fileOne.getName() + " and " + fileTwo.getName());
    				mergedResults.add(mergeRecordsFromSheets(fileOne, fileTwo, inputter.getSheetIndex(fileOne, sheetFromOne.getName()), inputter.getSheetIndex(fileTwo, sheetFromTwo.getName())));
    				sheetNotAdded = false;
    			}
    		}
    		if (sheetNotAdded) {
				//send sheet one without sheet two
				logger.info("No matching sheet was found for " + sheetFromOne.getName() + ". Only outputting files from " + fileOne.getName());
				mergedResults.add(mergeRecordsFromSheets(fileOne, null, inputter.getSheetIndex(fileOne, sheetFromOne.getName()), -1));
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
				mergedResults.add(mergeRecordsFromSheets(fileTwo, null, inputter.getSheetIndex(fileTwo, sheetFromTwo.getName()), -1));
        	}
    	}
    	
    	return mergedResults;
    }
    
    public Set<Record> mergeRecordsFromSheets(File fileOne, File fileTwo, int sheetNumberOne, int sheetNumberTwo) {
		Set<Record> storedRecordsOne = inputter.readRecordsFromSheet(fileOne, sheetNumberOne);
		Set<Record> storedRecordsTwo = inputter.readRecordsFromSheet(fileTwo, sheetNumberTwo);
		Set<Record> compiledRecords = new HashSet<>();
		
		Set<Record> outerSet = storedRecordsOne==null?new HashSet<>():new HashSet<>(storedRecordsOne);
		Set<Record> innerSet = storedRecordsTwo==null?new HashSet<>():new HashSet<>(storedRecordsTwo);

		int mergedCount = 0;
		for (Record recordOne : outerSet) {
			for (Record recordTwo : innerSet) {
				if (recordOne.matches(recordTwo)) {
					recordOne.merge(recordTwo);
					compiledRecords.add(recordOne);
					storedRecordsTwo.remove(recordTwo);
					mergedCount++;
				} else {
					compiledRecords.add(recordOne);
					storedRecordsOne.remove(recordOne);
				}
			}
		}
		compiledRecords.addAll(storedRecordsOne);
		compiledRecords.addAll(storedRecordsTwo);
		logger.info(mergedCount + " records were merged");
		logger.info(compiledRecords.size() + " total records as a result of the merge");
		return compiledRecords;
	}
}
