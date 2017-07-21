package com.mcd.spider.main.util;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;

public class EngineUtil {
	
	private static final Logger logger = Logger.getLogger(EngineUtil.class);

	public boolean docWasRetrieved(Document doc) {
		if (doc!=null) {
			if (doc.body().text().equals("")) {
				logger.error("You might be blocked. This doc retrieved was empty.");
			} else {
				return true;
			}
		} else {
			logger.error("No document was retrieved");
			return false;
		}
		return false;
	}
	
	
}
