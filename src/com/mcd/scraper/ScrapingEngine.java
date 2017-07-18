package com.mcd.scraper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

	public static final Logger logger = Logger.getLogger(ScrapingEngine.class);
	
	ScraperUtil util = new ScraperUtil();

	protected void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		long time = System.currentTimeMillis();
		Document doc = util.getHtmlAsDoc(url);
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
		Document doc = util.getHtmlAsDoc(url);
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
		if (!util.offline()) {
			logger.info("Processing time was " + (totalTime-(recordsProcessed*perRecordSleepTimeAverage)) + " ms");
		} else {
			logger.info("Processing time was " + totalTime + " ms");
		}
		
	}

	private int scrapeSite(State state, Site site) {
		int recordsProcessed = 0;
		long perRecordSleepTime = util.offline()?0:site.getPerRecordSleepTime();
		String baseUrl = site.getBaseUrl(new String[]{state.getName()});
		//Add some retries if first connection to state site fails?
		Document mainPageDoc = util.getHtmlAsDoc(baseUrl);
		if (mainPageDoc!=null) {
			int numberOfPages = site.getPages(mainPageDoc);
			if (numberOfPages==0) {
				numberOfPages = 1;
			}
			for (int p=1; p<=numberOfPages;p++) {
				logger.debug("----Site: " + site.getName() + " - " + state.getName() + ": Page " + p);
				Document resultsPageDoc = util.getHtmlAsDoc(site.getResultsPageUrl(p, 14));
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
			Document profileDetailDoc = util.getHtmlAsDoc(site.getRecordDetailDocUrl(pdTag));
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
		ArrestRecord record = new ArrestRecord();
		record.setId(profileDetails.get(0).baseUri().substring(profileDetails.get(0).baseUri().lastIndexOf('/')+1));
		for (Element profileDetail : profileDetails) {
			matchPropertyToField(record, profileDetail);
			logger.info("\t" + profileDetail.text());
		}
		return record;
	}
	
	@SuppressWarnings("deprecation")
	private void matchPropertyToField(ArrestRecord record, Element profileDetail) {
		String label = profileDetail.select("b").text().toLowerCase();
		Elements charges = profileDetail.select(".charges li");
		if (!charges.isEmpty()) {
			//should I try to categorize charge types???
			String[] chargeStrings = new String[charges.size()];
			for (int c = 0; c < charges.size(); c++) {
				chargeStrings[c] = charges.get(c).text();
			}
			record.setCharges(chargeStrings);
		} else if (!label.equals("")) {
			try {
				if (label.contains("full name")) {
					formatFullName(record, profileDetail);
				} else if (label.contains("date")) {
					Date date = new Date(stripOffLabel(profileDetail));
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					record.setArrestDate(calendar);
				} else if (label.contains("time")) {
					formatArrestTime(record, profileDetail);
				} else if (label.contains("arrest age")) {
					record.setArrestAge(Integer.parseInt(stripOffLabel(profileDetail)));
				} else if (label.contains("gender")) {
					record.setGender(stripOffLabel(profileDetail));
				} else if (label.contains("city")) {
					String city = profileDetail.select("span[itemProp=\"addressLocality\"]").text();
					String state = profileDetail.select("span[itemprop=\"addressRegion\"]").text();
					record.setCity(city);
					record.setState(state);
				} else if (label.contains("total bond")) {
					String bondAmount = stripOffLabel(profileDetail);
					int totalBond = Integer.parseInt(bondAmount.replace("$", ""));
					record.setTotalBond(totalBond);
				} else if (label.contains("height")) {
					record.setHeight(stripOffLabel(profileDetail));
				} else if (label.contains("weight")) {
					record.setWeight(stripOffLabel(profileDetail));
				} else if (label.contains("hair color")) {
					record.setHairColor(stripOffLabel(profileDetail));
				} else if (label.contains("eye color")) {
					record.setEyeColor(stripOffLabel(profileDetail));
				} else if (label.contains("birth")) {
					record.setBirthPlace(stripOffLabel(profileDetail));
				}
			} catch (NumberFormatException nfe) {
				logger.error("Couldn't parse a numeric value from " + profileDetail.text());
			}
		} else if (profileDetail.select("h3").hasText()) {
			record.setCounty(profileDetail.select("h3").text().replaceAll("(?i)county", "").trim());
		}
	}
	
	private void formatFullName(ArrestRecord record, Element profileDetail) {
		record.setFirstName(profileDetail.select("span [itemprop=\"givenName\"]").text());
		record.setMiddleName(profileDetail.select("span [itemprop=\"additionalName\"]").text());
		record.setLastName(profileDetail.select("span [itemprop=\"familyName\"]").text());
		String fullName = record.getFirstName();
		fullName += record.getMiddleName()!=null?" " + record.getMiddleName():"";
		fullName += " " + record.getLastName();
		record.setFullName(fullName);
	}
	
	private void formatArrestTime(ArrestRecord record, Element profileDetail) {
		Calendar arrestDate = record.getArrestDate();
		if (arrestDate!=null) {
			String arrestTimeText = profileDetail.text().replaceAll("(?i)time:", "").trim();
			arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
			arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
			arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
			record.setArrestDate(arrestDate);
		}
	}
	
	private String stripOffLabel(Element profileDetail) {
		return profileDetail.text().substring(profileDetail.text().indexOf(':')+1).trim();
	}
}
