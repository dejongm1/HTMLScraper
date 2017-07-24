package com.mcd.spider.main.entities.audit;


/**
 * 
 * @author u569220
 *
 */

public class LinkResponse implements Comparable<LinkResponse>{

	private int code;
	private String url;
	
	public LinkResponse(int code, String url) {
		this.code = code;
		this.url = url;
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
