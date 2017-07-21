package com.main.mcd.engine;

import com.main.mcd.spider.entities.ArrestRecord;
import com.main.mcd.spider.entities.Record;
import com.main.mcd.spider.entities.State;
import com.main.mcd.spider.entities.Term;
import com.main.mcd.spider.entities.site.Site;
import com.main.mcd.spider.util.ConnectionUtil;
import com.main.mcd.spider.util.EmailUtil;
import com.main.mcd.spider.util.ExcelWriter;
import com.main.mcd.spider.util.SpiderUtil;
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
public class SpiderEngine {

	public static final Logger logger = Logger.getLogger(SpiderEngine.class);
	
	SpiderUtil spiderUtil = new SpiderUtil();

    public void testRandomConnections(int numberOfTries) {
        long time = System.currentTimeMillis();
        int trie = 0;
        while (trie<numberOfTries) {
            Document doc = spiderUtil.getHtmlAsDocTest("http://www.whoishostingthis.com/tools/user-agent/");
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

	public void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
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
			
			Map<String, Term> sortedWords = spiderUtil.sortByValue(termCountMap);
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

	public void getTextBySelector(String url, String selector) {
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
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

    public void getArrestRecords(List<State> states, long maxNumberOfResults) {
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
                sleepTimeSum += spiderUtil.offline()?0:sleepTimeAverage;
                long time = System.currentTimeMillis();
                recordsProcessed += scrapeSite(state, site, excelWriter);
                sitesScraped++;
                time = System.currentTimeMillis() - time;
                logger.info(site.getBaseUrl(new String[]{state.getName()}) + " took " + time + " ms");
            }

            //remove ID column on final save?
            //or use for future processing? check for ID and start where left off
            //excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
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
        if (!spiderUtil.offline()) {
            logger.info("Sleep time was approximately " + (recordsProcessed*perRecordSleepTimeAverage) + " ms");
            logger.info("Processing time was approximately " + (totalTime-(recordsProcessed*perRecordSleepTimeAverage)) + " ms");
        } else {
            logger.info("Total time taken was " + totalTime + " ms");
        }
    }

    private int scrapeSite(State state, Site site, ExcelWriter excelWriter) {
        //refactor to split out randomizing functionality, maybe reuse??
        int recordsProcessed = 0;
        String firstPageResults = site.getBaseUrl(null);
        //Add some retries if first connection to state site fails?
        Document mainPageDoc = spiderUtil.getHtmlAsDoc(firstPageResults);
        if (docWasRetrieved(mainPageDoc)) {
            int numberOfPages = site.getTotalPages(mainPageDoc);
            if (numberOfPages==0) {
                numberOfPages = 1;
            }
            Map<String, String> resultsUrlPlusMiscMap = new HashMap<>();
            logger.debug("Generating list of results pages for : " + site.getName() + " - " + state.getName());
            //also get misc urls
            //Map<String,String> miscUrls = site.getMiscSafeUrlsFromDoc(mainPageDoc, numberOfPages);
            for (int p=1; p<=numberOfPages;p++) {
                resultsUrlPlusMiscMap.put(String.valueOf(p), site.generateResultsPageUrl(p));
            }

            //resultsUrlPlusMiscMap.putAll(miscUrls);

            //shuffle urls before retrieving docs
            Map<String,Document> resultsDocPlusMiscMap = new HashMap<>();
            List<String> keys = new ArrayList<>(resultsUrlPlusMiscMap.keySet());
            Collections.shuffle(keys);
            for (String k : keys) {
                resultsDocPlusMiscMap.put(String.valueOf(k), spiderUtil.getHtmlAsDoc(resultsUrlPlusMiscMap.get(k)));
                try {
                    int sleepTime = ConnectionUtil.getSleepTime(site);
                    Thread.sleep(sleepTime);
                    logger.debug("Sleeping for " + sleepTime + " after fetching " + resultsUrlPlusMiscMap.get(k));
                } catch (InterruptedException e) {
                    logger.error("Failed to sleep after fetching " + resultsUrlPlusMiscMap.get(k), e);
                }
            }

            //saving this for later?? should be able to get previous sorting by looking at page number in baseUri
            site.setOnlyResultsPageDocuments(resultsDocPlusMiscMap);

            //build a list of details page urls by parsing only results page docs in order
            Map<String,Document> resultsPageDocsMap = site.getResultsPageDocuments();
            Map<String,String> recordDetailUrlMap = new HashMap<>();
            for (Entry<String, Document> entry : resultsPageDocsMap.entrySet()) {
                Document doc = entry.getValue();
                //only proceed if document was retrieved
                if (docWasRetrieved(doc)){
                    logger.debug("Gather complete list of records to scrape from " + doc.baseUri());
                    recordDetailUrlMap.putAll(parseDocForUrls(doc, site));
                    //including some non-detail page links then randomize
                    recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, 20)); //TODO change from a static value

                    //recordsProcessed += scrapePage(doc, site, excelWriter);
                } else {
                    //log something
                    logger.info("Nothing was retrieved for " + doc.baseUri());
                }
            }

            int recordsGathered = recordDetailUrlMap.size();
            logger.debug("Gathered links for " + recordsGathered + " record profiles and misc");

            //****TODO
            //****use sorted map to check for already scraped records - should I used ID as map.key instead of a sequence?

            //****iterate over collection, scraping records and simply opening others
            recordsProcessed += scrapeRecords(recordDetailUrlMap, site, excelWriter);

            //****sort by arrest date (or something else) once everything has been gathered? can I sort spreadsheet after creation?

        } else {
            logger.error("Failed to load html doc from " + site.getBaseUrl(new String[]{state.getName()}));
        }
        return recordsProcessed;
    }

