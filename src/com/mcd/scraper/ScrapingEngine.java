package com.mcd.scraper;

import com.mcd.scraper.entities.ArrestRecord;
import com.mcd.scraper.entities.Record;
import com.mcd.scraper.entities.State;
import com.mcd.scraper.entities.Term;
import com.mcd.scraper.entities.site.Site;
import com.mcd.scraper.util.ConnectionUtil;
import com.mcd.scraper.util.EmailUtil;
import com.mcd.scraper.util.ExcelWriter;
import com.mcd.scraper.util.Util;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {

	public static final Logger logger = Logger.getLogger(ScrapingEngine.class);
	
	Util util = new Util();

    protected void testRandomConnections(int numberOfTries) {
        long time = System.currentTimeMillis();
        int trie = 0;
        while (trie<numberOfTries) {
            Document doc = util.getHtmlAsDocTest("http://www.whoishostingthis.com/tools/user-agent/");
            if (docWasRetrieved(doc)) {
                Elements tags = doc.select("#user-agent .user-agent, #user-agent .ip");
                for (Element tag : tags) {
                    logger.debug(tag.text());
                }
            } else {
                logger.error("Failed to load html for testing connection");
            }
            trie++;
        }
        time = System.currentTimeMillis() - time;
        logger.info("Took " + time + " ms");
    }

	protected void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		long time = System.currentTimeMillis();
		Document doc = util.getHtmlAsDoc(url);
		if (docWasRetrieved(doc)) {
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
		} else {
			logger.error("Failed to load html doc from " + url);
		}
	}

	protected void getTextBySelector(String url, String selector) {
		long time = System.currentTimeMillis();
		Document doc = util.getHtmlAsDoc(url);
		if (docWasRetrieved(doc)) {
			Elements tags = doc.select(selector);
			for (Element tag : tags) {
				logger.info(tag.text());
			}
		} else {
			logger.error("Failed to load html doc from " + url);
		}
		time = System.currentTimeMillis() - time;
		logger.info("Took " + time + " ms");
	}
	
	protected void getArrestRecords(List<State> states, long maxNumberOfResults) {
		logger.debug("Sending spider " + (System.getProperty("offline").equals("true")?"offline":"online" ));
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
				ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord());
				excelWriter.createSpreadhseet();
				for(Site site : sites){
					int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
					sleepTimeSum += util.offline()?0:sleepTimeAverage;
					long time = System.currentTimeMillis();
					recordsProcessed += scrapeSite(state, site, excelWriter);
					sitesScraped++;
					time = System.currentTimeMillis() - time;
					logger.info(site.getBaseUrl(new String[]{state.getName()}) + " took " + time + " ms");
				}

				//remove ID column on final save? 
				//or use for future processing? check for ID and start where left off 
				stateTime = System.currentTimeMillis() - stateTime;
				logger.info(state.getName() + " took " + stateTime + " ms");
				
				try {
					EmailUtil.send("dejong.c.michael@gmail.com", 
							"Pack##92", //need to encrypt
							"dejong.c.michael@gmail.com", 
							"Arrest record parsing for " + state.getName(), 
							"Michael's a stud, he just successfully parsed the interwebs for arrest records in the state of Iowa");
				} catch (RuntimeException re) {
					logger.error("An error occurred, email not sent");
				}
			}
		//}
		int perRecordSleepTimeAverage = sitesScraped!=0?(sleepTimeSum/sitesScraped):0;
		totalTime = System.currentTimeMillis() - totalTime;
		logger.info(states.size() + " states took " + totalTime + " ms");
		if (!util.offline()) {
			logger.info("Sleep time was approximately " + (recordsProcessed*perRecordSleepTimeAverage) + " ms");
			logger.info("Processing time was approximately " + (totalTime-(recordsProcessed*perRecordSleepTimeAverage)) + " ms");
		} else {
			logger.info("Total time taken was " + totalTime + " ms");
		}
	}

	private int scrapeSite(State state, Site site, ExcelWriter excelWriter) {
		int recordsProcessed = 0;
		String baseUrl = site.getBaseUrl(new String[]{state.getName()});
		//Add some retries if first connection to state site fails?
		Document mainPageDoc = util.getHtmlAsDoc(baseUrl);
		if (docWasRetrieved(mainPageDoc)) {
			int numberOfPages = site.getPages(mainPageDoc);
			if (numberOfPages==0) {
				numberOfPages = 1;
			}
			for (int p=1; p<=numberOfPages;p++) {
				logger.debug("----Site: " + site.getName() + " - " + state.getName() + ": Page " + p);
				Document resultsPageDoc = util.getHtmlAsDoc(site.getResultsPageUrl(p));
				if (docWasRetrieved(resultsPageDoc)){
					recordsProcessed += scrapePage(resultsPageDoc, site, excelWriter);
				} else {
					//log something
				}
			}
		} else {
			logger.error("Failed to load html doc from " + baseUrl);
		}
		return recordsProcessed;
	}
	
	private int scrapePage(Document resultsPageDoc, Site site, ExcelWriter excelWriter) {
		//eventually output to spreadsheet
		int recordsProcessed = 0;
		List<Record> arrestRecords = new ArrayList<>();
		//ArrestRecord record = new ArrestRecord();
		Elements profileDetailTags = site.getRecordElements(resultsPageDoc);
		//WritableSheet sheet = excelWriter.getWorksheet(0);//just do one sheet per excel for now
		for (Element pdTag : profileDetailTags) {
			logger.debug(pdTag.text());
			Document profileDetailDoc = util.getHtmlAsDoc(site.getRecordDetailDocUrl(pdTag));
			if(docWasRetrieved(profileDetailDoc)){
				recordsProcessed++;
				//should we check for ID first or not bother unless we see duplicates??
                try {
                    arrestRecords.add(populateArrestRecord(profileDetailDoc, site));
                    int sleepTime = ConnectionUtil.getSleepTime(site);
                    logger.debug("Sleeping for: " + sleepTime);
					Thread.sleep(sleepTime);//sleep at random interval
				} catch (InterruptedException ie) {
					logger.error(ie);
				}
            } else {
    			logger.error("Failed to load html doc from " + pdTag);
    		}
		}
		excelWriter.saveRecordsToWorkbook(arrestRecords);
		//excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
		return recordsProcessed;
	}
	
	private ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site) {
		Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
		String baseURI = profileDetailDoc.baseUri();
		ArrestRecord record = new ArrestRecord();
		record.setId(profileDetails.get(0).baseUri().substring(baseURI.indexOf("/Arrests/")+9, baseURI.length()-1));
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
			//should I try to categorize charge types here???
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
	
	private boolean docWasRetrieved(Document doc) {
		return doc!=null && !doc.body().text().equals("");
	}
}
