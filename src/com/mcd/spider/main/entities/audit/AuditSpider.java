package com.mcd.spider.main.entities.audit;

import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.SpiderUtil;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AuditSpider {
	
	private URL baseUrl;
	private Connection.Response rootResponse;
	private Document rootDocument;
	private long inBoundLinksCount;
	private Set<String> inBoundLinks;
	private long outBoundLinksCount;
	private Set<String> outBoundLinks;
	private LinkResponses linkResponses;
	private File robotsTxt;
	private long averagePageLoadTime;
	
	public AuditSpider(String baseUrl, boolean offline) throws IOException {
	    if (baseUrl.endsWith("/")) {
	        baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        }
		this.baseUrl = new URL(baseUrl);
		if (offline) {
			this.rootResponse = null;
			this.rootDocument = SpiderUtil.getOfflinePage(baseUrl);
		} else {
			this.rootResponse = ConnectionUtil.getConnection(this.baseUrl.toString(), "").execute();
			this.rootDocument = this.rootResponse.parse();
		}
		this.linkResponses =  new LinkResponses();
		this.inBoundLinks = new HashSet<>();
		this.outBoundLinks = new HashSet<>();
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	public Connection.Response getRootResponse() {
		return rootResponse;
	}

	public Document getRootDocument() {
		return rootDocument;
	}

	public long getInBoundLinksCount() {
		return inBoundLinksCount;
	}

	public Set<String> getInBoundLinks() {
		return inBoundLinks;
	}

	public void addInBoundLink(String link) {
		this.inBoundLinks.add(link);
		this.inBoundLinksCount++;
	}
	
	public long getOutBoundLinksCount() {
		return outBoundLinksCount;
	}

	public Set<String> getOutBoundLinks() {
		return outBoundLinks;
	}

	public void addOutBoundLink(String link) {
		this.outBoundLinks.add(link);
		this.outBoundLinksCount++;
	}
	
	public LinkResponses getLinkResponses() {
		return linkResponses;
	}

	public void setLinkResponses(LinkResponses linkResponses) {
		this.linkResponses = linkResponses;
	}

	public File getRobotsTxt() {
		return robotsTxt;
	}

	public void setRobotsTxt(File robotsTxt) {
		this.robotsTxt = robotsTxt;
	}

	public long getAveragePageLoadTime() {
		return averagePageLoadTime;
	}

	public void setAveragePageLoadTime(long averagePageLoadTime) {
		this.averagePageLoadTime = averagePageLoadTime;
	}
}
