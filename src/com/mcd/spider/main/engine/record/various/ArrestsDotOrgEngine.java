package com.mcd.spider.main.engine.record.various;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.ArrestRecord.RecordColumnEnum;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.main.entities.site.html.SiteHTML;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.OutputUtil;
import com.mcd.spider.main.util.SpiderUtil;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
    private Set<String> crawledIds;
    private RecordFilterEnum filter;
    private boolean offline;
    private ConnectionUtil connectionUtil;

    @Override
    public Site getSite(String[] args) {
    	return new ArrestsDotOrgSite(args);
    }
    
    @Override
    public void getArrestRecords(State state, long maxNumberOfResults, RecordFilterEnum filter) throws SpiderException {
        long totalTime = System.currentTimeMillis();
        long recordsProcessed = 0;
        int sleepTimeSum = 0;
        this.filter = filter;
	    offline = System.getProperty("offline").equals("true");

        //while(recordsProcessed <= maxNumberOfResults) {
        ArrestsDotOrgSite site = (ArrestsDotOrgSite) getSite(new String[]{state.getName()});
        OutputUtil outputUtil = initializeOutputter(state, site);
        connectionUtil = new ConnectionUtil(true);
        
        long siteTime = System.currentTimeMillis();
        logger.info("----Site: " + site.getName() + "-" + state.getName() + "----");
        logger.debug("Sending spider " + (offline?"offline":"online" ));
        
        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
        sleepTimeSum += offline?0:sleepTimeAverage;
        long time = System.currentTimeMillis();
        
        recordsProcessed += scrapeSite(state, site, outputUtil, 1, maxNumberOfResults);
        
        time = System.currentTimeMillis() - time;
        logger.info(site.getBaseUrl() + " took " + time + " ms");

        //outputUtil.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
        siteTime = System.currentTimeMillis() - siteTime;
        logger.info(state.getName() + " took " + siteTime + " ms");

        spiderUtil.sendEmail(state);
        
        //}
        totalTime = System.currentTimeMillis() - totalTime;
        if (!offline) {
            logger.info("Sleep time was approximately " + sleepTimeSum + " ms");
            logger.info("Processing time was approximately " + (totalTime-sleepTimeSum) + " ms");
        } else {
            logger.info("Total time taken was " + totalTime + " ms");
        }
        logger.info(recordsProcessed + " total records were processed");
    }

    @Override
    public int scrapeSite(State state, Site site, OutputUtil outputUtil, int attemptCount, long maxNumberOfResults) {
        //refactor to split out randomizing functionality, maybe reuse??
    	int maxAttempts = site.getMaxAttempts();
        int recordsProcessed = 0;
        SiteHTML htmlSite = (ArrestsDotOrgSite)site;
        htmlSite.getBaseUrl();
        String firstPageResults = htmlSite.generateResultsPageUrl(1);
        Document mainPageDoc = null;
        Map<String,String> nextRequestCookies = new HashMap<>();
        try {
        	//TODO also set headers?
            Connection.Response response = connectionUtil.retrieveConnectionResponse(firstPageResults, "www.google.com");
            for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()){
                logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
            }
            mainPageDoc = response.parse();
            nextRequestCookies = response.cookies();
            nextRequestCookies.put("__utmb", "202431297.1.10.1502492018");
        } catch (IOException e) {
            logger.error("Couldn't make initial connection to site. Trying again " + (maxAttempts-attemptCount) + " more times", e);
            //if it's a 500, we're probably blocked. Try a new user-agent TODO and IP if possible
            if (e instanceof HttpStatusException && ((HttpStatusException) e).getStatusCode()==500) {
            	connectionUtil = new ConnectionUtil(true);
            }
            attemptCount++;
            scrapeSite(state, site, outputUtil, attemptCount, maxNumberOfResults);
        }
        if (spiderUtil.docWasRetrieved(mainPageDoc) && attemptCount<=maxAttempts) {
            //restricting to 2 pages for now
        	//int numberOfPages = site.getTotalPages(mainPageDoc);
        	int numberOfPages = 3;
            if (numberOfPages==0) {
                numberOfPages = 1;
            }
            Map<Object, String> resultsUrlPlusMiscMap = new HashMap<>();
            logger.debug("Generating list of results pages for : " + htmlSite.getName() + " - " + state.getName());
            //also get misc urls
            Map<Object,String> miscUrls = htmlSite.getMiscSafeUrlsFromDoc(mainPageDoc, numberOfPages);
            for (int p=1; p<=numberOfPages;p++) {
                resultsUrlPlusMiscMap.put(p, htmlSite.generateResultsPageUrl(p));
            }

            resultsUrlPlusMiscMap.putAll(miscUrls);

            //shuffle urls before retrieving docs
            Map<Integer,Document> resultsDocPlusMiscMap = new HashMap<>();
            List<Object> keys = new ArrayList<>(resultsUrlPlusMiscMap.keySet());
            Collections.shuffle(keys);
            Integer previousKey = (Integer)keys.get(keys.size()-1);
            int furthestPageToCheck = 9999;
            for (Object k : keys) {
            	int page = (Integer) k;
            	if (page<=furthestPageToCheck) {
	            	Document docToCheck = null;
	            	String url = resultsUrlPlusMiscMap.get(k);
	            	try {
		        		//can we guarantee previous is a page that has access to the current?
	                	//TODO also set headers?
		    			Connection.Response response = connectionUtil.retrieveConnectionResponse(url, resultsUrlPlusMiscMap.get(previousKey), nextRequestCookies);
		        		docToCheck = response.parse();
		        		nextRequestCookies = setCookies(response, nextRequestCookies, recordsProcessed);
	            	} catch (FileNotFoundException fnfe) {
	                	logger.error("No html doc found for " + url);
	                } catch (IOException e) {
	            		logger.error("Failed to get a connection to " + url, e);
	            	}
	            	//if docToCheck contains a crawledId, remember page number and don't add subsequent pages
	            	for (String crawledId : crawledIds) {
	            		if (furthestPageToCheck==9999) {
			            	if (docToCheck!=null && docToCheck.html().contains(crawledId)) {
			            		//set as current page number
			            		furthestPageToCheck = page;
			            	}
	            		}
	            	}
	            	if (docToCheck!=null) {
	            		resultsDocPlusMiscMap.put((Integer)k, docToCheck);
	            	}
	                int sleepTime = ConnectionUtil.getSleepTime(htmlSite);
                    logger.debug("Sleeping for " + sleepTime + " after fetching " + resultsUrlPlusMiscMap.get(k));
	                spiderUtil.sleep(sleepTime, false);
	                
	                previousKey = page;
            	}
            }

            //build a list of details page urls by parsing results page docs
            Map<Object,String> recordDetailUrlMap = new HashMap<>();
            for (Map.Entry<Integer, Document> entry : resultsDocPlusMiscMap.entrySet()) {
                Document doc = entry.getValue();
                //only proceed if document was retrieved
                if (spiderUtil.docWasRetrieved(doc) && doc.baseUri().contains("&results=")){
                    logger.debug("Gather complete list of records to scrape from " + doc.baseUri());
                    recordDetailUrlMap.putAll(parseDocForUrls(doc, htmlSite));
                    //include some non-detail page links then randomize
                    recordDetailUrlMap.putAll(htmlSite.getMiscSafeUrlsFromDoc(mainPageDoc, recordDetailUrlMap.size())); 
                } else {
                    logger.info("Nothing was retrieved for " + doc.baseUri());
                }
            }

            int recordsGathered = recordDetailUrlMap.size();
            logger.info("Gathered links for " + recordsGathered + " record profiles and misc");

            spiderUtil.sleep(1000, true);
            //****iterate over collection, scraping records and simply opening others
            recordsProcessed += scrapeRecords(recordDetailUrlMap, site, outputUtil, nextRequestCookies);

        } else {
            logger.error("Failed to load html doc from " + site.getBaseUrl()+ ". Trying again " + (maxAttempts-attemptCount) + " more times");
            attemptCount++;
            scrapeSite(state, site, outputUtil, attemptCount, maxNumberOfResults);
        }
        return recordsProcessed;
    }
    
    @Override
    public Map<String,String> parseDocForUrls(Object doc, Site site) {
    	SiteHTML htmlSite = (ArrestsDotOrgSite) site;
        Map<String,String> recordDetailUrlMap = new HashMap<>();
        Elements recordDetailElements = htmlSite.getRecordElements((Document) doc);
        for(int e=0;e<recordDetailElements.size();e++) {
            String url = htmlSite.getRecordDetailDocUrl(recordDetailElements.get(e));
            String id = htmlSite.generateRecordId(url);
            //only add if we haven't already crawled it
            if (!crawledIds.contains(id)) {
            	recordDetailUrlMap.put(id, url);
            }
        }
        return recordDetailUrlMap;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	@Override
    public int scrapeRecords(Map<Object,String> recordsDetailsUrlMap, Site site, OutputUtil outputUtil, Map<String,String> cookies) {
    	SiteHTML htmlSite = (ArrestsDotOrgSite) site;
    	int failedAttempts = 0;
        int recordsProcessed = 0;
        List<Record> arrestRecords = new ArrayList<>();
        ArrestRecord arrestRecord;
        List<Object> keys = new ArrayList<>(recordsDetailsUrlMap.keySet());
        Collections.shuffle(keys);
        String previousKey = String.valueOf(keys.get(keys.size()-1));
        Map<String,String> nextRequestCookies = cookies;
        for (Object k : keys) {
            String url = recordsDetailsUrlMap.get(k);
    		Document profileDetailDoc = null;
        	try {
        		//can we guarantee previous is a page that has access to the current?
            	//TODO also set headers?
    			Connection.Response response = connectionUtil.retrieveConnectionResponse(url, recordsDetailsUrlMap.get(previousKey), nextRequestCookies);
    			profileDetailDoc = response.parse();
        		nextRequestCookies = setCookies(response, nextRequestCookies, recordsProcessed);
        		//depending on response status code, take action
        	} catch (FileNotFoundException fnfe) {
            	logger.error("No html doc found for " + url);
            } catch (IOException e) {
            	failedAttempts++;
        		logger.error("Failed to get a connection to " + recordsDetailsUrlMap.get(k), e);
            	if (failedAttempts>=site.getMaxAttempts()) {
            		//save remaining records to file and exit 
            		//TODO or retry with new connection/IP?
            		outputUtil.backupUnCrawledRecords(recordsDetailsUrlMap);
            		logger.info("Hit the limit of failed connections. Saving list of unprocessed records and quitting");
            		return recordsProcessed;
            	}
            }
            if (htmlSite.isARecordDetailDoc(profileDetailDoc)) {
                if (spiderUtil.docWasRetrieved(profileDetailDoc)) {
                    try {
                        recordsProcessed++;
                        //should we check for ID first or not bother unless we start seeing duplicates??
                        arrestRecord = populateArrestRecord(profileDetailDoc, htmlSite);
                        arrestRecords.add(arrestRecord);
                        //save each record in case of failures mid-crawling
                        outputUtil.addRecordToMainWorkbook(arrestRecord);
                        spiderUtil.sleep(connectionUtil.getSleepTime(htmlSite), true);//sleep at random interval
                    } catch (Exception e) {
                        logger.error("Generic exception caught while trying to grab arrest record for " + profileDetailDoc.baseUri(), e);
                    }
                    
                } else {
                    logger.error("Failed to load html doc from " + url);
                }
            }
            spiderUtil.sleep(connectionUtil.getSleepTime(htmlSite)/2, false);
        	logger.info("Sleeping for half time because no record was crawled");
            previousKey = String.valueOf(k);
        }
        
        //format the output
        Collections.sort(arrestRecords, ArrestRecord.CountyComparator);
    	String delimiter = RecordColumnEnum.COUNTY_COLUMN.title();
    	Class clazz = ArrestRecord.class;
        if (filter!=null) {
	        List<Record> filteredRecords = filterRecords(arrestRecords);
	        List<List<Record>> splitRecords = Record.splitByField(filteredRecords, delimiter, clazz);
	        //create a separate sheet with filtered results
	        logger.info(filteredRecords.size() + " " + filter.filterName() + " " + "records were crawled");
	        outputUtil.createFilteredSpreadsheet(filter, filteredRecords);
	        outputUtil.splitIntoSheets(outputUtil.getFilteredDocName(filter), delimiter, splitRecords, clazz);
        }
        List<List<Record>> splitRecords = Record.splitByField(arrestRecords, delimiter, clazz);
        outputUtil.splitIntoSheets(outputUtil.getDocName(), delimiter, splitRecords, clazz);
    
        return recordsProcessed;
    }
    
    @Override
    public ArrestRecord populateArrestRecord(Object profileDetailObj, Site site) {
    	SiteHTML htmlSite = (ArrestsDotOrgSite) site;
        Elements profileDetails = htmlSite.getRecordDetailElements((Document) profileDetailObj);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.generateRecordId(((Node) profileDetailObj).baseUri()));
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.info("\t" + profileDetail.text());
        }
        return record;
    }

    @Override
    public OutputUtil initializeOutputter(State state, Site site) throws SpiderException {
    	OutputUtil outputUtil = new OutputUtil(state, new ArrestRecord(), site);
        try {
            //this will get previously written IDs but then overwrite the spreadsheet
            crawledIds = outputUtil.getPreviousIds();
            outputUtil.createSpreadsheet();
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return outputUtil;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void matchPropertyToField(ArrestRecord record, Object profileDetail) {
    	Element profileDetailElement = (Element) profileDetail;
        String label = profileDetailElement.select("b").text().toLowerCase();
        Elements charges = profileDetailElement.select(".charges li");
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
                    formatName(record, profileDetailElement);
                } else if (label.contains("date")) {
                    Date date = new Date(extractValue(profileDetailElement));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    record.setArrestDate(calendar);
                } else if (label.contains("time")) {
                    formatArrestTime(record, profileDetailElement);
                } else if (label.contains("arrest age")) {
                    record.setArrestAge(Integer.parseInt(extractValue(profileDetailElement)));
                } else if (label.contains("gender")) {
                    record.setGender(extractValue(profileDetailElement));
                } else if (label.contains("city")) {
                    String city = profileDetailElement.select("span[itemProp=\"addressLocality\"]").text();
                    String state = profileDetailElement.select("span[itemprop=\"addressRegion\"]").text();
                    record.setCity(city);
                    record.setState(state);
                } else if (label.contains("total bond")) {
                    String bondAmount = extractValue(profileDetailElement);
                    int totalBond = Integer.parseInt(bondAmount.replace("$", ""));
                    record.setTotalBond(totalBond);
                } else if (label.contains("height")) {
                    record.setHeight(extractValue(profileDetailElement));
                } else if (label.contains("weight")) {
                    record.setWeight(extractValue(profileDetailElement));
                } else if (label.contains("hair color")) {
                    record.setHairColor(extractValue(profileDetailElement));
                } else if (label.contains("eye color")) {
                    record.setEyeColor(extractValue(profileDetailElement));
                } else if (label.contains("birth")) {
                    record.setBirthPlace(extractValue(profileDetailElement));
                }
            } catch (NumberFormatException nfe) {
                logger.error("Couldn't parse a numeric value from " + profileDetailElement.text());
            }
        //trying it twice as the data seems inconsistent for county
        } else if (profileDetailElement.select("h3").hasText()) {
            record.setCounty(profileDetailElement.select("h3").text().replaceAll("(?i)county", "").trim());
        } else if (profileDetailElement.hasAttr("src") && profileDetailElement.attr("src").contains("/mugs")) {
        	String srcPath = profileDetailElement.attr("src").replaceAll("/mugs/", "");
            record.setCounty(srcPath.substring(0, srcPath.indexOf('/')));
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
    
    @Override
    public List<Record> filterRecords(List<Record> fullArrestRecords) {
    	List<Record> filteredArrestRecords = new ArrayList<>();
    	//TODO nullpointer somewhere in here
    	for (Record record : fullArrestRecords) {
    		boolean recordMatches = false;
    		String[] charges = ((ArrestRecord) record).getCharges();
    		for (String charge : charges) {
    			if (!recordMatches) {
    				recordMatches = RecordFilter.filter(charge, filter);
    			}
    		}
    		if (recordMatches) {
    			filteredArrestRecords.add(record);
    		}
    	}
    	return filteredArrestRecords;
    }
    
    private Map<String,String> setCookies(Connection.Response response, Map<String,String> nextRequestCookies, int recordsProcessed) {
    	for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()) {
			nextRequestCookies.put(cookieEntry.getKey(), cookieEntry.getValue());
            logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
		}
        try {
            String cookieToCycle = nextRequestCookies.get("__utmb");
            int recordCap = offline?3:100;
            if (recordsProcessed!=0 && recordsProcessed % recordCap == 0) {
                //every 100 records, cycle back to 1
                //TODO change user-agent as well?
                //TODO change IP?
                //these should only increment with results page views or new details pages, not layover details
    //			response.cookie("views_session", "1");
    //			response.cookie("views_24", "1");
                String[] stringPieces = cookieToCycle.split("\\.");
                String newCookie = stringPieces[0] + "." + "1" + "." + stringPieces[2] + "." + stringPieces[3];
                nextRequestCookies.put("__utmb", newCookie);
            } else {
                String[] stringPieces = cookieToCycle.split("\\.");
                String newCookie = stringPieces[0] + "." + (Integer.parseInt(stringPieces[1])+1) + "." + stringPieces[2] + "." + stringPieces[3];
                nextRequestCookies.put("__utmb", newCookie);
            }
        } catch (NumberFormatException nfe) {
            logger.error("Failed to parse __utmb cookie for resetting");
        }
    	return nextRequestCookies;
    }
}
