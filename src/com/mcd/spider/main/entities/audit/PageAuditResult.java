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
	private String fullResponseCode;
	private String url;
	private long loadTime;
	private Map<String, Term> frequentWords;
	private SearchResults searchResults;
	private SortedSet<Link> inBoundLinks = new TreeSet<>();
	private SortedSet<Link> outBoundLinks = new TreeSet<>(); 
	
	public PageAuditResult(String url) {
		this.url = url;
		this.searchResults = new SearchResults(this);
	}
	
	public PageAuditResult(int code, String url, String fullResponseCode) {
		this.code = code;
		this.url = url;
		this.fullResponseCode = fullResponseCode;
		this.searchResults = new SearchResults(this);
	}

	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nPage: " + this.url);
		sb.append("\n\tStatus Code: " + this.code);
		sb.append("\n\tFull Response Code: " + this.fullResponseCode);
		sb.append("\n\tLoad Time: " + this.loadTime);
		sb.append("\n\tInbound Links: " + this.inBoundLinks.size());
		sb.append("\n\tOutbound Links: " + this.outBoundLinks.size());
		if (frequentWords!=null) {
            sb.append("\n\tFrequent words: ");
            for (Map.Entry<String,Term> entry : this.frequentWords.entrySet())  {
                sb.append("\n\t\t" + entry.getValue().getWord() + " is used " + entry.getValue().getCount() + " times");
            }
        }
		sb.append(this.searchResults.prettyPrint());
		return sb.toString();
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getFullResponseCode() {
		return fullResponseCode;
	}
	public void setFullResponseCode(String fullResponseCode) {
		this.fullResponseCode = fullResponseCode;
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
	public Map<String,Term> getFrequentWords() {
	    return this.frequentWords;
    }
    public void setFrequentWords(Map<String,Term> frequentWords) {
	    this.frequentWords = frequentWords;
    }
	public SearchResults getSearchResults() {
		return searchResults;
	}
	public void setSearchResults(SearchResults searchResults) {
		this.searchResults = searchResults;
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
