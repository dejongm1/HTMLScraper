package com.mcd.spider.main.engine.router;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.exception.SpiderException;
import common.Logger;

/**
 * 
 * @author u569220
 *
 */

public class StateRouter implements EngineRouter {

	private static final Logger logger = Logger.getLogger(StateRouter.class);
	private State state;
	
	public StateRouter(State state) {
		this.state = state;
	}
	
	
	@Override
	public void collectRecords(long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException {
        logger.info("Routing record collection to " + state.getName() + " engines");

        for (ArrestRecordEngine engine : state.getEngines()) {
	        logger.info("Collecting records from " + engine.getClass().getSimpleName() );
	        engine.getArrestRecords(state, maxNumberOfResults, filter);
        }
	}
	
	@Override
	public void collectRecordsUsingThreading(long maxNumberOfResults, RecordFilter.RecordFilterEnum filter){
		logger.info("Routing record collection to " + state.getName() + " engines");

		for (ArrestRecordEngine engine : state.getEngines()) {
			new Thread("" + engine.getClass().getSimpleName()){
				@Override
				public void run(){
					logger.info("Thread: " + getName() + " running");
					logger.info("Collecting records from " + engine.getClass().getSimpleName() );
					try {
						engine.getArrestRecords(state, maxNumberOfResults, filter);
					} catch (SpiderException e) {
						logger.error("Thread: " + getName() + " caught an exception", e);
					}
				}
			}.start();

		}
	}

}
