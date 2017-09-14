package com.mcd.spider.engine.router;

import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.exception.SpiderException;

/**
 * 
 * @author u569220
 *
 */

public interface EngineRouter {
	
	void collectRecords(SpiderWeb spiderWeb) throws SpiderException;
	void collectRecordsUsingThreading(SpiderWeb spiderWeb) throws SpiderException;

}
