package com.mcd.scraper.entities.site;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface Site {
	
	
	String getBaseUrl(String[] arg);
	String getName();
	Url getUrl();
//	public Record getRecord();
//	public List<Record> getRecords();
	Element getRecordElement(Document doc);
	Elements getRecordElements(Document doc);
	String getRecordDetailDocUrl(Element record);
	Elements getRecordDetailElements(Document doc);
	int getPages(Document doc);
	int getTotalRecordCount(Document doc);
	String getResultsPageUrl(int page/*, int resultsPerPage*/);
	int[] getPerRecordSleepRange();
	
}
