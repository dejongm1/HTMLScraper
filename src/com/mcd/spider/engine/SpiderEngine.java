package com.mcd.spider.engine;

import com.mcd.spider.engine.audit.AuditEngine;
import com.mcd.spider.engine.record.various.ArrestsDotOrgEngine;
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
import java.util.ArrayList;
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
				router.collectRecords(maxNumberOfResults, filter, retrieveMissedRecords);
			} else {
				throw new StateNotReadyException(state);
			}
		}
	}
	
	public void getArrestRecordsThroughTheBackDoor(List<State> states, long maxNumberOfResults, RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException {
		for (State state : states) {
			state.getEngines().clear();
            state.addEngine(new ArrestsDotOrgEngine());
//			state.addEngine(new DesMoinesRegisterComEngine());
//			state.addEngine(new MugShotsDotComEngine());
			StateRouter router = new StateRouter(state);
			router.collectRecords(maxNumberOfResults, filter, retrieveMissedRecords);
		}
		
	}
	
	public void getArrestRecordsByThreading(List<State> states, long maxNumberOfResults, RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException {
		for (State state : states) {
			if (!state.getEngines().isEmpty()) {
				StateRouter router = new StateRouter(state);
				router.collectRecordsUsingThreading(maxNumberOfResults, filter, retrieveMissedRecords);
				RecordIOUtil mainIOutil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(0).getSite());
				//start with the second engine and iterate over the rest
				for (int e=1;e<state.getEngines().size();e++) {
				    //TODO this will likely overwrite _MERGED if more than 2 engine per site
				    logger.info("Attempting to merge record output from " + state);
					RecordIOUtil comparingIOUtil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(e).getSite());
					Set<Record> mergedRecords = mainIOutil.mergeRecordsFromSheet(new File(mainIOutil.getMainDocName()), new File(comparingIOUtil.getMainDocName()), 0);
                    mainIOutil.getOutputter().createMergedSpreadsheet(new ArrayList<>(mergedRecords));
				}
			} else {
				throw new StateNotReadyException(state);
			}
		}
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
