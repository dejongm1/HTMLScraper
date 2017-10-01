package com.mcd.spider.engine.router;

import com.mcd.spider.exception.SpiderException;

/**
 * 
 * @author u569220
 *
 */

public interface EngineRouter {
	
	void collectRecords() throws SpiderException;
	void collectRecordsUsingThreading() throws SpiderException;

}
