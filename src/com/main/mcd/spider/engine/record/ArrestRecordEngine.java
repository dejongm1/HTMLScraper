package com.main.mcd.spider.engine.record;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.main.mcd.spider.entities.ArrestRecord;
import com.main.mcd.spider.entities.State;
import com.main.mcd.spider.entities.site.Site;
import com.main.mcd.spider.util.ExcelWriter;

public interface ArrestRecordEngine {

	void getArrestRecords(State state, long maxNumberOfResults);
	int scrapeSite(State state, Site site, ExcelWriter excelWriter);
	Map<String,String> parseDocForUrls(Document doc, Site site);
	int scrapeRecords(Map<String, String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter);
	ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site);
	void matchPropertyToField(ArrestRecord record, Element profileDetail);
	void formatName(ArrestRecord record, Element profileDetail);
	String extractValue(Element profileDetail);
	void formatArrestTime(ArrestRecord record, Element profileDetail);
	
}
