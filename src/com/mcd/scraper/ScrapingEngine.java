package com.mcd.scraper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.scraper.entities.ArrestRecord;
import com.mcd.scraper.entities.State;
import com.mcd.scraper.entities.Term;
import com.mcd.scraper.entities.site.Site;
import com.mcd.scraper.util.ScraperUtil;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {

	private static final Logger logger = Logger.getLogger(ScrapingEngine.class);
	
	ScraperUtil util = new ScraperUtil();

	protected void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		long time = System.currentTimeMillis();
		Document doc = getHtmlAsDoc(url);
		if (doc!=null) {
			//give option to leave in numbers? 
			Map<String, Term> termCountMap = new CaseInsensitiveMap<String, Term>();
			String bodyText = doc.body().text();
			String[] termsInBody = bodyText.split("\\s+");
			for (String term : termsInBody) {
				term = term.replaceAll("[[^\\p{L}\\p{Nd}]+]", "");
				//instead of get, can this be a generous match?
				if (!term.equals("")) {
					Term termObj = termCountMap.get(term);
					if (termObj == null) {
						termObj = new Term(term, 1);
						termCountMap.put(term, termObj);
					} else {
						termObj.increment();
					}
				}
			}
			
			Map<String, Term> sortedWords = util.sortByValue(termCountMap);
			int i = 0;
			Iterator<Entry<String, Term>> iter = sortedWords.entrySet().iterator();
			while (iter.hasNext()) {
				Term term =  iter.next().getValue();
				if (i < numberOfWords && !term.getWord().equals("")) {
					logger.info(term.getCount() + "\t" + term.getWord());
					i++;
				}
			}
			time = System.currentTimeMillis() - time;
			logger.info("Took " + time + " ms");
			
		}
	}

	protected void getTextBySelector(String url, String selector) {
		long time = System.currentTimeMillis();
		Document doc = getHtmlAsDoc(url);
		if (doc!=null) {
			Elements tags = doc.select(selector);
			for (Element tag : tags) {
				logger.info(tag.text());
			}
		}
		time = System.currentTimeMillis() - time;
		logger.info("Took " + time + " ms");
	}
	
	protected void getArrestRecords(List<State> states, long maxNumberOfResults) {
		//split into more specific methods
		long totalTime = System.currentTimeMillis();
		long recordsProcessed = 0;
		int sleepTimeSum = 0;
		int sitesScraped = 0;
		//use maxNumberOfResults to stop processing once this method has been broken up
		//this currently won't stop a single site from processing more than the max number of records
		//while(recordsProcessed <= maxNumberOfResults) {
			for (State state : states){
				long stateTime = System.currentTimeMillis();
				logger.info("----State: " + state.getName() + "----");
				Site[] sites = state.getSites();
				for(Site site : sites){
					sitesScraped++;
					sleepTimeSum += util.offline()?0:site.getPerRecordSleepTime();
					long time = System.currentTimeMillis();
					recordsProcessed += scrapeSite(state, site);
					time = System.currentTimeMillis() - time;
					logger.info(site.getBaseUrl(new String[]{state.getName()}) + " took " + time + " ms");
				}
				stateTime = System.currentTimeMillis() - stateTime;
				logger.info(state.getName() + " took " + stateTime + " ms");
				
			}
		//}

		int perRecordSleepTimeAverage = sitesScraped!=0?(sleepTimeSum/sitesScraped):0;
		totalTime = System.currentTimeMillis() - totalTime;
		logger.info(states.size() + " states took " + totalTime + " ms");
		logger.info("Processing time was approximately " + (totalTime-(recordsProcessed*perRecordSleepTimeAverage)) + " ms");
	}

	private int scrapeSite(State state, Site site) {
		int recordsProcessed = 0;
		long perRecordSleepTime = util.offline()?0:site.getPerRecordSleepTime();
		String baseUrl = site.getBaseUrl(new String[]{state.getName()});
		//Add some retries if first connection to state site fails?
		Document mainPageDoc = getHtmlAsDoc(baseUrl);
		if (mainPageDoc!=null) {
			int numberOfPages = site.getPages(mainPageDoc);
			if (numberOfPages==0) {
				numberOfPages = 1;
			}
			for (int p=1; p<=numberOfPages;p++) {
				logger.debug("----Site: " + site.getName() + " - " + state.getName() + ": Page " + p);
				Document resultsPageDoc = getHtmlAsDoc(site.getResultsPageUrl(p, 14));
				recordsProcessed += scrapePage(resultsPageDoc, site, perRecordSleepTime);
			}
		}
		logger.info("Sleep time for site " + site.getName() + " was " + recordsProcessed*perRecordSleepTime + " ms");
		return recordsProcessed;
	}
	
	private int scrapePage(Document resultsPageDoc, Site site, long perRecordSleepTime) {
		//eventually output to spreadsheet
		int recordsProcessed = 0;
		List<ArrestRecord> arrestRecords = new ArrayList<>();
		Elements profileDetailTags = site.getRecordElements(resultsPageDoc);
		for (Element pdTag : profileDetailTags) {
			logger.info(pdTag.text());
			Document profileDetailDoc = getHtmlAsDoc(site.getRecordDetailDocUrl(pdTag));
			if(profileDetailDoc!=null){
				recordsProcessed++;
				arrestRecords.add(populateArrestRecord(profileDetailDoc, site));
				//addToSpreadsheet(arrestRecords);
				try {
					Thread.sleep(perRecordSleepTime);
				} catch (InterruptedException ie) {
					logger.error(ie.getMessage());
				}
			} else {
				logger.error("Couldn't load details for " + pdTag.text());
			}
		}
		return recordsProcessed;
	}
	
	private ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site) {
		Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
//			ArrestRecord record = new ArrestRecord(id, 
//					fullName, 
//					arrestDate, 
//					bond, 
//					arrestAge, 
//					gender, 
//					city, 
//					state, 
//					height, 
//					weight, 
//					hairColor, 
//					eyeColor, 
//					charges);
		for (Element profileDetail : profileDetails) {
			logger.info("\t" + profileDetail.text());
		}
		//return record;
		return new ArrestRecord(0, "", null, 0, 0, "", "", "", "", 0, "", "", new String[]{});
	}
	
	private Document getHtmlAsDoc(String url) {
		try {
			if (util.offline()) {
				return util.getOfflinePage(url);
			} else {
				return util.getConnection(url).get();
			}
		} catch (FileNotFoundException fne) {
			logger.error("I couldn't find an html file for " + url);
		} catch (ConnectException ce) {
			logger.error("I couldn't connect to " + url + ". Please be sure you're using a site that exists and are connected to the interweb.");
		} catch (IOException ioe) {
			logger.error("I tried to scrape that site but had some trouble. \n" + ioe.getMessage());
		}
		return null;
	}

}
