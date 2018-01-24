package com.mcd.spider.engine.record.various;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.entities.site.html.MugshotsDotComSite;
import com.mcd.spider.exception.SpiderException;
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
//
//    public MugshotsDotComEngine(SpiderWeb web) {
//    	this.spiderWeb = web;
//    	this.site = new MugshotsDotComSite(new String[]{web.getState().getName(), county, web.getState().getAbbreviation()});
//        connectionUtil = new ConnectionUtil(true);
//    }

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
	public void getArrestRecords(String stateName) throws SpiderException {
		long totalTime = System.currentTimeMillis();
      recordIOUtil = initializeIOUtil(stateName);

      logger.info("----Site: " + site.getName() + "-" + stateName + "----");
      logger.debug("Sending spider " + (spiderWeb.isOffline()?"offline":"online" ));
      
      int sleepTimeAverage = spiderWeb.isOffline()?0:(site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2000;
      
      scrapeSite();

      spiderUtil.sendEmail(stateName);
      
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
		int maxAttempts = site.getMaxAttempts();
        String firstPageResultsUrl = site.generateFirstResultsPageUrl();
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
                Map<Object, String> recordDetailUrlMap;
                if  (spiderWeb.retrieveMissedRecords() && !spiderWeb.getUncrawledIds().isEmpty()) {
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
	public Object initiateConnection(String arg) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordIOUtil initializeIOUtil(String stateName) throws SpiderException {
		// TODO Auto-generated method stub
		return null;
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

}
