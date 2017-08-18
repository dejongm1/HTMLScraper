package com.mcd.spider.main.engine.record;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.io.RecordIOUtil;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Map;

public interface ArrestRecordEngine {

	void getArrestRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException;
	int scrapeSite(int attemptCount, long maxNumberOfResults);
	Map<String,String> parseDocForUrls(Object objectToParse);
	int scrapeRecords(Map<Object, String> recordsDetailsUrlMap, Map<String,String> cookies, long maxNumberOfResults);
	ArrestRecord populateArrestRecord(Object profileDetailObj);
	void matchPropertyToField(ArrestRecord record, Object profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	RecordIOUtil initializeIOUtil(State state) throws SpiderException;
	List<Record> filterRecords(List<Record> fullArrestRecords);
	
}
