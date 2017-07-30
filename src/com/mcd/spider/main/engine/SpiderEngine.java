package com.mcd.spider.main.engine;

import com.mcd.spider.main.engine.audit.AuditEngine;
import com.mcd.spider.main.engine.record.ArrestsDotOrgEngine;
import com.mcd.spider.main.engine.router.StateRouter;
import com.mcd.spider.main.entities.audit.AuditParameters;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.StateNotReadyException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 
 * @author U569220
 *
 */

public class SpiderEngine {

	public static final Logger logger = Logger.getLogger(SpiderEngine.class);

	//redirect to Appropriate engine from here
	public void getArrestRecordsByState(List<State> states, long maxNumberOfResults) throws StateNotReadyException, ExcelOutputException {
		//TODO use threading here for multiple states, maybe even within states
		for (State state : states) {
			if (state.getEngines().size()!=0) {
				StateRouter router = new StateRouter(state);
				router.collectRecords(maxNumberOfResults);
			} else {
				throw new StateNotReadyException(state);
			}
		}
		
	}
	
	public void getArrestRecordsByStateCrack(List<State> states, long maxNumberOfResults) throws StateNotReadyException, ExcelOutputException {
		for (State state : states) {
			state.getEngines().removeAll(state.getEngines());
			state.addEngine(new ArrestsDotOrgEngine());
//			state.addEngine(new MugShotsDotComEngine());
			StateRouter router = new StateRouter(state);
			router.collectRecords(maxNumberOfResults);
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
