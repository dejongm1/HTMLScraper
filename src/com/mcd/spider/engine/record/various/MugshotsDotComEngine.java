package com.mcd.spider.engine.record.various;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.MugshotsDotComSite;
import com.mcd.spider.exception.ExcelOutputException;
import com.mcd.spider.exception.IDCheckException;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.util.ConnectionUtil;
import com.mcd.spider.util.SpiderUtil;
import com.mcd.spider.util.io.RecordIOUtil;
import com.mcd.spider.util.io.RecordOutputUtil;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.mcd.spider.entities.record.ArrestRecord.ArrestDateComparator;

/**
 *
 * @author Michael De Jong
 *
 */

public class MugshotsDotComEngine implements ArrestRecordEngine {

    public static final Logger logger = Logger.getLogger(MugshotsDotComEngine.class);

    private SpiderUtil spiderUtil = new SpiderUtil();
    private ConnectionUtil connectionUtil;
    private MugshotsDotComSite site;
    private RecordIOUtil recordIOUtil;
    private SpiderWeb spiderWeb;
    List<Record> arrestRecords = new ArrayList<>();
    private boolean connectionEstablished = false;

    public MugshotsDotComEngine(SpiderWeb web) {
    	this.spiderWeb = web;
    	this.site = new MugshotsDotComSite(new String[]{web.getState().getName(), web.getState().getAbbreviation()});
        connectionUtil = new ConnectionUtil(true);
    }

    public MugshotsDotComEngine(String stateName, String countyName, String stateAbbreviation) {
    	this.site = new MugshotsDotComSite(new String[]{stateName, countyName, stateAbbreviation});
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

	@Override
	public void getArrestRecords() throws SpiderException{
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
	      //TODO loop through for each county? may need to adjust splitting if that's the case
		int maxAttempts = site.getMaxAttempts();
		for (String county : spiderWeb.getState().getCounties()) {
	        String firstCountyPageResultsUrl = site.generateResultsPageUrl(county);
	        Document mainPageDoc = null;
	        if (spiderWeb.getAttemptCount()<=maxAttempts) {
		        try {
		            logger.info("Trying to make initial connection to " + site.getName());
		            if (!connectionEstablished) {
		            	mainPageDoc = (Document) initiateConnection(firstCountyPageResultsUrl);
		            } else {
		            	//TODO don't use initiateConnection
		            }
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
	                Map<Object, String> recordDetailUrlMap;
	                //TODO implement uncrawled logic later
//	                if (spiderWeb.retrieveMissedRecords() && !spiderWeb.getUncrawledIds().isEmpty()) {
//	                    //create map from uncrawled records
//	                    logger.info("Generating details page urls from backup file");
//	
//	                    //build a list of details page urls by reading in uncrawled ids file
//	                    recordDetailUrlMap = compileRecordDetailUrlMapFromBackup(mainPageDoc, spiderWeb.getUncrawledIds());
//	                    logger.info("Gathered links for "+recordDetailUrlMap.size()+" uncrawled record profiles and misc pages");
//	
//	                } else {
	                    logger.info("Retrieving results page docs");
	                    Map<Integer, Document> resultsDocMap = compileResultsDocMap(mainPageDoc);
	                    
	                    if (resultsDocMap.isEmpty()) {
	                        logger.info("No results doc pages were gathered. Quitting");
	                        return;
	                    }
	
	                    logger.info("Retrieving details page urls");
	                    //build a list of details page urls by parsing results page docs
	                    recordDetailUrlMap = compileRecordDetailUrlMap(resultsDocMap);
	                    logger.info("Gathered links for "+recordDetailUrlMap.size()+" record profiles");
//	                }
	                spiderUtil.sleep(spiderWeb.isOffline()?0:ConnectionUtil.getSleepTime(site)*2, true);
	                //****iterate over collection
	                scrapeRecords(recordDetailUrlMap);
	            } else {
		            logger.error("Failed to load html doc from " + site.getBaseUrl()+ ". Trying again " + (maxAttempts-spiderWeb.getAttemptCount()) + " more times");
		            spiderWeb.increaseAttemptCount();
		            scrapeSite();
		        }
	        } else {
	        	logger.error("Too many attempts accessing " + site.getBaseUrl()+ ". Quitting.");
	        }
		}
		finalizeOutput(arrestRecords);
	}

    public Map<Integer,Document> compileResultsDocMap(Document mainPageDoc) {
		//build docs map by traversing next button to get total pages/records
    	//TODO should we add misc pages here to make crawling appear random?
		Integer numberOfPages = 0;
		Map<Integer,Document> resultsDocMap = new HashMap<>();
		String nextPageUrl = site.getNextResultsPageUrl(mainPageDoc);
		String currentPageUrl = mainPageDoc.baseUri();
		while (nextPageUrl!=null) {
			numberOfPages++;
			Document docToCheck = null;
			try {
				Connection.Response response = connectionUtil.retrieveConnectionResponse(nextPageUrl, currentPageUrl, spiderWeb.getSessionCookies(), spiderWeb.getHeaders());
				docToCheck = response.parse();
				setCookies(response);
				resultsDocMap.put(numberOfPages, docToCheck);
			} catch (FileNotFoundException fnfe) {
				logger.error("No html doc found for " + nextPageUrl);
			} catch (IOException e) {
				spiderWeb.increaseAttemptCount();
				logger.error("Failed to get a connection to " + nextPageUrl, e);
			}
			currentPageUrl = nextPageUrl;
			nextPageUrl = site.getNextResultsPageUrl(mainPageDoc);
		}
        spiderWeb.setNumberOfPages(numberOfPages);
        return resultsDocMap;
	}
	
	@Override
	public Map<String, String> parseDocForUrls(Object objectToParse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void scrapeRecords(Map<Object, String> recordsDetailsUrlMap) {
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
//                            if (arrestRecord.getState()==null || arrestRecord.getState().equalsIgnoreCase(spiderWeb.getState().getName())) {
                                arrestRecords.add(arrestRecord);
                                //save each record in case of failures mid-crawling
                                recordOutputUtil.addRecordToMainWorkbook(arrestRecord);
                                //"remove" record from recordsDetailUrlMap
                                recordsDetailsUrlMap.replace(k, "CRAWLED" + recordsDetailsUrlMap.get(k));
                                logger.debug("Record " + spiderWeb.getRecordsProcessed() + " saved");
//                            }
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

                previousKey = String.valueOf(k);
            }
        }
	}

