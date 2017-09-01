package com.mcd.spider.engine;

import com.mcd.spider.engine.audit.AuditEngine;
import com.mcd.spider.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.engine.router.StateRouter;
import com.mcd.spider.entities.audit.AuditParameters;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.exception.StateNotReadyException;
import com.mcd.spider.util.io.RecordIOUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Set;

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
				
				//TODO split this into it's own method
				RecordIOUtil mainIOutil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(0).getSite());
				//start with the second engine and iterate over the rest
				for (int e=1;e<state.getEngines().size();e++) {
				    //TODO this will overwrite _MERGED if more than 2 engine per site
                    //TODO merge filtered workbooks?
				    logger.info("Attempting to merge record output from " + state);
					RecordIOUtil comparingIOUtil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(e).getSite());
					List<Set<Record>> mergedRecords = mainIOutil.mergeRecordsFromWorkbooks(new File(mainIOutil.getMainDocPath()), new File(comparingIOUtil.getMainDocPath()));
					String[] sheetNames = new String[mergedRecords.size()];
					//TODO fix sheetNames (first one should be state.getName())
					for (int mrs=0;mrs<sheetNames.length;mrs++) {
						sheetNames[mrs] = ((ArrestRecord)mergedRecords.get(mrs).toArray()[0]).getCounty();
					}
					if (!mergedRecords.isEmpty()) {
						mainIOutil.getOutputter().createWorkbook(mainIOutil.getOutputter().getMergedDocPath(), mergedRecords, false, sheetNames);
						logger.info("Merge Complete.");
					} else {
						logger.info("Nothing found to merge");
					}
					
					//TODO convert to using mergeRecordsFromWorkbooks after test cases are written
//					Set<Record> mergedRecords = mainIOutil.mergeRecordsFromSheets(new File(mainIOutil.getMainDocPath()), new File(comparingIOUtil.getMainDocPath()), 0, 0);
//					if (!mergedRecords.isEmpty()) {
//	                    mainIOutil.getOutputter().createWorkbook(mainIOutil.getOutputter().getMergedDocPath(), mergedRecords, false);
//	            		logger.info("Merge Complete.");
//					} else {
//						logger.info("Nothing found to merge");
//					}
				}
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
}
