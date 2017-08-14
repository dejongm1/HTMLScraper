package com.mcd.spider.main.engine.record;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.RecordOutputUtil;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Map;

public interface ArrestRecordEngine {

	Site getSite(String[] args);
	void getArrestRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException;
	int scrapeSite(Site site, RecordOutputUtil recordOutputUtil, int attemptCount, long maxNumberOfResults);
	Map<String,String> parseDocForUrls(Object objectToParse, Site site);
	int scrapeRecords(Map<Object, String> recordsDetailsUrlMap, Site site, RecordOutputUtil recordOutputUtil, Map<String,String> cookies);
	ArrestRecord populateArrestRecord(Object profileDetailObj, Site site);
	void matchPropertyToField(ArrestRecord record, Object profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	RecordOutputUtil initializeOutputter(State state, Site site) throws SpiderException;
	List<Record> filterRecords(List<Record> fullArrestRecords);
	
}
