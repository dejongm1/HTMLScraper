package com.mcd.spider.engine;

import static com.mcd.spider.entities.record.ArrestRecord.ArrestDateComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.engine.audit.AuditEngine;
import com.mcd.spider.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.engine.router.StateRouter;
import com.mcd.spider.entities.audit.AuditParameters;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.ArrestRecord.RecordColumnEnum;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.exception.StateNotReadyException;
import com.mcd.spider.util.io.RecordIOUtil;

/**
 * 
 * @author U569220
 *
 */

public class SpiderEngine {

	public static final Logger logger = Logger.getLogger(SpiderEngine.class);

	//redirect to Appropriate engine from here
	public void getArrestRecordsByState(List<State> states, long maxNumberOfResults, RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException {
		for (State state : states) {
			if (!state.getEngines().isEmpty()) {
				StateRouter router = new StateRouter(state);
				router.collectRecordsUsingThreading(maxNumberOfResults, filter, retrieveMissedRecords);
		        RecordIOUtil mainIOutil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(0).getSite());
				customizeArrestOutputs(mainIOutil, state, filter);
			} else {
				throw new StateNotReadyException(state);
			}
		}
		logger.info("Spider has finished crawling. Shutting down.");
	}
	
	public void getArrestRecordsThroughTheBackDoor(List<State> states, long maxNumberOfResults, RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException {
		for (State state : states) {
			state.getEngines().clear();
//            state.addEngine(new ArrestsDotOrgEngine());
			state.addEngine(new DesMoinesRegisterComEngine());
//			state.addEngine(new MugShotsDotComEngine());
			StateRouter router = new StateRouter(state);
			router.collectRecords(maxNumberOfResults, filter, retrieveMissedRecords);
		}
		logger.info("Spider has finished crawling. Shutting down.");
	}
	
	public void performSEOAudit(AuditParameters auditParams) {
		AuditEngine engine = new AuditEngine();
		engine.performSEOAudit(auditParams);
	}
	
	public void search(String url, String words, int levelOfGenerosity) {
		AuditEngine engine = new AuditEngine();
		engine.search(url, words, levelOfGenerosity);
	}
	
	public void getPopularWords(String url, int numberOfWords) {
		AuditEngine engine = new AuditEngine();
		engine.getPopularWords(url, numberOfWords);
	}
	
	public void getTextBySelector(String url, String selector) {
		AuditEngine engine = new AuditEngine();
		engine.getTextBySelector(url, selector);
	}

	protected void customizeArrestOutputs(RecordIOUtil mainIOutil, State state, RecordFilterEnum filter) {
		//TODO move this whole thing to RecordOutputUtil?
        //start with the second engine and iterate over the rest
        List<Set<Record>> allMergedRecords = new ArrayList<>();
        for (int e=1;e<state.getEngines().size();e++) {
            //TODO this will overwrite _MERGED if more than 2 engines per site
            //try simply gathering merged records from all engines, then outputting everything at the end
        	//can wait until a third site is added to any state. Implement RecordWorkbook in conjunction with this
            logger.info("Attempting to merge record output from " + state);
            RecordIOUtil comparingIOUtil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(e).getSite());
            List<Set<Record>> mergedRecords = mainIOutil.mergeRecordsFromWorkbooks(new File(mainIOutil.getMainDocPath()), new File(comparingIOUtil.getMainDocPath()));
        
            String[] sheetNames = new String[mergedRecords.size()];
            sheetNames[0] = state.getName();
            for (int mrs=1;mrs<sheetNames.length;mrs++) {
                sheetNames[mrs] = ((ArrestRecord)mergedRecords.get(mrs).toArray()[0]).getCounty();
            }
            if (!mergedRecords.isEmpty()) {
                mainIOutil.getOutputter().createWorkbook(mainIOutil.getOutputter().getMergedDocPath(null), mergedRecords, false, sheetNames, ArrestDateComparator);
                logger.info("Merge of all records complete.");
            } else {
                logger.info("Nothing found to merge");
            }

            if (!filter.filterName().equals(RecordFilterEnum.NONE.filterName())) {
                logger.info("Attempting to merge filtered record output from "+state);
                List<Set<Record>> mergedFilteredRecords = mainIOutil.mergeRecordsFromWorkbooks(new File(mainIOutil.getOutputter().getFilteredDocPath(filter)), new File(comparingIOUtil.getOutputter().getFilteredDocPath(filter)));
                String[] filteredSheetNames = new String[mergedFilteredRecords.size()];
                filteredSheetNames[0] = state.getName();
                for (int mrs = 1; mrs<filteredSheetNames.length; mrs++) {
                    filteredSheetNames[mrs] = ((ArrestRecord) mergedFilteredRecords.get(mrs).toArray()[0]).getCounty();
                }
                if (!mergedFilteredRecords.isEmpty()) {
                    mainIOutil.getOutputter().createWorkbook(mainIOutil.getOutputter().getMergedDocPath(mainIOutil.getOutputter().getFilteredDocPath(filter)), mergedFilteredRecords, false, filteredSheetNames, ArrestDateComparator);
                    logger.info("Merge of filtered records complete.");
                } else {
                    logger.info("No filtered records found to merge");
                }
            }
        }

        //TODO test this and move to it's own method
        if (state.meetsLexisNexisCriteria()) {
        	File docPathtoConvert = new File( filter.equals(RecordFilterEnum.NONE)?mainIOutil.getMainDocPath():mainIOutil.getOutputter().getFilteredDocPath(filter));
        	List<Set<Record>> eligibleRecordsCrawled;
        	if (!allMergedRecords.isEmpty()) {
        		//use allMergedRecords if not empty
        		eligibleRecordsCrawled = filterOutLexisNexisEligibleRecords(allMergedRecords);
        	} else {
	            //read records in from main workbook
        		eligibleRecordsCrawled = filterOutLexisNexisEligibleRecords(mainIOutil.getInputter().readRecordsFromWorkbook(docPathtoConvert));
        	}
        	if (!RecordWorkbook.isEmpty(eligibleRecordsCrawled)) {
	        	mainIOutil.getOutputter().createWorkbook(mainIOutil.getOutputter().getLNPath(), eligibleRecordsCrawled, false, new String[]{state.getName()}, ArrestDateComparator);
	        	List<Integer> columnsToRemove = new ArrayList<>();
	        	for (RecordColumnEnum columnEnum : RecordColumnEnum.values()) {
	        		if (!columnEnum.equals(RecordColumnEnum.ARRESTDATE_COLUMN) && !columnEnum.equals(RecordColumnEnum.DOB_COLUMN)
	        				&& !columnEnum.equals(RecordColumnEnum.FIRSTNAME_COLUMN) && !columnEnum.equals(RecordColumnEnum.LASTNAME_COLUMN)) {
	        			columnsToRemove.add(columnEnum.getColumnIndex());
	        		}
	        	}
	        	mainIOutil.getOutputter().removeColumnsFromSpreadsheet(columnsToRemove.toArray(new Integer[columnsToRemove.size()]), mainIOutil.getOutputter().getLNPath());
	        	
	            logger.info("Lexis Nexis input sheet complete.");
        	} else {
        		logger.info("No records eligible for Lexis Nexis were found");
        	}
        } else {
            logger.info("This state does not meet the criteria for Lexis Nexis");
        }
    }
	
	protected List<Set<Record>> filterOutLexisNexisEligibleRecords(List<Set<Record>> records) {
		List<Set<Record>> eligibleRecords = new ArrayList<>();
		for (Set<Record> recordSet : records) {
			Set<Record> eligibleRecordSet = new HashSet<>();
			for (Record record : recordSet) {
				if (((ArrestRecord)record).getDob()!=null && ((ArrestRecord)record).getArrestDate()!=null && ((ArrestRecord)record).getFirstName()!=null&& ((ArrestRecord)record).getLastName()!=null) {
					eligibleRecordSet.add(record);
				}
			}
			eligibleRecords.add(eligibleRecordSet);
		}
		return eligibleRecords;
	}
}
