package com.mcd.spider.engine.record.various;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.ArrestRecord.RecordColumnEnum;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.filter.RecordFilter;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.exception.ExcelOutputException;
import com.mcd.spider.exception.IDCheckException;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.util.ConnectionUtil;
import com.mcd.spider.util.SpiderUtil;
import com.mcd.spider.util.io.RecordIOUtil;
import com.mcd.spider.util.io.RecordOutputUtil;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.mcd.spider.entities.record.ArrestRecord.ArrestDateComparator;
import static com.mcd.spider.entities.record.ArrestRecord.CountyComparator;

/**
 *
 * @author Michael De Jong
 *
 */

public class ArrestsDotOrgEngine implements ArrestRecordEngine {

    public static final Logger logger = Logger.getLogger(ArrestsDotOrgEngine.class);

    private SpiderUtil spiderUtil = new SpiderUtil();
    private ConnectionUtil connectionUtil;
    private ArrestsDotOrgSite site;
    private RecordIOUtil recordIOUtil;
    private SpiderWeb spiderWeb;
    List<Record> arrestRecords = new ArrayList<>();
    private boolean connectionEstablished = false;

    public ArrestsDotOrgEngine(SpiderWeb web) {
    	this.spiderWeb = web;
    	this.site = new ArrestsDotOrgSite(new String[]{web.getState().getName()});
        connectionUtil = new ConnectionUtil(true);
    }

    public ArrestsDotOrgEngine(String stateName) {
    	this.site = new ArrestsDotOrgSite(new String[]{stateName});
        connectionUtil = new ConnectionUtil(true);
    }
    
    @Override
    public void setSpiderWeb(SpiderWeb web) {
    	this.spiderWeb = web;
    }

    @Override
    public SpiderWeb getSpiderWeb() {
		return spiderWeb;
	}

    @Override
    public Site getSite() {
    	return site;
    }
    
    @Override
    public RecordIOUtil getRecordIOUtil() {
    	return recordIOUtil;
    }

    public void setRecordIOUtil(RecordIOUtil recordIOUtil) {
    	this.recordIOUtil = recordIOUtil;
    }

