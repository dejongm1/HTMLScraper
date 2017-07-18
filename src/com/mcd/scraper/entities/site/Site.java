package com.mcd.scraper.entities.site;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface Site {
	
	
	public String getBaseUrl(String[] arg);
	public String getName();
	public Url getUrl();
//	public Record getRecord();
//	public List<Record> getRecords();
	public Element getRecordElement(Document doc);
	public Elements getRecordElements(Document doc);
	public String getRecordDetailDocUrl(Element record);
	public Elements getRecordDetailElements(Document doc);
	public int getPages(Document doc);
	public int getTotalRecordCount(Document doc);
	public String getResultsPageUrl(int page, int resultsPerPage);
	public int getPerRecordSleepTime();
	
}
