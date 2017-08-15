package com.mcd.spider.main.entities.site.html;

import com.mcd.spider.main.entities.site.Url;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ArrestsDotOrgSite implements SiteHTML {

	private static final Url url = new Url("http://", "arrests.org", new String[]{});
	private static final String name = "ArrestsOrg";
	private String baseUrl;
	private int pages;
	private int totalRecordCount;
	private static final int[] perRecordSleepRange = new int[]{5000,15000};
	private Map<String,Document> resultsPageDocuments;
	private int maxAttempts = 3;
	private int resultsPerPage = 56;

	public ArrestsDotOrgSite (String[] args) {
		setBaseUrl(args);
	}
	
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
	public int getResultsPerPage() {
		return resultsPerPage;
	}
	@Override
	public String generateResultsPageUrl(int page/*, int resultsPerPage*/) {
		String builtUrl = baseUrl;
		builtUrl += "/?page="+page;
		/*if (resultsPerPage % 14 == 0) {
			builtUrl += "&results="+resultsPerPage;
		} else {*/
			builtUrl += "&results=" + resultsPerPage;
		/*}*/
		return builtUrl;
	}

	public void setOnlyResultsPageDocuments(Map<String,Document> resultsPlusMiscDocumentsMap) {
		Map<String,Document> resultsDocMap = new HashMap<>();
		for(Entry<String, Document> entry : resultsPlusMiscDocumentsMap.entrySet()) {
			if (isAResultsDoc( entry.getValue())) {
				resultsDocMap.put(entry.getKey(), entry.getValue());
			}
		}
		this.resultsPageDocuments = resultsDocMap;
	}
	@Override
	public Map<String, Document> getResultsPageDocuments() {
		return this.resultsPageDocuments;
	}
	@Override
	public void setBaseUrl(String[] args) {
		if (baseUrl==null) {
			Url url = getUrl();
//			String resultsPerPage = args.length>1?args[1]:null;
//			String pageNumber = args.length>2?args[2]:null;
			String builtUrl = url.getProtocol() + (args[0]!=null?args[0]+".":"") + url.getDomain();
//			builtUrl += "/?page="+(pageNumber!=null?pageNumber:"1");
//			builtUrl += "&results="+(resultsPerPage!=null?resultsPerPage:"56");
			baseUrl =  builtUrl.toLowerCase();
		}
	}
	@Override
	public String getBaseUrl() {
		return this.baseUrl;
	}
//	@Override
//	public Element getRecordElement(Document doc) {
//		//need to return a specific record?
//		return null;
//	}
	@Override
	public Elements getRecordElements(Document doc) {
		return doc.select(".search-results .profile-card .title a");
	}
	@Override
	public String getRecordDetailDocUrl(Element record) {
		String pdLink = record.attr("href");
//		pdLink = pdLink.replace("?d=1", "");
		return baseUrl+pdLink;
	}
	@Override
	public Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
		//TODO
		return null;
	}
	@Override
	public Elements getRecordDetailElements(Document doc) {
		return doc.select(".content-box.profile.profile-full h3, .info .section-content div, .section-content.charges, img[src^=\"/mugs/\"]");
	}
	@Override
	public int getTotalPages(Document doc) {
		if (pages==0) {
			Elements pageCountElements = doc.select(".content-box .pager :nth-last-child(2)");
			try {
				pages = Integer.parseInt(pageCountElements.get(0).text());
			} catch (NumberFormatException nfe) {
				pages = 0;
			}
		}
		return pages;
	}
	@Override
	public int getTotalRecordCount(Document doc) {
		if (totalRecordCount==0) {
			int recordsPerPage = 14;//default
			Elements recordsPerDropdown = doc.select(".content-box .pager-options  option[selected=\"selected\"]");
			for (Element recordsPer : recordsPerDropdown) {
				try {
					recordsPerPage = Integer.parseInt(recordsPer.text());//try to get actual
				} catch (NumberFormatException nfe) {
				}
			}
			int pages = getTotalPages(doc);
			totalRecordCount = recordsPerPage * pages;
		}
		return totalRecordCount;
	}
	@Override
	public int getPageNumberFromDoc(Document doc) {
		String baseUri = doc.baseUri();
		return Character.getNumericValue(baseUri.charAt(baseUri.indexOf('&')-1));
	}
	@Override
	public Map<Object, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
		Elements links = doc.select("a[href]");
		Collections.shuffle(links);
		//get max of one misc page per results page
		//double the size of the list and only fill the second half
		Map<Object, String> safeUrls = new HashMap<>();
		try {
			for (int u=pagesToMatch+1;safeUrls.size()<pagesToMatch;u++) {
				Element link = links.get(u);
				//(ignore rel=stylesheet, include '/ABC/', '/', '/Arrests/ABC')
				if (!link.hasAttr("rel") &&  !link.attr("href").contains("?d=1")
						&& (link.attr("href").startsWith("/Arrests/")
							//|| link.attr("href").equals("#")
							|| link.attr("href").matches("/[a-zA-Z]+/")
							|| link.attr("href").equals("/"))) {
					if (!safeUrls.containsValue(baseUrl + link.attr("href"))) {
						safeUrls.put(u,baseUrl + link.attr("href"));
					}
				}
			}
		} catch (IndexOutOfBoundsException aiobe) {
			//this catches if there aren't as many unique safe misc urls as pages to match
			return safeUrls;
		}
		return safeUrls;
	}

    @Override
    public boolean isAResultsDoc(Document doc) {
        if (doc!=null) {
            return doc.baseUri().contains("/?page=") && doc.baseUri().contains("&results=");
        } else {
            return false;
        }
    }

    @Override
    public boolean isARecordDetailDoc(Document doc) {
        if (doc!=null) {
            return doc.baseUri().matches(".*[A-Za-z]+_[0-9]/?.+") && doc.baseUri().endsWith("/?d=1");
        } else {
            return false;
        }
    }

	@Override
	public String generateRecordId(String url) {
		return url.substring(url.indexOf("/Arrests/")+9, url.indexOf("/?d=1"));
	}

    @Override
    public int getMaxAttempts() {
    	return maxAttempts;
    }
}
