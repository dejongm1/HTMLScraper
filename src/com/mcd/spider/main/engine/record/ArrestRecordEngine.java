package com.mcd.spider.main.engine.record;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ExcelWriter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;

public interface ArrestRecordEngine {

	Site getSite(String[] args);
	void getArrestRecords(State state, long maxNumberOfResults) throws SpiderException;
	int scrapeSite(State state, Site site, ExcelWriter excelWriter);
	Map<String,String> parseDocForUrls(Object objectToParse, Site site);
	int scrapeRecords(Map<String, String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter);
	ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site);
	void matchPropertyToField(ArrestRecord record, Element profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	ExcelWriter initializeOutputter(State state, Site site) throws SpiderException;
	
}
