package com.mcd.spider.main.engine.record.various;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.audit.OfflineResponse;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.ArrestsDotOrgSite;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.*;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Michael De Jong
 *
 */

public class ArrestsDotOrgEngine implements ArrestRecordEngine {

    public static final Logger logger = Logger.getLogger(ArrestsDotOrgEngine.class);
    

    SpiderUtil spiderUtil = new SpiderUtil();
    EngineUtil engineUtil = new EngineUtil();
    private Set<String> scrapedIds;

    @Override
    public Site getSite(String[] args) {
    	return new ArrestsDotOrgSite(args);
    }
    
    @Override
    public void getArrestRecords(State state, long maxNumberOfResults) throws SpiderException {
        long totalTime = System.currentTimeMillis();
        long recordsProcessed = 0;
        int sleepTimeSum = 0;

        //while(recordsProcessed <= maxNumberOfResults) {
        ArrestsDotOrgSite site = (ArrestsDotOrgSite) getSite(new String[]{state.getName()});
        ExcelWriter excelWriter = initializeOutputter(state, site);
        
        long siteTime = System.currentTimeMillis();
        logger.info("----Site: " + site.getName() + "-" + state.getName() + "----");
        logger.debug("Sending spider " + (System.getProperty("offline").equals("true")?"offline":"online" ));
        
        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
        sleepTimeSum += spiderUtil.offline()?0:sleepTimeAverage;
        long time = System.currentTimeMillis();
        
        recordsProcessed += scrapeSite(state, site, excelWriter);
        
        time = System.currentTimeMillis() - time;
        logger.info(site.getBaseUrl() + " took " + time + " ms");

        //excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
        siteTime = System.currentTimeMillis() - siteTime;
        logger.info(state.getName() + " took " + siteTime + " ms");

        spiderUtil.sendEmail(state);
        
        //}
        totalTime = System.currentTimeMillis() - totalTime;
        if (!spiderUtil.offline()) {
            logger.info("Sleep time was approximately " + sleepTimeSum + " ms");
            logger.info("Processing time was approximately " + (totalTime-sleepTimeSum) + " ms");
        } else {
            logger.info("Total time taken was " + totalTime + " ms");
        }
        logger.info(recordsProcessed + " records were processed");
    }

    @Override
    public int scrapeSite(State state, Site site, ExcelWriter excelWriter) {
        //refactor to split out randomizing functionality, maybe reuse??
        int recordsProcessed = 0;
        site.getBaseUrl();
        String firstPageResults = site.generateResultsPageUrl(1);
        //Add some retries if first connection to state site fails?
        Document mainPageDoc = spiderUtil.getHtmlAsDoc(firstPageResults);
        if (engineUtil.docWasRetrieved(mainPageDoc)) {
            //restricting to 2 pages for now
        	//int numberOfPages = site.getTotalPages(mainPageDoc);
        	int numberOfPages = 2;
            if (numberOfPages==0) {
                numberOfPages = 1;
            }
            Map<String, String> resultsUrlPlusMiscMap = new HashMap<>();
            logger.debug("Generating list of results pages for : " + site.getName() + " - " + state.getName());
            //also get misc urls
            Map<String,String> miscUrls = site.getMiscSafeUrlsFromDoc(mainPageDoc, numberOfPages);
            for (int p=1; p<=numberOfPages;p++) {
                resultsUrlPlusMiscMap.put(String.valueOf(p), site.generateResultsPageUrl(p));
            }

            resultsUrlPlusMiscMap.putAll(miscUrls);

            //shuffle urls before retrieving docs
            Map<String,Document> resultsDocPlusMiscMap = new HashMap<>();
            List<String> keys = new ArrayList<>(resultsUrlPlusMiscMap.keySet());
            Collections.shuffle(keys);
            String previousKey = keys.get(keys.size()-1);
            for (String k : keys) {
            	try {
            		//can we guarantee previous is a page that has access to the current?
	        		Connection.Response response = null;
	        		Document docToCheck = null;
	            	Connection conn = ConnectionUtil.getConnection(resultsUrlPlusMiscMap.get(k), resultsUrlPlusMiscMap.get(previousKey));
	    			if (spiderUtil.offline()) {
	    				response = new OfflineResponse(200, resultsUrlPlusMiscMap.get(k));
	    			} else {
	    				response = conn.execute();
	    			}
					//TODO depending on response status code, take action
	    			docToCheck = response.parse();
	    			resultsDocPlusMiscMap.put(k, docToCheck);
            	} catch (IOException ioe) {
            		logger.error(ioe);
            	}
                
                int sleepTime = ConnectionUtil.getSleepTime(site);
                spiderUtil.sleep(sleepTime, false);
                logger.debug("Sleeping for " + sleepTime + " after fetching " + resultsUrlPlusMiscMap.get(k));
                
                previousKey = k;
            }

            //saving this for later?? should be able to get previous sorting by looking at page number in baseUri
            site.setOnlyResultsPageDocuments(resultsDocPlusMiscMap);

            //build a list of details page urls by parsing only results page docs in order
            Map<String,Document> resultsPageDocsMap = site.getResultsPageDocuments();
            Map<String,String> recordDetailUrlMap = new HashMap<>();
            for (Map.Entry<String, Document> entry : resultsPageDocsMap.entrySet()) {
                Document doc = entry.getValue();
                //only proceed if document was retrieved
                if (engineUtil.docWasRetrieved(doc)){
                    logger.debug("Gather complete list of records to scrape from " + doc.baseUri());
                    recordDetailUrlMap.putAll(parseDocForUrls(doc, site));
                    //including some non-detail page links then randomize
                    recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, recordDetailUrlMap.size())); 
                } else {
                    logger.info("Nothing was retrieved for " + doc.baseUri());
                }
            }

