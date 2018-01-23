package com.mcd.spider.entities.site;

public interface Site {
	
	String getBaseUrl();
	void setBaseUrl(String[] arg);
	String getName();
	Url getUrl();
	int getMaxAttempts();
	String obtainRecordId(String url);
	int[] getPerRecordSleepRange();
	String obtainDetailUrl(String id);
}
