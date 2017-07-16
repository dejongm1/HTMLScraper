package com.mcd.scraper;

public class Site {

	private String protocol;
	private String domain;
	private String[] extensions;
	private String name;
	private String[] selectors;
	
	public static final Site ArrestsDotOrg = new Site("http://",
													  "arrests.org/",
													  new String[]{"?page=1&results=56"}, //results in multiples of 14 up to 56
													  "Arrests.org", 
													  new String[]{".profile-card .title a",
																   ".info .section-content div, .section-content.charges .charge-title, .section-content.charges .charge-description"});
	
	private Site(String protocol, String domain, String[] extensions, String name, String[] selectors) {
		this.protocol = protocol;
		this.domain = domain;
		this.extensions = extensions;
		this.name = name;
		this.selectors = selectors;
	}
	
	public String getProtocol() {
		return protocol;
	}
	public String getDomain() {
		return domain;
	}
	public String[] getExtensions() {
		return extensions;
	}
	public String getName() {
		return name;
	}
	public String[] getSelectors() {
		return selectors;
	}
}
