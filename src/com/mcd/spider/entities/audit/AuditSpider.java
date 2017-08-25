package com.mcd.spider.entities.audit;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AuditSpider {
	
	private URL baseUrl;
	private Connection.Response rootResponse;
	private Document rootDocument;
	private AuditResults auditResults;
	private File robotsTxt;
	private long averagePageLoadTime; //this only measures html loading for now
	private long sleepTime;
	private List<Term> termsToSearch;
	
	public AuditSpider(String baseUrl, boolean offline) throws IOException {
		this.baseUrl = new URL(baseUrl);
		if (offline) {
			//this.rootResponse = null;
			//this.rootDocument = SpiderUtil.getOfflinePage(baseUrl);
		} else {
			//this.rootResponse = ConnectionUtil.getConnection(this.baseUrl.toString(), "").execute();
			//this.rootDocument = this.rootResponse.parse();
		}
		this.auditResults =  new AuditResults();
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

	public AuditResults getAuditResults() {
		return auditResults;
	}

	public void setAuditResults(AuditResults auditResults) {
		this.auditResults = auditResults;
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

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public List<Term> getTermsToSearch() {
		return termsToSearch;
	}

	public void setTermsToSearch(List<Term> termsToSearch) {
		this.termsToSearch = termsToSearch;
	}
}
