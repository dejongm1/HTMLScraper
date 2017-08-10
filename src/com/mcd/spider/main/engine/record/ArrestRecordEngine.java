package com.mcd.spider.main.engine.record;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.OutputUtil;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Map;

public interface ArrestRecordEngine {

	Site getSite(String[] args);
	void getArrestRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException;
	int scrapeSite(State state, Site site, OutputUtil outputUtil, int attemptCount, long maxNumberOfResults);
	Map<String,String> parseDocForUrls(Object objectToParse, Site site);
	int scrapeRecords(Map<Object, String> recordsDetailsUrlMap, Site site, OutputUtil outputUtil, Map<String,String> cookies);
	ArrestRecord populateArrestRecord(Object profileDetailObj, Site site);
	void matchPropertyToField(ArrestRecord record, Object profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	OutputUtil initializeOutputter(State state, Site site) throws SpiderException;
	List<Record> filterRecords(List<Record> fullArrestRecords);
	
}
