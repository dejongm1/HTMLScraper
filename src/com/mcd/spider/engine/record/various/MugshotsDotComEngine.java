package com.mcd.spider.engine.record.various;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.MugshotsDotComSite;
import com.mcd.spider.util.ConnectionUtil;
import com.mcd.spider.util.SpiderUtil;
import com.mcd.spider.util.io.RecordIOUtil;

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
	public void getArrestRecords() {
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
	                    //got here 1/26/18 @2:05
	                    
	                    if (resultsDocMap.isEmpty()) {
	                        logger.info("No results doc pages were gathered. Quitting");
	                        return;
	                    }
	
	                    logger.info("Retrieving details page urls");
	                    //build a list of details page urls by parsing results page docs
	                    recordDetailUrlMap = compileRecordDetailUrlMap(mainPageDoc, resultsDocMap);
	                    logger.info("Gathered links for "+recordDetailUrlMap.size()+" record profiles");
//	                }
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
		// TODO Auto-generated method stub

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
	public Object initiateConnection(String firstResultsPageUrl) throws IOException{
		Connection.Response response = connectionUtil.retrieveConnectionResponse(firstResultsPageUrl, "www.google.com");
        for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()){
            logger.debug(cookieEntry.getKey() + "=" + cookieEntry.getValue());
        }
        Map<String,String> headers = new HashMap<>();
//        headers.put("Host", "iowa.arrests.org");
//        headers.put("Accept", "text/html, */*; q=0.01");
//        headers.put("Accept-Language", "en-US,en;q=0.5  ");
//        headers.put("Accept-Encoding", "gzip, deflate, br");
//        headers.put("X-fancyBox", "true");
//        headers.put("X-Requested-With", "XMLHttpRequest");
//        headers.put("Connection", "keep-alive");
        spiderWeb.setHeaders(headers);
        spiderWeb.setSessionCookies(response.cookies());
        return response.parse();
	}

	@Override
	public RecordIOUtil initializeIOUtil(String stateName) {
		// TODO Auto-generated method stub
		return null;
	}

    public Map<Object,String> compileRecordDetailUrlMap(Document mainPageDoc, Map<Integer,Document> resultsDocPlusMiscMap) {
        Map<Object,String> recordDetailUrlMap = new HashMap<>();
        /*for (Map.Entry<Integer, Document> entry : resultsDocPlusMiscMap.entrySet()) {
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
        }*/
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
