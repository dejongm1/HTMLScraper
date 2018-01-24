package com.mcd.spider.engine.router;

import com.mcd.spider.exception.SpiderException;

/**
 * 
 * @author Michael De Jong
 *
 */

public interface EngineRouter {
	
	void collectRecords() throws SpiderException;
	void collectRecordsUsingThreading() throws SpiderException;

}
