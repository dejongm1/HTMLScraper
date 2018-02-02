package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.Url;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArrestsDotOrgSite implements SiteHTML {

	public static final Logger logger = Logger.getLogger(ArrestsDotOrgSite.class);

	private static final Url url = new Url("https://", "arrests.org", new String[]{});
	private static final String name = "ArrestsOrg";
	private String baseUrl;
	private int pages;
	private int totalRecordCount;
	private static final int[] perRecordSleepRange = new int[]{5000,15000};
	private int maxAttempts = 5;
	private int resultsPerPage = 56;
    private State state;

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
	public void setBaseUrl(String[] args) {
	    //expects state name
        setState(State.getState(args[0]));
	    //TODO what if not null? will the baseUrl ever need be changed?
		if (baseUrl==null) {
			Url url = getUrl();
			String builtUrl = url.getProtocol() + (state!=null?state.getName()+".":"") + url.getDomain();
			baseUrl =  builtUrl.toLowerCase();
		}
	}

    private void setState(State state) {
        this.state = state;
    }
	@Override
	public String getBaseUrl() {
		return baseUrl;
	}
    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }
//	@Override
//	public Element getRecordElement(Document doc) {
//		//need to return a specific record?
//		return null;
//	}

    @Override
    public String obtainRecordId(String url) {
        if (!url.equals("") && url.contains("/Arrests/") && url.contains("/?d=1")) {
            return url.substring(url.indexOf("/Arrests/")+9, url.indexOf("/?d=1"));
        } else {
            return null;
        }
    }

    @Override
	public Elements getRecordElements(Document doc) {
	    //TODO is this working properly?
		return doc.select(".content-box .search-results .profile-card .title a");
	}
	@Override
	public String getRecordDetailDocUrl(Element record) {
		String pdLink = record.attr("href");
		if (!pdLink.equals("")) {
            return baseUrl+pdLink;
        } else {
		    return "";
        }
	}
	@Override
	public Elements getRecordDetailElements(Document doc) {
		return doc.select(".content-box.profile.profile-full h3, .info .section-content div, .section-content.charges, img[src^=\"/mugs/\"]");
	}
	//@Override
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
	//TODO get rid of if not using
//	@Override
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
	public String generateResultsPageUrl(String pageNumber) {
		String builtUrl = baseUrl;
		builtUrl += "/?page="+pageNumber;
		builtUrl += "&results=" + resultsPerPage;
		return builtUrl;
	}
	//TODO get rid of if never used
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
    public String obtainDetailUrl(String id) {
        return baseUrl + "/Arrests/" + id + "/?d=1";
    }
}