    private Map<String,String> parseDocForUrls(Document doc, Site site) {
        Map<String,String> recordDetailUrlMap = new HashMap<>();
        Elements recordDetailElements = site.getRecordElements(doc);
        for(int e=0;e<recordDetailElements.size();e++) {
            String url = site.getRecordDetailDocUrl(recordDetailElements.get(e));
            String id = site.getRecordId(url);
            //String id = url.substring(url.indexOf("/Arrests/")+9, url.length()-1);
            recordDetailUrlMap.put(id, url);
        }
        return recordDetailUrlMap;
    }

    private int scrapeRecords(Map<String,String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter) {
        int recordsProcessed = 0;
        List<Record> arrestRecords = new ArrayList<>();
        Record arrestRecord = new ArrestRecord();
//        List<String> keys = new ArrayList<>(recordsDetailsUrlMap.keySet());
//        Collections.shuffle(keys);
//        for (String k : keys) {
        for (Entry<String,String> entry : recordsDetailsUrlMap.entrySet()) {
            String id = entry.getKey();
            String url = recordsDetailsUrlMap.get(id);
            Document profileDetailDoc = spiderUtil.getHtmlAsDoc(url);
            if (site.isARecordDetailDoc(profileDetailDoc)) {
                if (docWasRetrieved(profileDetailDoc)) {
                    recordsProcessed++;
                    //should we check for ID first or not bother unless we see duplicates??
                    try {
                        arrestRecords.add(populateArrestRecord(profileDetailDoc, site));
                        //save each record in case of failures
                        //excelWriter.saveRecordsToWorkbook(arrestRecord);
                        int sleepTime = ConnectionUtil.getSleepTime(site);
                        logger.debug("Sleeping for: " + sleepTime);
                        Thread.sleep(sleepTime);//sleep at random interval
                    } catch (InterruptedException ie) {
                        logger.error(ie);
                    }
                } else {
                    logger.error("Failed to load html doc from " + url);
                }
            }
        }
        //save the whole thing at the end
        //order and save the overwrite the spreadsheet
        excelWriter.saveRecordsToWorkbook(arrestRecords);
        return recordsProcessed;
    }

    private ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site) {
        Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.getRecordId(profileDetailDoc.baseUri()));
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.info("\t" + profileDetail.text());
        }
        return record;
    }



	@SuppressWarnings("deprecation")
	private void matchPropertyToField(ArrestRecord record, Element profileDetail) {
		String label = profileDetail.select("th").text().toLowerCase();
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
				if (label.equals("name")) {
					formatFullName(record, profileDetail);
				} else if (label.contains("book date")) {
					Date date = new Date(extractValue(profileDetail));
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					record.setArrestDate(calendar);
				} else if (label.contains("age")) {
					record.setArrestAge(Integer.parseInt(extractValue(profileDetail)));
				} else if (label.contains("offender")) {
					record.setOffenderId(extractValue(profileDetail));
				} else if (label.contains("sex")) {
					record.setGender(extractValue(profileDetail));
				} else if (label.contains("city")) {
//					record.setCity(city);
//					record.setState(state);
				} else if (label.contains("bond")) {
					String bondAmount = extractValue(profileDetail);
					int totalBond = Integer.parseInt(bondAmount.replace("$", "").replace(",", ""));
					record.setTotalBond(totalBond);
				} else if (label.contains("height")) {
					record.setHeight(extractValue(profileDetail));
				} else if (label.contains("weight")) {
					record.setWeight(extractValue(profileDetail));
				} else if (label.contains("hair")) {
					record.setHairColor(extractValue(profileDetail));
				} else if (label.contains("eyes")) {
					record.setEyeColor(extractValue(profileDetail));
				} else if (label.contains("race")) {
					record.setRace(extractValue(profileDetail));
				}
			} catch (NumberFormatException nfe) {
				logger.error("Couldn't parse a numeric value from " + profileDetail.text());
			}
		} else if (profileDetail.select("h3").hasText()) {
			record.setCounty(profileDetail.select("h3").text().replaceAll("(?i)county", "").trim());
		}
	}
	
	private void formatFullName(ArrestRecord record, Element profileDetail) {
		String fullName = extractValue(profileDetail);
		record.setFullName(fullName);
	}
	
	private String extractValue(Element profileDetail) {
		return profileDetail.select("td").text().trim();
	}
	
	private boolean docWasRetrieved(Document doc) {
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