	@Override
    public void getArrestRecords() throws SpiderException {
        long totalTime = System.currentTimeMillis();  
        recordIOUtil = initializeIOUtil(spiderWeb.getState().getName());

        logger.info("----Site: " + site.getName() + "-" + spiderWeb.getState().getName() + "----");
        logger.debug("Sending spider " + (spiderWeb.isOffline()?"offline":"online" ));
        
        int sleepTimeAverage = spiderWeb.isOffline()?0:(site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2000;
        
        scrapeSite();

        spiderUtil.sendEmail(spiderWeb.getState().getName());
        
        totalTime = System.currentTimeMillis() - totalTime;
        if (!spiderWeb.isOffline()) {
            logger.info("Sleep time was approximately " + sleepTimeAverage*spiderWeb.getRecordsProcessed() + " ms");
            logger.info("Processing time was approximately " + (totalTime-(sleepTimeAverage*spiderWeb.getRecordsProcessed())) + " ms");
        } else {
            logger.info("Total time taken was " + totalTime + " ms");
        }
        logger.info(spiderWeb.getRecordsProcessed() + " total records were processed");
    }

    @Override
    public void scrapeSite() {
    	arrestRecords = new ArrayList<>();
    	int maxAttempts = site.getMaxAttempts();
        String firstPageResultsUrl = site.generateResultsPageUrl("1");
        Document mainPageDoc = null;
        if (spiderWeb.getAttemptCount()<=maxAttempts) {
	        try {
	            logger.info("Trying to make initial connection to " + site.getName());
                mainPageDoc = (Document) initiateConnection(firstPageResultsUrl);
	        } catch (IOException e) {
	            logger.error("Couldn't make initial connection to site. Trying again " + (maxAttempts-spiderWeb.getAttemptCount()) + " more times", e);
	            //if it's a 500, we're probably blocked. TODO Try a new IP if possible, else bail
	            if (e instanceof HttpStatusException && ((HttpStatusException) e).getStatusCode()==500) {
	            	connectionUtil = new ConnectionUtil(true);
	            }
                spiderUtil.sleep(5000, true);
	            spiderWeb.increaseAttemptCount();
	            scrapeSite();
	        }
            if (spiderUtil.docWasRetrieved(mainPageDoc)) {
                connectionEstablished = true;
                Map<Object, String> recordDetailUrlMap;
                if (spiderWeb.retrieveMissedRecords() && !spiderWeb.getUncrawledIds().isEmpty()) {
                    //create map from uncrawled records
                    logger.info("Generating details page urls from backup file");

                    //build a list of details page urls by reading in uncrawled ids file
                    recordDetailUrlMap = compileRecordDetailUrlMapFromBackup(mainPageDoc, spiderWeb.getUncrawledIds());
                    logger.info("Gathered links for "+recordDetailUrlMap.size()+" uncrawled record profiles and misc pages");

                } else {
                    spiderWeb.setNumberOfPages(getNumberOfResultsPages(mainPageDoc));

                    logger.info("Generating list of results pages for : "+site.getName()+" - " + spiderWeb.getState().getName());
                    Map<Object, String> resultsUrlPlusMiscMap = compileResultsUrlMap(mainPageDoc);

                    logger.info("Retrieving results page docs");
                    Map<Integer, Document> resultsDocPlusMiscMap = compileResultsDocMap(resultsUrlPlusMiscMap);

                    if (resultsDocPlusMiscMap.isEmpty()) {
                        logger.info("No results doc pages were gathered. Quitting");
                        return;
                    }

                    logger.info("Retrieving details page urls");
                    //build a list of details page urls by parsing results page docs
                    recordDetailUrlMap = compileRecordDetailUrlMap(mainPageDoc, resultsDocPlusMiscMap);
                    logger.info("Gathered links for "+recordDetailUrlMap.size()+" record profiles and misc pages");
                }
                spiderUtil.sleep(spiderWeb.isOffline()?0:ConnectionUtil.getSleepTime(site)*2, true);
                //****iterate over collection, scraping records and simply opening others
                scrapeRecords(recordDetailUrlMap);
            } else {
	            logger.error("Failed to load html doc from " + site.getBaseUrl()+ ". Trying again " + (maxAttempts-spiderWeb.getAttemptCount()) + " more times");
	            spiderWeb.increaseAttemptCount();
	            scrapeSite();
	        }
        } else {
        	logger.error("Too many attempts accessing " + site.getBaseUrl()+ ". Quitting.");
        }
		finalizeOutput(arrestRecords);
    }

    @Override
    public Map<String,String> parseDocForUrls(Object doc) {
        Map<String,String> recordDetailUrlMap = new HashMap<>();
        Elements recordDetailElements = site.getRecordElements((Document) doc);
        for(int e=0;e<recordDetailElements.size();e++) {
            String url = site.getRecordDetailDocUrl(recordDetailElements.get(e));
            String id = site.obtainRecordId(url);
            //only add if we haven't already crawled it
            if (id != null && !spiderWeb.getCrawledIds().contains(id)) {
            	recordDetailUrlMap.put(id, url);
            }
        }
        return recordDetailUrlMap;
    }

	@Override
    public void scrapeRecords(Map<Object,String> recordsDetailsUrlMap) {
    	RecordOutputUtil recordOutputUtil = recordIOUtil.getOutputter();
        arrestRecords.addAll(spiderWeb.getCrawledRecords().getRecords());
        ArrestRecord arrestRecord;
        List<Object> keys = new ArrayList<>(recordsDetailsUrlMap.keySet());
        Collections.shuffle(keys);
        String previousKey = String.valueOf(keys.get(keys.size()-1));
        for (Object k : keys) {
        	if (spiderWeb.getRecordsProcessed()<spiderWeb.getMaxNumberOfResults()) {
	            String url = recordsDetailsUrlMap.get(k);
	            Document profileDetailDoc = null;
	            try {
		    		profileDetailDoc = obtainRecordDetailDoc(url, recordsDetailsUrlMap.get(previousKey));
	        	} catch (FileNotFoundException fnfe) {
	            	logger.error("No html doc found for " + url);
	            } catch (IOException e) {
	            	spiderWeb.increaseAttemptCount();
	        		logger.error("Failed to get a connection to " + recordsDetailsUrlMap.get(k), e);
	            	if (spiderWeb.getAttemptCount()>=site.getMaxAttempts()) {
	            		//save remaining records to file and exit
	            		//TODO or retry with new connection/IP?
	            		logger.error("Hit the limit of failed connections. Saving list of unprocessed records, formatting the current output and quitting");
	            		recordOutputUtil.backupUnCrawledRecords(recordsDetailsUrlMap);
	            		return;
	            	}
	            }

	            if (spiderUtil.docWasRetrieved(profileDetailDoc)) {
	            	if (site.isARecordDetailDoc(profileDetailDoc)) {
	            		try {
	            			spiderWeb.addToRecordsProcessed(1);
	            			arrestRecord = populateArrestRecord(profileDetailDoc);
	            			//try to match the record/county to the state being crawled
	            			if (arrestRecord.getState()==null || arrestRecord.getState().equalsIgnoreCase(spiderWeb.getState().getName())) {
	            				arrestRecords.add(arrestRecord);
	            				//save each record in case of failures mid-crawling
	            				recordOutputUtil.addRecordToMainWorkbook(arrestRecord);
	            				//"remove" record from recordsDetailUrlMap
                                recordsDetailsUrlMap.replace(k, "CRAWLED" + recordsDetailsUrlMap.get(k));
	            				logger.debug("Record " + spiderWeb.getRecordsProcessed() + " saved");
	            			}
	            			spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
	            		} catch (Exception e) {
	            			logger.error("Generic exception caught while trying to grab arrest record for " + profileDetailDoc.baseUri(), e);
                            logger.info("Sleeping for half time because no record was crawled");
                            spiderUtil.sleep(ConnectionUtil.getSleepTime(site)/2, false);
	            		}

	            	} else {
	            		logger.debug("This doc doesn't have any record details: " + profileDetailDoc.baseUri());
                        logger.info("Sleeping for half time because no record was crawled");
                        spiderUtil.sleep(ConnectionUtil.getSleepTime(site)/2, false);
	            	}
	            } else {
	            	logger.error("Failed to load html doc from " + url);
                    logger.info("Sleeping for half time because no record was crawled");
                    spiderUtil.sleep(ConnectionUtil.getSleepTime(site)/2, false);
	            }

                //don't change previous key (referer) if current url is detail page, they're just layovers
                if (!site.isARecordDetailDoc(profileDetailDoc)) {
                    previousKey = String.valueOf(k);
                }
        	}
        }
    }

    @Override
    public ArrestRecord populateArrestRecord(Object profileDetailObj) {
        Elements profileDetails = site.getRecordDetailElements((Document) profileDetailObj);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.obtainRecordId(((Node) profileDetailObj).baseUri()));
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.debug("\t" + profileDetail.text());
        }
        return record;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void matchPropertyToField(ArrestRecord record, Object profileDetail) {
    	Element profileDetailElement = (Element) profileDetail;
        String label = profileDetailElement.select("b").text().toLowerCase();
        Elements charges = profileDetailElement.select(".charges li");
        if (!charges.isEmpty()) {
            String[] chargeStrings = new String[charges.size()];
            for (int c = 0; c < charges.size(); c++) {
                chargeStrings[c] = charges.get(c).text();
            }
            record.setCharges(chargeStrings);
        } else if (!label.equals("")) {
            try {
                if (label.contains("full name")) {
                    formatName(record, profileDetailElement);
                } else if (label.contains("birthdate")) { //needs to be first to avoid arrest date getting put here
                    Date date = new Date(extractValue(profileDetailElement));
                    record.setDob(date);
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
                    long totalBond = Integer.parseInt(bondAmount.replace("$", ""));
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
    public Object initiateConnection(String url) throws IOException {
        Connection.Response response;
        if (!connectionEstablished) {
            response = connectionUtil.retrieveConnectionResponse(url, "www.google.com");
            for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()){
                logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("Host", "iowa.arrests.org");
            headers.put("Accept", "text/html, */*; q=0.01");
            headers.put("Accept-Language", "en-US,en;q=0.5  ");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("X-fancyBox", "true");
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("Connection", "keep-alive");
            spiderWeb.setHeaders(headers);
        } else {
            response = connectionUtil.retrieveConnectionResponse(url, "www.google.com", spiderWeb.getSessionCookies(), spiderWeb.getHeaders());
        }
        spiderWeb.setSessionCookies(response.cookies());
        return response.parse();
    }

    public int getNumberOfResultsPages(Document mainPageDoc) {
        int numberOfPages = site.getTotalPages(mainPageDoc);
        if (!spiderWeb.retrieveMissedRecords() && (numberOfPages>spiderWeb.getMaxNumberOfResults()/site.getResultsPerPage())) {
            numberOfPages = (int) spiderWeb.getMaxNumberOfResults() / site.getResultsPerPage();
            numberOfPages = (int) spiderWeb.getMaxNumberOfResults() % site.getResultsPerPage()>0?numberOfPages+1:numberOfPages+0;
        }
    
        if (numberOfPages==0) {
            numberOfPages = 1;
        }
        return numberOfPages;
    }
    
    public Map<Object,String> compileResultsUrlMap(Document mainPageDoc) {
        Map<Object,String> resultsUrlMap = new HashMap<>();
        for (int p=1; p<=spiderWeb.getNumberOfPages();p++) {
            resultsUrlMap.put(p, site.generateResultsPageUrl(String.valueOf(p)));
        }
        //also get misc urls
        if (spiderWeb.getMisc()) {
            Map<Object,String> miscUrls = site.getMiscSafeUrlsFromDoc(mainPageDoc, spiderWeb.getNumberOfPages());
            resultsUrlMap.putAll(miscUrls);
        }
        return resultsUrlMap;
    }
    
    public Map<Integer,Document> compileResultsDocMap(Map<Object,String> resultsUrlPlusMiscMap) {
    	//shuffle urls before retrieving docs
    	Map<Integer,Document> resultsDocPlusMiscMap = new HashMap<>();
    	List<Object> keys = new ArrayList<>(resultsUrlPlusMiscMap.keySet());
    	Collections.shuffle(keys);
    	Integer previousKey = (Integer)keys.get(keys.size()-1);
    	for (Object k : keys) {
    		if (spiderWeb.getAttemptCount()<site.getMaxAttempts()) {
    			int page = (Integer) k;
    			if (page<=spiderWeb.getFurthestPageToCheck()) {
    				Document docToCheck = null;
    				String url = resultsUrlPlusMiscMap.get(k);
    				try {
    					//can we guarantee previous is a page that has access to the current?
    					Connection.Response response = connectionUtil.retrieveConnectionResponse(url, resultsUrlPlusMiscMap.get(previousKey), spiderWeb.getSessionCookies(), spiderWeb.getHeaders());
    					docToCheck = response.parse();
    					setCookies(response);
    				} catch (FileNotFoundException fnfe) {
    					logger.error("No html doc found for " + url);
    				} catch (IOException e) {
    					spiderWeb.increaseAttemptCount();
    					logger.error("Failed to get a connection to " + url, e);
    				}
    				//if docToCheck contains a crawledId, remember page number and don't add subsequent pages
    				//unless trying to retrieve missed records
    				if (spiderWeb.getFurthestPageToCheck()==9999 && !spiderWeb.retrieveMissedRecords()) {
    					for (String crawledId : spiderWeb.getCrawledIds()) {
    						if (docToCheck!=null && site.isAResultsDoc(docToCheck) && docToCheck.html().contains(crawledId)) {
    							//set as current page number
    							spiderWeb.setFurthestPageToCheck(page);
    						}
    					}
    				}
    				if (docToCheck!=null && !docToCheck.baseUri().equals(site.getBaseUrl()+"/")) {
    					resultsDocPlusMiscMap.put((Integer)k, docToCheck);
    				}
    				int sleepTime = ConnectionUtil.getSleepTime(site);
    				logger.debug("Sleeping for " + sleepTime + " milliseconds after fetching " + resultsUrlPlusMiscMap.get(k));
    				spiderUtil.sleep(sleepTime, false);

    				previousKey = page;
    			}

    		} else {
    			logger.error("Hit the limit of failed connections trying to compile results doc map");
    			return new HashMap<>();
    		}	

    	}
        //remove docs past this point here since we're not processing sequentially
        removeCrawledDocs(resultsDocPlusMiscMap, spiderWeb.getFurthestPageToCheck());
    	return resultsDocPlusMiscMap;
    }

    @Override
    public RecordIOUtil initializeIOUtil(String stateName) throws SpiderException {
    	RecordIOUtil ioUtil = new RecordIOUtil(stateName, new ArrestRecord(), site);
        try {
            //load previously written records IDs into memory
        	spiderWeb.setCrawledIds(ioUtil.getInputter().getCrawledIds());
        	if (spiderWeb.retrieveMissedRecords()) {
                spiderWeb.setUncrawledIds(ioUtil.getInputter().getUnCrawledIds());
            }
        	//load records in current spreadsheet into memory
            spiderWeb.setCrawledRecords(ioUtil.getInputter().readRecordsFromSheet(new File(ioUtil.getMainDocPath()),0));
            ioUtil.getOutputter().createWorkbook(ioUtil.getMainDocPath(), spiderWeb.getCrawledRecords(), true, ArrestDateComparator);
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return ioUtil;
    }

    public Map<Object,String> compileRecordDetailUrlMap(Document mainPageDoc, Map<Integer,Document> resultsDocPlusMiscMap) {
        Map<Object,String> recordDetailUrlMap = new HashMap<>();
        for (Map.Entry<Integer, Document> entry : resultsDocPlusMiscMap.entrySet()) {
            Document doc = entry.getValue();
            //only crawl for records if document was retrieved, is a results doc and has not already been crawled
            int page = entry.getKey();
            if (spiderUtil.docWasRetrieved(doc) && doc.baseUri().contains("&results=") && page<=spiderWeb.getFurthestPageToCheck()){
                logger.info("Gather complete list of records to scrape from " + doc.baseUri());
                recordDetailUrlMap.putAll(parseDocForUrls(doc));

                //include some non-detail page links
                if (spiderWeb.getMisc()) {
                    recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, recordDetailUrlMap.size()));
                }
            } else {
                logger.info("Nothing was retrieved for " + doc.baseUri());
            }
        }
        return recordDetailUrlMap;
    }

    public Document obtainRecordDetailDoc(String url, String referer) throws IOException {
		Connection.Response response = connectionUtil.retrieveConnectionResponse(url, referer, spiderWeb.getSessionCookies(), spiderWeb.getHeaders());
		setCookies(response);
    	return response.parse();
    }
    
    @Override
    public void setCookies(Connection.Response response) {
    	for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()) {
    		spiderWeb.addSessionCookie(cookieEntry.getKey(), cookieEntry.getValue());
            logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
		}

		if (spiderWeb.getRecordsProcessed() % spiderWeb.getRecordCap() == 0 && spiderWeb.getRecordsProcessed() != 0) {
    	    Map<String,String> sessionCookies = spiderWeb.getSessionCookies();
            sessionCookies.remove("PHPSESSID");
            //"__cfduid"? WTF does override security information mean?
            sessionCookies.remove("__cfduid");
			//every 200 or so records, cycle back to 1
			//TODO change IP?
			//these should only increment with results page views or new details pages, not layover details
			response.cookie("views_24", "1");
			response.cookie("views_session", "1");
			response.cookie("starttime_24", String.valueOf(Calendar.getInstance().getTime().getTime()));
			connectionUtil.changeUserAgent();
        }
    }
    
    @Override
    public void finalizeOutput(List<Record> arrestRecords) {
        //format the output
        if (!arrestRecords.isEmpty()) {
            logger.info("Starting to finalize the result output");
            Collections.sort(arrestRecords, CountyComparator);
            String columnDelimiter = RecordColumnEnum.COUNTY_COLUMN.getFieldName();
            Class<ArrestRecord> clazz = ArrestRecord.class;
            if (spiderWeb.getFilter()!=null && spiderWeb.getFilter()!=RecordFilterEnum.NONE) {
                try {
                    logger.info("Outputting filtered results");
                    List<Record> filteredRecords = filterRecords(arrestRecords);
                    RecordWorkbook splitRecords = Record.splitByField(filteredRecords, columnDelimiter, clazz);
                    //create a separate sheet with filtered results
                    logger.info(filteredRecords.size()+" "+spiderWeb.getFilter().filterName()+" records have been crawled");
                    if (!filteredRecords.isEmpty()) {
                      recordIOUtil.getOutputter().splitIntoSheets(recordIOUtil.getOutputter().getFilteredDocPath(spiderWeb.getFilter()), columnDelimiter, splitRecords, clazz, ArrestDateComparator);
                    }
                } catch (Exception e) {
                    logger.error("Error trying to create filtered spreadsheet", e);
                }
            }
            try {
                RecordWorkbook splitRecords = Record.splitByField(arrestRecords, columnDelimiter, clazz);
                recordIOUtil.getOutputter().splitIntoSheets(recordIOUtil.getMainDocPath(), columnDelimiter, splitRecords, clazz, ArrestDateComparator);
            } catch (Exception e) {
                logger.error("Error trying to split full list of records", e);
            }
        } else {
            logger.info("There were no records to output!!");
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
            arrestDate.set(Calendar.AM_PM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?Calendar.AM:Calendar.PM);
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
    	for (Record record : fullArrestRecords) {
    		boolean recordMatches = false;
	    		if (((ArrestRecord)record).getCharges()!=null) {
	                String[] charges = ((ArrestRecord)record).getCharges();
	                for (String charge : charges) {
	                    if (!recordMatches) {
	                        recordMatches = RecordFilter.filter(charge, spiderWeb.getFilter());
	                    }
	                }
	                if (recordMatches) {
	                    filteredArrestRecords.add(record);
	                }
	            }
    		}
    	return filteredArrestRecords;
    }
    
    public Map<Object,String> compileRecordDetailUrlMapFromBackup(Document mainPageDoc, Set<String> idList) {
        Map<Object,String> recordDetailUrlMap = new HashMap<>();
        for (String id : idList) {
            //list should only have uncrawled records but adding this check to be safe
            if (!spiderWeb.getCrawledIds().contains(id)) {
                recordDetailUrlMap.put(id, site.obtainDetailUrl(id));
            }
        }
        recordIOUtil.getUncrawledIdFile().delete();
        //include some non-detail page links
        if (spiderWeb.getMisc()) {
            recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, recordDetailUrlMap.size()));
        }
        return recordDetailUrlMap;
    }

	@Override
	public List<String> findAvailableCounties() {
		Document doc = null;
		if (spiderWeb.getAttemptCount()<=site.getMaxAttempts()) {
			try {
				logger.info("Trying to make initial connection to " + site.getName());
				doc = (Document) initiateConnection(site.getBaseUrl());
			} catch (IOException e) {
				logger.error("Couldn't make initial connection to site. Trying again " + (site.getMaxAttempts()-spiderWeb.getAttemptCount()) + " more times", e);
				if (e instanceof HttpStatusException && ((HttpStatusException) e).getStatusCode()==500) {
					connectionUtil = new ConnectionUtil(true);
				}
				spiderUtil.sleep(5000, true);
				spiderWeb.increaseAttemptCount();
				findAvailableCounties();
			}
		}
		return parseDocForCounties(doc);
	}
	
	private List<String> parseDocForCounties(Document doc) {
		List<String> countiesList = new ArrayList<>();
		if (doc!=null) {
            connectionEstablished = true;
			logger.info("Parsing counties with records for " + spiderWeb.getState().getName() + " in " + site.getName());
			Elements navigationList = doc.select("div.navigation ul li");
			Element countyListItem = null;
			for (Element li : navigationList) {
				if (li.text()!=null && li.text().contains("Counties")) {
					countyListItem = li;
				}
			}
			if (countyListItem!=null) {
				for (Element item : countyListItem.select("ul li")) {
					if (item.text()!=null)
						countiesList.add(item.text());
				}
			}
		}
		logger.info(countiesList.size() + " counties with records were found from " + site.getName());
		spiderUtil.sleep(3000, true);
		return countiesList;
	}

	private void removeCrawledDocs(Map<Integer,Document> docsMap, int furthestKey) {
        for(Map.Entry<Integer,Document> entry : docsMap.entrySet()) {
            if (entry.getKey()>furthestKey && site.isAResultsDoc(entry.getValue())) {
                docsMap.remove(entry.getKey());
            }
        }
    }
}
