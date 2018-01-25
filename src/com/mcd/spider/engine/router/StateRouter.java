package com.mcd.spider.engine.router;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.exception.SpiderException;
import common.Logger;

/**
 * 
 * @author Michael De Jong
 *
 */

public class StateRouter implements EngineRouter {

	private static final Logger logger = Logger.getLogger(StateRouter.class);
	private State state;
	
	public StateRouter(State state) {
		this.state = state;
	}
	
	
	@Override
	public void collectRecords() throws SpiderException {
        logger.info("Routing record collection to " + state.getName() + " engines");

        for (ArrestRecordEngine engine : state.getEngines()) {
	        logger.info("Collecting records from " + engine.getClass().getSimpleName() );
			engine.getArrestRecords();
        }
	}
	
	@Override
	public void collectRecordsUsingThreading() {
		logger.info("Routing record collection to " + state.getName() + " engines");

		Thread[] threads = new Thread[state.getEngines().size()];
		int t = 0;
		for (ArrestRecordEngine engine : state.getEngines()) {
			threads[t] = new Thread("" + engine.getClass().getSimpleName()){
				@Override
				public void run(){
					logger.info("Thread: " + getName() + " running");
					logger.info("Collecting records from " + engine.getClass().getSimpleName() );
					try {
						engine.getArrestRecords();
					} catch (SpiderException e) {
						logger.error("Thread: " + getName() + " caught an exception", e);
					}
				}
			};
			threads[t].start();
			t++;
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("Error starting or joining thread", e);
			}
		}
	}

}