            int recordsGathered = recordDetailUrlMap.size();
            logger.info("Gathered links for " + recordsGathered + " record profiles and misc");

            //****TODO
            //****use sorted map to check for already scraped records - should I used ID as map.key instead of a sequence?

            spiderUtil.sleep(1000, true);
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
        Elements recordDetailElements = site.getRecordElements((Document) doc);
        for(int e=0;e<recordDetailElements.size();e++) {
            String url = site.getRecordDetailDocUrl(recordDetailElements.get(e));
            String id = site.getRecordId(url);
            //TODO try this earlier to avoid opening crawled results pages?
            //only add if we haven't already crawled it
            if (!scrapedIds.contains(id)) {
            	recordDetailUrlMap.put(id, url);
            }
        }
        return recordDetailUrlMap;
    }
    
    @Override
    public int scrapeRecords(Map<String,String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter) {
        int recordsProcessed = 0;
        List<Record> arrestRecords = new ArrayList<>();
        Record arrestRecord = new ArrestRecord();
        List<String> keys = new ArrayList<>(recordsDetailsUrlMap.keySet());
        Collections.shuffle(keys);
        String previousKey = keys.get(keys.size()-1);
        for (String k : keys) {
            String url = recordsDetailsUrlMap.get(k);
            Connection.Response response = null;
    		Document profileDetailDoc = null;
            try {
	            //can we guarantee previous is a page that has access to the current?
	        	Connection conn = ConnectionUtil.getConnection(url, recordsDetailsUrlMap.get(previousKey));
				if (spiderUtil.offline()) {
					response = new OfflineResponse(200, recordsDetailsUrlMap.get(k));
				} else {
					response = conn.execute();
				}
				//TODO depending on response status code, take action
				profileDetailDoc = response.parse();
            } catch (FileNotFoundException fnfe) {
            	logger.error("No html doc found for " + url);
            } catch (IOException ioe) {
            	logger.error("Error trying to connect and retrieve " + url, ioe);
            }
            if (site.isARecordDetailDoc(profileDetailDoc)) {
                if (engineUtil.docWasRetrieved(profileDetailDoc)) {
                    recordsProcessed++;
                    //should we check for ID first or not bother unless we see duplicates??
                    arrestRecord = populateArrestRecord(profileDetailDoc, site);
                    arrestRecords.add(arrestRecord);
                    //save each record in case of failures
                    excelWriter.addRecordToWorkbook(arrestRecord);
                    spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
                    
                } else {
                    logger.error("Failed to load html doc from " + url);
                }
            }
            previousKey = k;
        }
        //save the whole thing at the end
        //order and save the overwrite the spreadsheet
        excelWriter.saveRecordsToWorkbook(arrestRecords);
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
    public ExcelWriter initializeOutputter(State state, Site site) throws SpiderException {
    	ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord(), site);
        try {
            //this will get previously written IDs but then overwrite the spreadsheet
            scrapedIds = excelWriter.getPreviousIds();
            excelWriter.createSpreadsheet();
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return excelWriter;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void matchPropertyToField(ArrestRecord record, Element profileDetail) {
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
                    formatName(record, profileDetail);
                } else if (label.contains("date")) {
                    Date date = new Date(extractValue(profileDetail));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    record.setArrestDate(calendar);
                } else if (label.contains("time")) {
                    formatArrestTime(record, profileDetail);
                } else if (label.contains("arrest age")) {
                    record.setArrestAge(Integer.parseInt(extractValue(profileDetail)));
                } else if (label.contains("gender")) {
                    record.setGender(extractValue(profileDetail));
                } else if (label.contains("city")) {
                    String city = profileDetail.select("span[itemProp=\"addressLocality\"]").text();
                    String state = profileDetail.select("span[itemprop=\"addressRegion\"]").text();
                    record.setCity(city);
                    record.setState(state);
                } else if (label.contains("total bond")) {
                    String bondAmount = extractValue(profileDetail);
                    int totalBond = Integer.parseInt(bondAmount.replace("$", ""));
                    record.setTotalBond(totalBond);
                } else if (label.contains("height")) {
                    record.setHeight(extractValue(profileDetail));
                } else if (label.contains("weight")) {
                    record.setWeight(extractValue(profileDetail));
                } else if (label.contains("hair color")) {
                    record.setHairColor(extractValue(profileDetail));
                } else if (label.contains("eye color")) {
                    record.setEyeColor(extractValue(profileDetail));
                } else if (label.contains("birth")) {
                    record.setBirthPlace(extractValue(profileDetail));
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
        record.setFirstName(profileDetail.select("span [itemprop=\"givenName\"]").text());
        record.setMiddleName(profileDetail.select("span [itemprop=\"additionalName\"]").text());
        record.setLastName(profileDetail.select("span [itemprop=\"familyName\"]").text());
        String fullName = record.getFirstName();
        fullName += record.getMiddleName()!=null?" " + record.getMiddleName():"";
        fullName += " " + record.getLastName();
        record.setFullName(fullName);
    }
    @Override
    public void formatArrestTime(ArrestRecord record, Element profileDetail) {
        Calendar arrestDate = record.getArrestDate();
        if (arrestDate!=null) {
            String arrestTimeText = profileDetail.text().replaceAll("(?i)time:", "").trim();
            arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
            arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
            arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
            record.setArrestDate(arrestDate);
        }
    }
    @Override
    public String extractValue(Element profileDetail) {
        return profileDetail.text().substring(profileDetail.text().indexOf(':')+1).trim();
    }
}
