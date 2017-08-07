package com.mcd.spider.main.entities.site.html;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.entities.site.Site;

public interface SiteHTML extends Site{
	
	Elements getRecordElements(Document doc);
	String getRecordDetailDocUrl(Element record);
	Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs);
	Elements getRecordDetailElements(Document doc);
	int getTotalPages(Document doc);
	int getTotalRecordCount(Document doc);
	String generateResultsPageUrl(int page/*, int resultsPerPage*/);
	Map<String, Document> getResultsPageDocuments();
	int getPageNumberFromDoc(Document doc);
	Map<Object, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch);
	boolean isAResultsDoc(Document doc);
    boolean isARecordDetailDoc(Document doc);
}
