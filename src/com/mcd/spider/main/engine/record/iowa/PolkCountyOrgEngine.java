package com.mcd.spider.main.engine.record.iowa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.ArrestRecordFilter;
import com.mcd.spider.main.entities.site.PolkCountyIowaGovSite;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.ExcelWriter;
import com.mcd.spider.main.util.SpiderUtil;

import common.Logger;

/**
 * 
 * @author u569220
 *
 */
public class PolkCountyOrgEngine implements ArrestRecordEngine {
	
	private static final Logger logger = Logger.getLogger(PolkCountyOrgEngine.class);
	private SpiderUtil spiderUtil = new SpiderUtil();
	private EngineUtil engineUtil = new EngineUtil();

    public Site getSite(String[] args) {
    	return new PolkCountyIowaGovSite(args);
    }
    
	@Override
    public void getArrestRecords(State state, long maxNumberOfResults, ArrestRecordFilter.ArrestRecordFilterEnum filter) {
        //split into more specific methods
        long totalTime = System.currentTimeMillis();
        long recordsProcessed = 0;
        int sleepTimeSum = 0;
        int sitesScraped = 0;

        //use maxNumberOfResults to stop processing once this method has been broken up
        //this currently won't stop a single site from processing more than the max number of records
        //while(recordsProcessed <= maxNumberOfResults) {

        long stateTime = System.currentTimeMillis();
        logger.info("----State: " + state.getName() + "----");
        logger.debug("Sending spider " + (System.getProperty("offline").equals("true")?"offline":"online" ));
        //Site[] sites = state.getSites();
//        for(Site site : sites){
        PolkCountyIowaGovSite site = new PolkCountyIowaGovSite(new String[]{state.getName()});
        ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord(), site);
        excelWriter.createSpreadsheet();
        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
        sleepTimeSum += spiderUtil.offline()?0:sleepTimeAverage;
        long time = System.currentTimeMillis();
        recordsProcessed += scrapeSite(state, site, excelWriter, 1);
        sitesScraped++;
        time = System.currentTimeMillis() - time;
        logger.info(site.getBaseUrl() + " took " + time + " ms");
//        }

        //remove ID column on final save?
        //or use for future processing? check for ID and start where left off
        //excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
        stateTime = System.currentTimeMillis() - stateTime;
        logger.info(state.getName() + " took " + stateTime + " ms");

