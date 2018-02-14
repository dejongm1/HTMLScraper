package com.mcd.spider.entities.site.html;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CaseFormat;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.Url;

/**
 *
 * @author Michael De Jong
 *
 */

public class MugshotsDotComSite  implements SiteHTML{

	public static final Logger logger = Logger.getLogger(MugshotsDotComSite.class);
	
    private static final Url url = new Url("https://", "mugshots.com", new String[]{"/US-Counties"});
    private static final String name = "Mugshots.com";
    private static final int[] perRecordSleepRange = new int[]{1000,5000};
    private String baseUrl;
    private int maxAttempts = 5;
    private State state;

	public MugshotsDotComSite (String[] args) {
	    setBaseUrl(args);
	}
	
    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String[] args) {
        //expect state name OR abbreviation
        //example http://mugshots.com/US-Counties/Mississippi
        setState(State.getState(args[0]));
        if (baseUrl==null) {
            Url urlResult = getUrl();
            String builtUrl = urlResult.getProtocol() + urlResult.getDomain()
                    + urlResult.getExtensions()[0]
                    + (state!=null?"/"+CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, state.getName()):"");
            baseUrl =  builtUrl;
        }
    }

    @Override
    public int[] getPerRecordSleepRange() {
        return perRecordSleepRange;
    }

    private void setState(State state) {
        this.state = state;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Url getUrl() {
        return url;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public String obtainRecordId(String url) {
        if (url.contains("-County-") && url.contains("/US-Counties") && url.endsWith(".html")) {
            return url.substring(url.lastIndexOf("/")+1, url.indexOf(".html"));
        } else {
            return null;
        }
    }

    @Override
    public Elements getRecordElements(Document doc) {
    	return doc.select("div.gallery-listing table tr td a.image-preview");
    }

    @Override
    public String getRecordDetailDocUrl(Element record) {
	    //extracting ID from partially qualified url and rebuilding so /US-Counties/... is not duplicated
    	String pdLink = obtainRecordId(record.attr("href"))!=null?"/"+obtainRecordId(record.attr("href"))+".html":"";
    	if (!pdLink.equals("")) {
            return baseUrl+pdLink;
        } else {
    	    return "";
        }
    }

    @Override
    public Elements getRecordDetailElements(Document doc) {
		return doc.select("#item-info > strong > div.p.graybox > div > div.field");
    }

	@Override
	public String generateResultsPageUrl(String county) {
		//expect county name
		//example http://mugshots.com/US-Counties/Mississippi/Scott-County-MS/
		String builtUrl = baseUrl + "/"
                            + county.replaceAll(" ", "-")
                            + (state!=null?"-County-" + state.getAbbreviation():"");
		return builtUrl;
	}

    @Override
    public Map<Object, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
    	//TODO refine 
    	Elements links = doc.select("a[href]");
		Collections.shuffle(links);
		//get max of one misc page per results page
		//double the size of the list and only fill the second half
		Map<Object, String> safeUrls = new HashMap<>();
		try {
			for (int u=pagesToMatch+1;safeUrls.size()<pagesToMatch;u++) {
				Element link = links.get(u);
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
    	//#counties > ul.cities
    	//AND has "US Counties" in breadcrumbs
    	//AND has an actual county
    	if (!doc.select("#counties > ul.cities").isEmpty()) {
    		Element breadcrumbList = doc.getElementById("small-breadcrumbs");
    		if (breadcrumbList!=null) {
    			Elements breadcrumbs = breadcrumbList.getElementsByAttribute("href");
                return breadcrumbs.get(0).hasText() && breadcrumbs.get(0).text().equals("US Counties")
                        && breadcrumbs.size() >= 3 && breadcrumbs.get(2).text().contains("County");
    		}
    	}
        return false;
    }

    @Override
    public boolean isARecordDetailDoc(Document doc) {
    	//has div itemtype="https://schema.org/Person"
    	//AND has div.p.graybox > div.fieldvalues 
        return !doc.select("div.p.graybox > div.fieldvalues").isEmpty()
                && !doc.select("div[itemtype='http://schema.org/Person']").isEmpty();
    }

	@Override
	public String obtainDetailUrl(String id) {
		//state and county not necessary for ID specific URLs, redirects go to full url
		//generating with state only for now
        return baseUrl + "/" + id + ".html";
	}
	
	public String getNextResultsPageUrl(Document doc) {
        //only get until a certain date is reached (month back?) or None
		boolean olderThanRange;
		long currentTimeInMillis = 0l;
		long rangeToCrawlInMillis = 31*24*60*60*1000l; //1 month
		long nextPageTimeInMillis = 0l;
		String nextPageParameters = "";
		String currentPageUrl = doc.baseUri();
		
		Elements nextPageButtons = doc.select(".pagination .next.page");
		Element nextPageButton = !nextPageButtons.isEmpty()?nextPageButtons.get(0):null;
		if (nextPageButton!=null) {
			nextPageParameters = nextPageButton.attr("href");
		}
		if (!nextPageParameters.contains("None")) {
			Calendar nextPageCalendar = Calendar.getInstance();
			String nextPageDateString = nextPageParameters.substring(nextPageParameters.indexOf('=')+1, nextPageParameters.indexOf('+'));
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date tempDate = sdf.parse(nextPageDateString);
				nextPageCalendar.setTime(tempDate);
				nextPageTimeInMillis = nextPageCalendar.getTimeInMillis();
			} catch (ParseException e) {
				logger.error("Couldn't parse " + nextPageDateString + " into a date during getNextResultsPageUrl() for " + currentPageUrl);
			}
		}
		currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
		olderThanRange = currentTimeInMillis - nextPageTimeInMillis > rangeToCrawlInMillis;
		
		if (!nextPageParameters.equals("") && !olderThanRange) {
			if (currentPageUrl.contains("?")) {
				return currentPageUrl.substring(currentPageUrl.indexOf('?'))+nextPageParameters;
			} else {
				return currentPageUrl+nextPageParameters;
			}
		}
		return null;
	}

}
