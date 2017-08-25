package com.mcd.spider.entities.audit;

public class Link implements Comparable<Link>{
	
	private String origin;
	private String url;
	
	public Link(String origin, String url) {
		this.origin = origin;
		this.url = url;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public int compareTo(Link l) {
		// compare origin first, then url
		if (this.origin.equalsIgnoreCase(l.origin)) {
			return this.url.compareToIgnoreCase(l.url);
		} else {
			return this.origin.compareToIgnoreCase(l.origin);
		}
	}
	
}
