package com.mcd.spider.main.engine.router;

import com.mcd.spider.main.exception.SpiderException;

/**
 * 
 * @author u569220
 *
 */

public interface EngineRouter {
	
	void collectRecords(long maxNumberOfRecords) throws SpiderException;

}
