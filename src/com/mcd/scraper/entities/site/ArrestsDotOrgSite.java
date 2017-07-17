package com.mcd.scraper.entities.site;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArrestsDotOrgSite implements Site {

	private static final Url url = new Url("http://", "arrests.org", new String[]{});
	private static final String name = "Arrests.org";
	private String[] selectors = new String[]{".profile-card .title a",
	   										  ".info .section-content div, .section-content.charges .charge-title, .section-content.charges .charge-description"};
	private String baseUrl;
	private int pages;
	private int totalRecordCount;

	public ArrestsDotOrgSite() {}
	
	@Override
	public Url getUrl() {
		return url;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getBaseUrl(String[] args) {
		if (baseUrl==null) {
			Url url = getUrl();
//			String resultsPerPage = args[1];
//			String pageNumber = args[2];
			String builtUrl = (url.getProtocol() + (args[0]!=null?args[0]+".":"") + url.getDomain());
//			builtUrl += "/?page="+(pageNumber!=null?pageNumber:"1");
//			builtUrl += "&results="+(resultsPerPage!=null?resultsPerPage:"14");
			baseUrl =  builtUrl.toLowerCase();
		}
		return baseUrl;
	}
	@Override
	public Element getRecordElement(Document doc) {
		//need to return a specific record?
		return null;
	}
	@Override
	public Elements getRecordElements(Document doc) {
		return doc.select(".profile-card .title a");
	}
	@Override
	public String getRecordDetailDocUrl(Element record) {
		String pdLink = record.attr("href");
		return getBaseUrl(new String[]{})+pdLink;
	}
	@Override
	public Elements getRecordDetailElements(Document doc) {
		return doc.select(".info .section-content div, .section-content.charges .charge-title, .section-content.charges .charge-description");
	}
	@Override
	public int getPages(Document doc) {
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
			int recordsPerPage = 12;//default
			Elements recordsPerDropdown = doc.select(".content-box .pager-options  option[selected=\"selected\"]");
			for (Element recordsPer : recordsPerDropdown) {
				try {
					recordsPerPage = Integer.parseInt(recordsPer.text());
				} catch (NumberFormatException nfe) {
				}
			}
			int pages = getPages(doc);
			totalRecordCount = recordsPerPage * pages;
		}
		return totalRecordCount;
	}
}
