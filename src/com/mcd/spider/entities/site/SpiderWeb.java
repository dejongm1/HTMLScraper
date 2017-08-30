package com.mcd.spider.entities.site;

import com.mcd.spider.entities.record.Record;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SpiderWeb {
	
	private int attemptCount;
    private Set<String> crawledIds;
    private Set<String> uncrawledIds;
    private Set<Record> crawledRecords;
    private Map<String,String> sessionCookies;
    private Map<String,String> headers;
    private boolean offline;
    private int furthestPageToCheck;
    private long recordsProcessed;
    private long maxNumberOfResults;
    private int numberOfPages;
    private boolean addMisc;
    private boolean retrieveMissedRecords;
    private int recordCap;
    
    public SpiderWeb(long maxNumberOfResults, boolean addMisc, boolean retrieveMissedRecords) {
    	attemptCount = 1;
    	this.maxNumberOfResults = maxNumberOfResults;
	    offline = System.getProperty("offline").equals("true");
	    furthestPageToCheck = 9999;
	    this.addMisc = addMisc;
	    this.retrieveMissedRecords = retrieveMissedRecords;
	    recordCap = ThreadLocalRandom.current().nextInt(150, 250);
    }
 
	public int getAttemptCount() {
		return attemptCount;
	}

	public void increaseAttemptCount() {
		attemptCount++;
	}

	public Set<String> getCrawledIds() {
		return crawledIds;
	}

	public void setCrawledIds(Set<String> set) {
		this.crawledIds = set;
	}

    public Set<String> getUncrawledIds() {
        return uncrawledIds;
    }

    public void setUncrawledIds(Set<String> set) {
        this.uncrawledIds = set;
    }

	public Set<Record> getCrawledRecords() {
		return crawledRecords;
	}

	public void setCrawledRecords(Set<Record> crawledRecords) {
		this.crawledRecords = crawledRecords;
	}

	public Map<String, String> getSessionCookies() {
		return sessionCookies;
	}

	public void setSessionCookies(Map<String, String> sessionCookies) {
		this.sessionCookies = sessionCookies;
	}

	public void addSessionCookie(String key, String value) {
		this.sessionCookies.put(key, value);
	}

	public int getFurthestPageToCheck() {
		return furthestPageToCheck;
	}

	public void setFurthestPageToCheck(int furthestPageToCheck) {
		this.furthestPageToCheck = furthestPageToCheck;
	}

	public long getRecordsProcessed() {
		return recordsProcessed;
	}

	public void addToRecordsProcessed(long recordsProcessed) {
		this.recordsProcessed += recordsProcessed;
	}

	public long getMaxNumberOfResults() {
		return maxNumberOfResults;
	}

	public void setMaxNumberOfResults(long maxNumberOfResults) {
		this.maxNumberOfResults = maxNumberOfResults;
	}

	public boolean isOffline() {
		return offline;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public boolean getMisc() {
		return addMisc;
	}

	public boolean retrieveMissedRecords() {
        return retrieveMissedRecords;
    }

    public int getRecordCap() {
        return recordCap;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String,String> headers) {
        this.headers = headers;
    }
}
