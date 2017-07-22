package com.mcd.spider.main.entities.site;

import com.mcd.spider.main.entities.service.Service;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

public interface Site {
	
	
	String getBaseUrl(String[] arg);
	String getName();
	Url getUrl();
//	Element getRecordElement(Document doc);
	Elements getRecordElements(Document doc);
	String getRecordDetailDocUrl(Element record);
	Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs);
	Elements getRecordDetailElements(Document doc);
	int getTotalPages(Document doc);
	int getTotalRecordCount(Document doc);
	String generateResultsPageUrl(int page/*, int resultsPerPage*/);
	Map<String, Document> getResultsPageDocuments();
	void setOnlyResultsPageDocuments(Map<String,Document> resultsPlusMiscDocuments);
	int[] getPerRecordSleepRange();
	int getPageNumberFromDoc(Document doc);
	Map<String, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch);
	boolean isAResultsDoc(Document doc);
    boolean isARecordDetailDoc(Document doc);
	String getRecordId(String url);
	Service getService();
}
