package com.mcd.spider.main.entities.audit;

import java.util.Map;

/**
 * 
 * @author u569220
 *
 */

public class LinkResponse implements Comparable<LinkResponse>{

	private int code;
	private String url;
	private long loadTime;
	private Map<String, Integer> frequentWords;
	private SearchResults results;
	
	public LinkResponse(int code, String url) {
		this.code = code;
		this.url = url;
	}
	
	public LinkResponse(int code, String url, long loadTime) {
		this.code = code;
		this.url = url;
		this.loadTime = loadTime;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getLoadTime() {
		return loadTime;
	}
	public void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}
	@Override
	public int compareTo(LinkResponse lr) {
		// TODO compare code first, then url
		if (this.code == lr.code) {
			return this.url.compareToIgnoreCase(lr.url);
		} else if (this.code < lr.code) {
			return -1;
		} else {
			return 1;
		}
	}
}
