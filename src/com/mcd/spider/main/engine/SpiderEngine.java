package com.mcd.spider.main.engine;

import java.util.List;

import org.apache.log4j.Logger;

import com.mcd.spider.main.engine.audit.AuditEngine;
import com.mcd.spider.main.engine.router.IowaRouter;
import com.mcd.spider.main.entities.audit.AuditParameters;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.exception.StateNotReadyException;

/**
 * 
 * @author U569220
 *
 */

public class SpiderEngine {

	public static final Logger logger = Logger.getLogger(SpiderEngine.class);

	//redirect to Appropriate engine from here
	public void getArrestRecordsByState(List<State> states, long maxNumberOfResults) throws StateNotReadyException {
		//TODO use threading here for multiple states, maybe even within states
		for (State state : states) {
			//use reflection for router?
			if (state.getName().equals("Iowa")) {
				IowaRouter router = new IowaRouter(state);
				router.collectRecords(maxNumberOfResults);
			} else {
				throw new StateNotReadyException(state);
			}
		}
		
	}
	
	public void performSEOAudit(AuditParameters auditParams) {
		AuditEngine engine = new AuditEngine();
		engine.performSEOAudit(auditParams);
	}
	
	public void search(String url, String words) {
		AuditEngine engine = new AuditEngine();
		engine.search(url, words, 0);//TODO allow for levelOfGenerosity
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
