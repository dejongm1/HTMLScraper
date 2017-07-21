package com.main.mcd.spider.entities.site;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DesMoinesRegisterComSite implements Site {

	private static final Url url = new Url("http://", "data.desmoinesregister.com/iowa-mugshots/index.php", new String[]{});
	private static final String name = "DesMoinesRegister.com";
	private String baseUrl;
	private int pages;
	private int totalRecordCount;
	private static final int[] perRecordSleepRange = new int[]{5,15};
	private Map<String,Document> resultsPageDocuments;

	public DesMoinesRegisterComSite() {}
	
	@Override
	public Url getUrl() {
		return url;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public int[] getPerRecordSleepRange() {
		return perRecordSleepRange;
	}
	@Override
	public String generateResultsPageUrl(int page) {
//		String builtUrl = baseUrl;
//        int pageRangeMax = page*20;
//		int pageRangeMin = pageRangeMax-19;
//		builtUrl += "/" + pageRangeMin + "-" + pageRangeMax;
//		return builtUrl;
        return baseUrl;
	}
	@Override
	public void setOnlyResultsPageDocuments(Map<String,Document> resultsPlusMiscDocumentsMap) {
		Map<String,Document> resultsDocMap = new HashMap<>();
		for(Entry<String, Document> entry : resultsPlusMiscDocumentsMap.entrySet()) {
			if (isAResultsDoc(entry.getValue())) {
				resultsDocMap.put(entry.getKey(), entry.getValue());
			}
		}
		this.resultsPageDocuments = resultsDocMap;
	}
	@Override
	public Map<String,Document> getResultsPageDocuments() {
		return this.resultsPageDocuments;
	}
	@Override
	public String getBaseUrl(String[] args) {
		if (baseUrl==null) {
            Url url = getUrl();
            String builtUrl = url.getProtocol() + url.getDomain() + "?co=" + args[0];
            baseUrl = builtUrl.toLowerCase();
		}
		return baseUrl;
	}
	@Override
	public Elements getRecordElements(Document doc) {
		return doc.select("#msdb-top #msdb-mugshots .mug-row a");
	}
	@Override
	public String getRecordDetailDocUrl(Element record) {
		String pdId = record.attr("id");
		return baseUrl+ "&id=" + pdId;
	}
	@Override
	public Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
		return null;
	}
	@Override
	public Elements getRecordDetailElements(Document doc) {
		return doc.select("#msdb-details-overlay #msdb-inmate-details");
	}
	@Override
	public int getTotalPages(Document doc) {
		if (pages==0) {
			//Elements pageCountElements = doc.select(".msdb-tab-content .msdb-table-page-control");// li :nth-last-child(2)");
            Elements pageCountElements = doc.select(".msdb-tab.noselect.active");
			try {
				pages = Integer.parseInt(pageCountElements.get(0).text());
			} catch (NumberFormatException | NullPointerException e) {
				pages = 0;
			}
		}
		return pages;
	}
	@Override
	public int getTotalRecordCount(Document doc) {
//		if (totalRecordCount==0) {
//			int recordsPerPage = 14;//default
//			Elements recordsPerDropdown = doc.select(".content-box .pager-options  option[selected=\"selected\"]");
//			for (Element recordsPer : recordsPerDropdown) {
//				try {
//					recordsPerPage = Integer.parseInt(recordsPer.text());//try to get actual
//				} catch (NumberFormatException nfe) {
//				}
//			}
//			int pages = getTotalPages(doc);
//			totalRecordCount = recordsPerPage * pages;
//		}
//		return totalRecordCount;
        return 0;
	}
	@Override
	public int getPageNumberFromDoc(Document doc) {
//		String baseUri = doc.baseUri();
//		return Character.getNumericValue(baseUri.charAt(baseUri.indexOf('&')-1));
        return 0;
	}
	@Override
	public Map<String,String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
		Elements links = doc.select("a[href]");
		Collections.shuffle(links);
		//get one misc page per results page
		//double the size of the list and only fill the second half
		Map<String,String> safeUrls = new HashMap<>();
		try {
			for (int u=pagesToMatch+1;u<pagesToMatch*2;u++) {
				Element link = links.get(u);
				//if () {
					safeUrls.put(String.valueOf(u),getBaseUrl(null) + link.attr("href"));
				//}
			}
		} catch (IndexOutOfBoundsException aiobe) {
			aiobe.printStackTrace();
			return safeUrls;
		}
		return safeUrls;
	}

    @Override
    public boolean isAResultsDoc(Document doc) {
//        if (doc!=null) {
//            return doc.baseUri().contains("index.php?co=") && doc.baseUri().contains("&id=");
//        } else {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isARecordDetailDoc(Document doc) {
        if (doc!=null) {
            return doc.baseUri().contains("index.php?co=") && doc.baseUri().contains("&id=");
        } else {
            return false;
        }
    }

	@Override
	public String getRecordId(String url) {
		return url.substring(url.indexOf("&id=")+4, url.length()-1);
	}

}
