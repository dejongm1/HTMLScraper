package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.site.Site;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

public interface SiteHTML extends Site{
	
	Elements getRecordElements(Document doc);
	String getRecordDetailDocUrl(Element record);
	Elements getRecordDetailElements(Document doc);
//	int getTotalPages(Document doc);
//	int getTotalRecordCount(Document doc);
	String generateResultsPageUrl(String arg);
	Map<Object, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch);
	boolean isAResultsDoc(Document doc);
    boolean isARecordDetailDoc(Document doc);
}
