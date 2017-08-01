package com.mcd.spider.main.entities.site;

import com.mcd.spider.main.entities.service.DesMoinesRegisterComService;
import com.mcd.spider.main.entities.service.Service;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class DesMoinesRegisterComSite implements Site {

	private static final Url url = new Url("http://", "data.desmoinesregister.com/iowa-mugshots/index.php", new String[]{});
	private static final String name = "DesMoinesRegister.com";
	private String baseUrl;
	private int pages;
	private static final int[] perRecordSleepRange = new int[]{1,2};
	private Map<String,Document> resultsPageDocuments;
	private Service service = new DesMoinesRegisterComService();

	public DesMoinesRegisterComSite(String[] args) {
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
	@Override
	public String generateResultsPageUrl(int page) {

        return baseUrl;
	}
	@Override
	public void setOnlyResultsPageDocuments(Map<String,Document> resultsPlusMiscDocumentsMap) {

	}
	@Override
	public Map<String,Document> getResultsPageDocuments() {
		return this.resultsPageDocuments;
	}
	@Override
	public void setBaseUrl(String[] args) {
		if (baseUrl==null) {
            Url url = getUrl();
            String builtUrl = url.getProtocol() + url.getDomain();
            if (args.length>0) {
                builtUrl += "?co=" + args[0];
            }
            baseUrl = builtUrl.toLowerCase();
		}
	}
	@Override
	public String getBaseUrl() {
		return this.baseUrl;
	}
	@Override
	public Elements getRecordElements(Document doc) {
		return null;
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
		return doc.select("h1, p");
	}
	@Override
	public int getTotalPages(Document doc) {
		return pages;
	}
	@Override
	public int getTotalRecordCount(Document doc) {
	    //count elements in json
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
					safeUrls.put(String.valueOf(u),baseUrl + link.attr("href"));
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
            return !doc.select("#msdb-mug-container").isEmpty() && doc.select("#permalink-url").hasText();
        } else {
            return false;
        }
    }

	@Override
	public String getRecordId(String url) {
		return url.substring(url.indexOf("&id=")+4, url.length());
	}

	public Service getService() {
	    return service;
    }

    public List<String> getCounties() {
        return Arrays.asList("Polk", "Johnson", "Story");
    }
}
