package com.mcd.spider.engine.router;

import com.mcd.spider.entities.record.filter.RecordFilter;
import com.mcd.spider.exception.SpiderException;

/**
 * 
 * @author u569220
 *
 */

public interface EngineRouter {
	
	void collectRecords(long maxNumberOfResults, RecordFilter.RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException;
	void collectRecordsUsingThreading(long maxNumberOfResults, RecordFilter.RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException;

}
