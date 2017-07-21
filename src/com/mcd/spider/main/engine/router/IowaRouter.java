package com.mcd.spider.main.engine.router;

import com.mcd.spider.main.engine.record.iowa.PolkCountyOrgEngine;
import com.mcd.spider.main.entities.State;

import common.Logger;

/**
 * 
 * @author u569220
 *
 */

public class IowaRouter implements EngineRouter {
	
	private State state;
	
	public IowaRouter(State state) {
		this.state = state;
	}
	
	private static final Logger logger = Logger.getLogger(IowaRouter.class);
	
	@Override
	public void collectRecords(long maxNumberOfResults) {
		PolkCountyOrgEngine pcoEngine = new PolkCountyOrgEngine();
		logger.info("Routing record collection to Iowa engines");
		
		logger.info("Collecting records from PolkCountyIowa.Org");
		pcoEngine.getArrestRecords(state, maxNumberOfResults);
	}

}
