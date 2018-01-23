package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.site.Url;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MugshotsDotComSite  implements SiteHTML{

    private static final Url url = new Url("https://", "mugshots.com", new String[]{"US-Counties"});
    private static final String name = "Mugshots.com";
    private static final int[] perRecordSleepRange = new int[]{1000,2000};
    private String baseUrl;
    private int maxAttempts = 5;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String[] args) {
        if (baseUrl==null) {
            Url urlResult = getUrl();
            String builtUrl = urlResult.getProtocol() + urlResult.getDomain() + (args[0]!=null?args[0]+"/":"");
            baseUrl =  builtUrl;
        }
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
        //example http://mugshots.com/US-Counties/Iowa/Scott-County-IA/George-Allen-Freeman-Jr.141436546.html
        if (url.contains("-County-") && url.contains("/US-Counties") && url.endsWith(".html")) {
            return url.substring(url.indexOf("-County-")+11, url.indexOf(".html"));
            //OR return url.substring(lastIndexOf("/", ), url.indexOf(".html"));
        } else {
            return null;
        }
    }

    @Override
    public int[] getPerRecordSleepRange() {
        return perRecordSleepRange;
    }

    @Override
    public Elements getRecordElements(Document doc) {
    	return doc.select("#main > div > div.gallery-listing > table > tbody > tr:nth-child(1) > td");
    }

    @Override
    public String getRecordDetailDocUrl(Element record) {		
    	String pdLink = record.attr("href");
    	return baseUrl+pdLink;
    }

    @Override
    public Elements getRecordDetailElements(Document doc) {
    	//TODO any others? charges?
		return doc.select("#item-info > strong > div.p.graybox > div > div.field");
    }

    @Override
    public int getTotalPages(Document doc) {
    	//TODO can't be determined without scrolling through pages
    	//do some math on count shown in county list?
        return 0;
    }

    @Override
    public int getTotalRecordCount(Document doc) {
    	//TODO use count shown in county list? does it round up or down?
        return 0;
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
    	//uri has US-Counties
    	//AND #counties > ul.cities
    	//AND has "US Counties" in breadcrumbs
    	//AND has an actual county
    	if (doc.baseUri().contains("US-Counties") && !doc.select("#counties > ul.cities").isEmpty()) {
    		Element breadcrumbList = doc.getElementById("small-breadcrumbs");
    		if (breadcrumbList!=null) {
    			Elements breadcrumbs = breadcrumbList.getElementsByAttribute("href");
    			if (breadcrumbs.get(0).hasText() && breadcrumbs.get(0).text().equals("US-Counties")
    					&& breadcrumbs.size()>=3) {
    				return true;
    			}
    		}
    	}
        return false;
    }

    @Override
    public boolean isARecordDetailDoc(Document doc) {
    	//uri has US-Counties
    	//AND has div itemtype="https://schema.org/Person"
    	//AND has div.p.graybox > div.fieldvalues 
    	if (doc.baseUri().contains("US-Counties") 
    			&& !doc.select("div.p.graybox > div.fieldvalues").isEmpty()
    			&& !doc.select("div[itemtype=\"https://schema.org/Person\"]").isEmpty()) {
    		return true;
    	}
        return false;
    }

	@Override
	public String obtainDetailUrl(String id) {
        return baseUrl + "/" + id + ".html";
	}
}
