package com.mcd.spider.engine.record;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Element;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.util.io.RecordIOUtil;

public interface ArrestRecordEngine {

	Site getSite();
	void setSpiderWeb(SpiderWeb web);
	RecordIOUtil getRecordIOUtil();
	SpiderWeb getSpiderWeb();
	void getArrestRecords() throws SpiderException;
	void scrapeSite();
	Map<String,String> parseDocForUrls(Object objectToParse);
	void scrapeRecords(Map<Object, String> recordsDetailsUrlMap);
	ArrestRecord populateArrestRecord(Object profileDetailObj);
	void matchPropertyToField(ArrestRecord record, Object profileDetail);
	Object initiateConnection(String arg) throws IOException;
	RecordIOUtil initializeIOUtil(String stateName) throws SpiderException;
	void setCookies(Response response);
	void finalizeOutput(List<Record> arrestRecords);
	void formatName(ArrestRecord record, Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	List<Record> filterRecords(List<Record> fullArrestRecords);
	
}
