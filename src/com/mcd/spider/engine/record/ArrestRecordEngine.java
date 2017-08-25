package com.mcd.spider.engine.record;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.util.io.RecordIOUtil;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ArrestRecordEngine {

	Site getSite();
	void getArrestRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter, boolean retrieveMissedRecords) throws SpiderException;
	void scrapeSite();
	Map<String,String> parseDocForUrls(Object objectToParse);
	void scrapeRecords(Map<Object, String> recordsDetailsUrlMap);
	ArrestRecord populateArrestRecord(Object profileDetailObj);
	void matchPropertyToField(ArrestRecord record, Object profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	RecordIOUtil initializeIOUtil(State state) throws SpiderException;
	List<Record> filterRecords(List<Record> fullArrestRecords);
	Object initiateConnection(String arg) throws IOException;
	void formatOutput(List<Record> arrestRecords);
	void setCookies(Response response);
	
}
