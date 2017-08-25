package com.mcd.spider.entities.site;

public interface Site {
	
	String getBaseUrl();
	void setBaseUrl(String[] arg);
	String getName();
	Url getUrl();
	int getMaxAttempts();
	String generateRecordId(String url);
	int[] getPerRecordSleepRange();
}
