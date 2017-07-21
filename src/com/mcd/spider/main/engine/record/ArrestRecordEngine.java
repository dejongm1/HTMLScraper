package com.mcd.spider.main.engine.record;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mcd.spider.main.entities.ArrestRecord;
import com.mcd.spider.main.entities.State;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.util.ExcelWriter;

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
