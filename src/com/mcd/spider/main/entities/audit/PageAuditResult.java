package com.mcd.spider.main.entities.audit;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author u569220
 *
 */

public class PageAuditResult implements Comparable<PageAuditResult>{

	private int code;
	private String url;
	private long loadTime;
	private Map<String, Integer> frequentWords;
	private SearchResults results;
	private SortedSet<Link> inBoundLinks = new TreeSet<>();
	private SortedSet<Link> outBoundLinks = new TreeSet<>(); 
	
	public PageAuditResult(String url) {
		this.url = url;
	}
	
	public PageAuditResult(int code, String url) {
		this.code = code;
		this.url = url;
	}

	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nPage: " + this.url);
		sb.append("\n\tStatus Code: " + this.code);
		sb.append("\n\tLoad Time: " + this.loadTime);
		sb.append("\n\tInbound Links: " + this.inBoundLinks.size());
		sb.append("\n\tOutbound Links: " + this.outBoundLinks.size());
		return sb.toString();
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
	public Set<Link> getInBoundLinks() {
		return inBoundLinks;
	}
	public void addInBoundLink(String toUrl) {
		this.inBoundLinks.add(new Link(this.url, toUrl));	
	}
	public Set<Link> getOutBoundLinks() {
		return outBoundLinks;
	}
	public void addOutBoundLink(String toUrl) {
		this.outBoundLinks.add(new Link(this.url, toUrl));	
	}
	@Override
	public int compareTo(PageAuditResult lr) {
		// compare code first, then url
		if (this.code == lr.code) {
			return this.url.compareToIgnoreCase(lr.url);
		} else if (this.code < lr.code) {
			return -1;
		} else {
			return 1;
		}
	}
}