	@Override
	public ArrestRecord populateArrestRecord(Object profileDetailObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void matchPropertyToField(ArrestRecord record, Object profileDetail) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object initiateConnection(String url) throws IOException{
        Connection.Response response;
        if (!connectionEstablished) {
            response = connectionUtil.retrieveConnectionResponse(url, "www.google.com");
            for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()){
                logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
            }
            Map<String,String> headers = new HashMap<>();
            headers.put("Host", "mugshots.com");
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            headers.put("Accept-Language", "en-US,en;q=0.5");
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("Connection", "keep-alive");
            headers.put("Upgrade-Insecure-Requests", "1");
            headers.put("Cache-Control", "max-age=0");
            spiderWeb.setHeaders(headers);
        } else {
            response = connectionUtil.retrieveConnectionResponse(url, "www.google.com", spiderWeb.getSessionCookies(), spiderWeb.getHeaders());
        }
        spiderWeb.setSessionCookies(response.cookies());
        return response.parse();
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

    public Map<Object,String> compileRecordDetailUrlMap(Map<Integer,Document> resultsDocMap) {
        Map<Object,String> recordDetailUrlMap = new HashMap<>();
        for (Map.Entry<Integer, Document> entry : resultsDocMap.entrySet()) {
            Document doc = entry.getValue();
            //only crawl for records if document was retrieved and has not already been crawled
            int page = entry.getKey();
            if (spiderUtil.docWasRetrieved(doc) && page<=spiderWeb.getFurthestPageToCheck()){
                logger.info("Gather complete list of records to scrape from " + doc.baseUri());
                recordDetailUrlMap.putAll(parseDocForUrls(doc));

                /*//include some non-detail page links
                if (spiderWeb.getMisc()) {
                    recordDetailUrlMap.putAll(site.getMiscSafeUrlsFromDoc(mainPageDoc, recordDetailUrlMap.size()));
                }*/
            } else {
                logger.info("Nothing was retrieved for " + doc.baseUri());
            }
        }
        return recordDetailUrlMap;
    }

	@Override
	public void setCookies(Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finalizeOutput(List<Record> arrestRecords) {
		// TODO Auto-generated method stub

	}

	@Override
	public void formatName(ArrestRecord record, Element profileDetail) {
		// TODO Auto-generated method stub

	}

	@Override
	public void formatArrestTime(ArrestRecord record, Element profileDetail) {
		// TODO Auto-generated method stub

	}

	@Override
	public String extractValue(Element profileDetail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Record> filterRecords(List<Record> fullArrestRecords) {
		// TODO Auto-generated method stub
		return null;
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
			logger.info("Parsing counties with records for " + site.getName());
			Elements countyItems = doc.select("#main .box #subcategories ul.categories li");
			//skip class="zero" or text().endsWith("(0)") because it's an empty county
			for (Element countyItem : countyItems) {
				if (!countyItem.hasClass("zero")
						&& countyItem.child(0)!=null && countyItem.child(0).text()!=null
						&& !countyItem.child(0).text().endsWith("(0)") && countyItem.text().contains(" County, ")) {
					countiesList.add(countyItem.text().substring(0, countyItem.text().indexOf(" County,")));
				}
			}
		}
		logger.info(countiesList.size() + " counties with records were found from " + site.getName());
		spiderUtil.sleep(3000, true);
		return countiesList;
	}
}