        //extract to Util class
//        try {
//            EmailUtil.send("dejong.c.michael@gmail.com",
//                    "Pack##92", //need to encrypt
//                    "dejong.c.michael@gmail.com",
//                    "Arrest record parsing for " + state.getName(),
//                    "Michael's a stud, he just successfully parsed the interwebs for arrest records in the state of Iowa");
//        } catch (RuntimeException re) {
//            logger.error("An error occurred, email not sent");
//        }
        //}
        int perRecordSleepTimeAverage = sitesScraped!=0?(sleepTimeSum/sitesScraped):0;
        totalTime = System.currentTimeMillis() - totalTime;
        if (!spiderUtil.offline()) {
            logger.info("Sleep time was approximately " + (recordsProcessed*perRecordSleepTimeAverage) + " ms");
            logger.info("Processing time was approximately " + (totalTime-(recordsProcessed*perRecordSleepTimeAverage)) + " ms");
        } else {
            logger.info("Total time taken was " + totalTime + " ms");
        }
    }

	@Override
	public int scrapeSite(State state, Site site, ExcelWriter excelWriter, int attemptCount) {
        //refactor to split out randomizing functionality, maybe reuse??
        int recordsProcessed = 0;
        String firstPageResults = site.getBaseUrl();
        //Add some retries if first connection to state site fails?
        Document mainPageDoc = spiderUtil.getHtmlAsDoc(firstPageResults);
        if (engineUtil.docWasRetrieved(mainPageDoc)) {
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
            Map<String,Object> resultsDocPlusMiscMap = new HashMap<>();
            List<String> keys = new ArrayList<>(resultsUrlPlusMiscMap.keySet());
            Collections.shuffle(keys);
            for (String k : keys) {
                resultsDocPlusMiscMap.put(String.valueOf(k), spiderUtil.getHtmlAsDoc(resultsUrlPlusMiscMap.get(k)));
                long sleepTime = ConnectionUtil.getSleepTime(site);
                spiderUtil.sleep(sleepTime, false);
                logger.debug("Sleeping for " + sleepTime + " after fetching " + resultsUrlPlusMiscMap.get(k));
                
            }

            //saving this for later?? should be able to get previous sorting by looking at page number in baseUri
            ((PolkCountyIowaGovSite) site).setOnlyResultsPageDocuments(resultsDocPlusMiscMap);

            //build a list of details page urls by parsing only results page docs in order
            Map<String,Object> resultsPageDocsMap = site.getResultsPageDocuments();
            Map<String,String> recordDetailUrlMap = new HashMap<>();
            for (Entry<String, Object> entry : resultsPageDocsMap.entrySet()) {
                Document doc = (Document) entry.getValue();
                //only proceed if document was retrieved
                if (engineUtil.docWasRetrieved(doc)){
                    logger.debug("Gather complete list of records to scrape from " + doc.baseUri());
                    recordDetailUrlMap.putAll(parseDocForUrls(doc, site));
                    //including some non-detail page links then randomize
                    recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, 20)); //TODO change from a static value

                    //recordsProcessed += scrapePage(doc, site, excelWriter);
                } else {
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
            logger.error("Failed to load html doc from " + site.getBaseUrl());
        }
        return recordsProcessed;
    }

	@Override
    public Map<String,String> parseDocForUrls(Object doc, Site site) {
        Map<String,String> recordDetailUrlMap = new HashMap<>();
        Elements recordDetailElements = site.getRecordElements((Document)doc);
        for(int e=0;e<recordDetailElements.size();e++) {
            String url = site.getRecordDetailDocUrl(recordDetailElements.get(e));
            String id = site.getRecordId(url);
            //String id = url.substring(url.indexOf("/Arrests/")+9, url.length()-1);
            recordDetailUrlMap.put(id, url);
        }
        return recordDetailUrlMap;
    }

	@Override
	public int scrapeRecords(Map<String,String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter) {
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
                if (engineUtil.docWasRetrieved(profileDetailDoc)) {
                    recordsProcessed++;
                    //should we check for ID first or not bother unless we see duplicates??
                	arrestRecord = populateArrestRecord(profileDetailDoc, site);
                    arrestRecords.add(arrestRecord);
                    //save each record in case of failures
                    excelWriter.addRecordToMainWorkbook(arrestRecord);
                    spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
                } else {
                    logger.error("Failed to load html doc from " + url);
                }
            }
        }
        //save the whole thing at the end
        //order and save the overwrite the spreadsheet
        excelWriter.saveRecordsToMainWorkbook(arrestRecords);
        return recordsProcessed;
    }

	@Override
	public ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site) {
        Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.getRecordId(profileDetailDoc.baseUri()));
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.info("\t" + profileDetail.text());
        }
        return record;
    }

	@Override
	public void matchPropertyToField(ArrestRecord record, Element profileDetail) {
		String label = profileDetail.select("th").text().toLowerCase();
		Elements charges = profileDetail.select("table.inmateCharges tr");
		if (!charges.isEmpty()) {
			int bond = 0;
			//should I try to categorize charge types here???
			String[] chargeStrings = new String[charges.size()-1]; 
			//extract bond  and charges from here
			for (int c=1;c<charges.size();c++) {
				Element charge = charges.get(c);
				Elements chargeDetails = charge.select("td");
				chargeStrings[c-1] = (!chargeDetails.get(0).text().equals("")?"Case: " + chargeDetails.get(0).text():"") + " " + chargeDetails.get(1).text();
				bond += Long.parseLong(chargeDetails.get(2).text().replace("$", "").replace(",", "").trim());
			}
			record.setCharges(chargeStrings);
			record.setTotalBond(bond);
		} else if (!label.equals("")) {
			try {
				if (label.equals("name")) {
					formatName(record, profileDetail);
				} else if (label.contains("book date")) {
					formatArrestTime(record, profileDetail);
				} else if (label.contains("age")) {
					record.setArrestAge(Integer.parseInt(extractValue(profileDetail)));
				} else if (label.contains("offender")) {
					record.setOffenderId(extractValue(profileDetail));
				} else if (label.contains("sex")) {
					record.setGender(extractValue(profileDetail));
				} else if (label.contains("city")) {
//					record.setCity(city);
//					record.setState(state);
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
	
	@Override
	public void formatName(ArrestRecord record, Element profileDetail) {
		String fullName = extractValue(profileDetail);
		record.setFullName(fullName);
	}
	
	@Override
	public String extractValue(Element profileDetail) {
		return profileDetail.select("td").text().trim();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void formatArrestTime(ArrestRecord record, Element profileDetail) {
		Date date = new Date(extractValue(profileDetail));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		record.setArrestDate(calendar);
	}

	@Override
	public ExcelWriter initializeOutputter(State state, Site site) throws SpiderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Record> filterRecords(List<ArrestRecord> fullArrestRecords) {
		return null;
	}
}
