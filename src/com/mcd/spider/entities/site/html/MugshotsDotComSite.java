package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.site.Url;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

public class MugshotsDotComSite  implements SiteHTML{

    private static final Url url = new Url("https://", "mugshots.com", new String[]{"US-Counties"});
    private static final String name = "Mugshots.com";
    private static final int[] perRecordSleepRange = new int[]{1000,2000};
    private String baseUrl;
    private int totalRecordCount;
    private Map<String,Document> resultsPageDocuments;
    private int maxAttempts = 5;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String[] args) {
        if (baseUrl==null) {
            Url url = getUrl();
            String builtUrl = url.getProtocol() + url.getDomain() + (args[0]!=null?args[0]+"/":"");
            baseUrl =  builtUrl.toLowerCase();
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
    public String generateRecordId(String url) {
        //example http://mugshots.com/US-Counties/Iowa/Scott-County-IA/Steven-Allen-Gayman-Jr.141436546.html
        if (url.contains("-County-") && url.contains("/US-Counties") && url.endsWith(".html")) {
            String subString =  url.substring(url.indexOf("-County-")+11, url.indexOf(".html"));
            return subString.substring(subString.indexOf('.'));
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
//        return null;
    }

    @Override
    public String getRecordDetailDocUrl(Element record) {
//        return null;
    }

    @Override
    public Map<String, String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
//        return null;
    }

    @Override
    public Elements getRecordDetailElements(Document doc) {
//        return null;
    }

    @Override
    public int getTotalPages(Document doc) {
//        can I do this?
//        return 0;
    }

    @Override
    public int getTotalRecordCount(Document doc) {
        return 0;
    }

    @Override
    public String generateResultsPageUrl(int page) {
//        return null;
    }

    @Override
    public Map<String, Document> getResultsPageDocuments() {
//        return null;
    }

    @Override
    public int getPageNumberFromDoc(Document doc) {
//        return 0;
    }

    @Override
    public Map<Object, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
//        return null;
    }

    @Override
    public boolean isAResultsDoc(Document doc) {
//        return false;
    }

    @Override
    public boolean isARecordDetailDoc(Document doc) {
//        return false;
    }
}
