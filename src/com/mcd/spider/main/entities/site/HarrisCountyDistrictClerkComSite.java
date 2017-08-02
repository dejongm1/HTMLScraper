package com.mcd.spider.main.entities.site;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.entities.service.Service;

public class HarrisCountyDistrictClerkComSite implements Site {
	
	private static final Url url = new Url("http://", "www.hcdistrictclerk.com", new String[]{"/Common", "/e-services", "/PublicDatasets.aspx"});
	private static final String name = "HarrisCountyDistrictClerk.com";
	private String baseUrl;
	private int totalRecordCount;
	private Map<String,Document> resultsPageDocuments;
	private final int maxAttempts = 3;
		
	@Override
	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public void setBaseUrl(String[] arg) {
		baseUrl = url.getProtocol() + url.getDomain();
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
	public Elements getRecordElements(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRecordDetailDocUrl(Element record) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Elements getRecordDetailElements(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalPages(Document doc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalRecordCount(Document doc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String generateResultsPageUrl(int page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getResultsPageDocuments() {
		//store the text file here
		return null;
	}

	@Override
	public int[] getPerRecordSleepRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPageNumberFromDoc(Document doc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAResultsDoc(Document doc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isARecordDetailDoc(Document doc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRecordId(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Service getService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxAttempts() {
		// TODO Auto-generated method stub
		return 0;
	}

}
