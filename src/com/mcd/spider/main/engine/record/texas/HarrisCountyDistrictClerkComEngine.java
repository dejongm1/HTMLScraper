package com.mcd.spider.main.engine.record.texas;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.ArrestRecordFilter;
import com.mcd.spider.main.entities.record.filter.ArrestRecordFilter.ArrestRecordFilterEnum;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.entities.site.service.DesMoinesRegisterComSite;
import com.mcd.spider.main.entities.site.service.HarrisCountyDistrictClerkComSite;
import com.mcd.spider.main.entities.site.service.SiteService;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.ExcelWriter;
import com.mcd.spider.main.util.SpiderUtil;

import common.Logger;

/**
 *
 * @author MikeyDizzle
 *
 */
public class HarrisCountyDistrictClerkComEngine implements CourtRecordEngine{

    private static final Logger logger = Logger.getLogger(HarrisCountyDistrictClerkComEngine.class);
    private SpiderUtil spiderUtil = new SpiderUtil();
    private EngineUtil engineUtil = new EngineUtil();
    private Set<String> crawledIds;
    private ArrestRecordFilterEnum filter;
    private boolean offline;

    @Override
    public Site getSite(String[] args) {
    	return new HarrisCountyDistrictClerkComSite();
    }
    
    @Override
    public void getArrestRecords(State state, long maxNumberOfResults, ArrestRecordFilter.ArrestRecordFilterEnum filter) throws SpiderException {
        offline = System.getProperty("offline").equals("true");
    	this.filter = filter;
        //split into more specific methods
        long totalTime = System.currentTimeMillis();
        long recordsProcessed = 0;
        int sleepTimeSum = 0;

        //while(recordsProcessed <= maxNumberOfResults) {
        HarrisCountyDistrictClerkComSite site = (HarrisCountyDistrictClerkComSite) getSite(null);
        ExcelWriter excelWriter = initializeOutputter(state, site);
        
        logger.info("----Site: " + site.getName() + "----");
        logger.debug("Sending spider " + (offline?"offline":"online" ));
        
        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
        sleepTimeSum += offline?0:sleepTimeAverage;
        long time = System.currentTimeMillis();
        
        recordsProcessed += scrapeSite(state, site, excelWriter, 1);
        
        time = System.currentTimeMillis() - time;
        logger.info(site.getBaseUrl() + " took " + time + " ms");

        //excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});

        spiderUtil.sendEmail(state);
        
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
    public int scrapeSite(State state, Site site, ExcelWriter excelWriter, int attemptCount) {
        int recordsProcessed = 0;
    	int maxAttempts = site.getMaxAttempts();
        SiteService siteService = (HarrisCountyDistrictClerkComSite) site;
        site.setBaseUrl(null);
        String firstPageResults = siteService.getBaseUrl();
        //Add some retries if first connection to state site fails?
        Document mainPageDoc = spiderUtil.getHtmlAsDoc(firstPageResults);
        if (engineUtil.docWasRetrieved(mainPageDoc) && attemptCount<=maxAttempts) {
        	String hiddenDownloadFile = "";
        	Elements tags = mainPageDoc.select("#hiddenDownloadFile");
        	for (Element tag : tags) {
        		if (tag.attr("value")!=null && tag.attr("value").contains("CrimFilingsDaily")) {
        			hiddenDownloadFile = tag.attr("value");
        		}
        	}
	        try {
	            //build http post request
	            URL obj = new URL(((DesMoinesRegisterComSite) siteService).getServiceUrl());
	            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
	            //add request header
	            con.setRequestMethod(siteService.getRequestType());
	            con.setRequestProperty("User-Agent", siteService.getUserAgent());
	            con.setRequestProperty("Accept-Language", siteService.getAcceptLanguage());
	            con.setRequestProperty("Accept-Encoding", siteService.getAcceptEncoding());
	            con.setRequestProperty("Accept", siteService.getAcceptType());
	            con.setRequestProperty("Content-Length", siteService.getContentLength());
	            con.setRequestProperty("Content-Type", siteService.getContentType());
	            con.setRequestProperty("Cookie", siteService.getCookie());
	            con.setRequestProperty("Referer", siteService.getReferer());
	            con.setRequestProperty("Proxy-Connection", siteService.getProxyConnection());
	            con.setRequestProperty("Host", siteService.getHost());
	            con.setRequestProperty("Pragma", siteService.getHost());
	            con.setRequestProperty("Cache-Control", siteService.getHost());
	            con.setRequestProperty("Origin", siteService.getHost());
	            con.setRequestProperty("Upgrade-Insecure-Requests", siteService.getHost());
	            
	            String urlParameters = siteService.getRequestBody(new String[]{hiddenDownloadFile});
	
	            // Send post request
	            logger.debug("\nSending 'POST' request to URL : " + site.getUrl());
	            StringBuffer response = sendRequest(con, urlParameters);
	
	            //parse the returned document, generating a map with ids and the line contents
	            Map<String, String> detailMap;
	            detailMap = parseDocForUrls(response, site);
                
	            recordsProcessed += scrapeRecords(detailMap, site, excelWriter);
	
	        } catch (java.io.IOException e) {
	            logger.error("IOException caught sending http request to " + site.getUrl(), e);
	        }
        }
    
        return recordsProcessed;
    }

    @Override
    public Map<String, String> parseDocForUrls(Object response, Site site){
        Map<String, String> detailMap = new HashMap<>();
        //read line in 
        for(Object obj : (List<String>) response) {//for each line in Response
        	String line = ""; //whole line
        	String id = site.generateRecordId(line);
            //only add if we haven't already crawled it
            if (!crawledIds.contains(id)) {
            	detailMap.put(id, line);
            }
        }
        return detailMap;
    }

    @Override
    public int scrapeRecords(Map<String, String> recordsDetailsMap, Site site, ExcelWriter excelWriter){
        int recordsProcessed = 0;
        List<ArrestRecord> arrestRecords = new ArrayList<>();
        ArrestRecord arrestRecord;
        for (Map.Entry<String,String> entry : recordsDetailsMap.entrySet()) {
            String id = entry.getKey();
            String detailString = recordsDetailsMap.get(id);
            logger.debug("\nParsing record details for ID: " + id);

            if (!detailString.equals("")) {
                    recordsProcessed++;
                arrestRecord = populateArrestRecord(entry, site);
                arrestRecords.add(arrestRecord);
                //save each record in case of application failures
                excelWriter.addRecordToMainWorkbook(arrestRecord);
                spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
                
            } else {
                logger.error("Failed to load details from " + id);
            }
            
        }
        
        if (filter!=null) {
	        List<Record> filteredRecords = filterRecords(arrestRecords);
	        //create a separate sheet with filtered results
	        logger.info(filteredRecords.size() + " " + filter.filterName() + " " + "records were crawled");
        }
        return recordsProcessed;
    }

    @SuppressWarnings("unchecked")
	@Override
    public CourtRecord populateArrestRecord(Object profileDetailObj, Site site) {
    	String[] detailList = ((Map.Entry<String,String>) profileDetailObj).getValue().split("\t");
        CourtRecord record = new ArrestRecord();
        record.setId(((Map.Entry<String,String>) profileDetailObj).getKey());
        matchPropertyToField(record, detailList);
        logger.info("\t" + ((Map.Entry<String,String>)profileDetailObj).getValue());
        
        return record;
    }

    @Override
    public ExcelWriter initializeOutputter(State state, Site site) throws SpiderException {
    	ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord(), site);
        try {
            crawledIds = excelWriter.getPreviousIds();
            excelWriter.createSpreadsheet();
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return excelWriter;
    }
    
    @Override
    public void matchPropertyToField(CourtRecord record, Object profileDetail) {
    	String[] profileDetaiLString = (String[]) profileDetail;
        formatArrestTime(record, profileDetail);
        record.setArrestAge(Integer.parseInt(extractValue(profileDetail)));//birthdate 21
        record.setCharges(extractValue(profileDetail).split(";"));
        record.setGender(extractValue(profileDetail));
		record.setCity(city);
		record.setState("TX");
        String bondAmount = extractValue(profileDetail);
        int totalBond = Integer.parseInt(bondAmount.replace("$", "").replace(",", ""));
        record.setTotalBond(totalBond); //9
        record.setHeight(extractValue(profileDetail));
        record.setWeight(extractValue(profileDetail));
        record.setHairColor(extractValue(profileDetail));
        record.setEyeColor(extractValue(profileDetail));
        record.setCounty(extractValue(profileDetail));
        record.setBirthPlace(birthPlace);
    }

    @Override
    public void formatName(ArrestRecord record, Element profileDetail) {
    	
    }

    @Override
    public String extractValue(Element profileDetail) {
        return null;
    }

    @Override
    public void formatArrestTime(ArrestRecord record, Element profileDetail) {
        String arrestDate = extractValue(profileDetail).replace("at", "");
        try {
            //replace today
            Date date = new Date(arrestDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            record.setArrestDate(calendar);
        } catch (Exception e) {
            logger.error("Error converting " + arrestDate + " for record id " + record.getId());
        }
    }

    public StringBuffer sendRequest(HttpURLConnection con, String urlParameters) throws IOException {
        //make an offline version?
    	con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        logger.debug("Post parameters : " + urlParameters);
        logger.debug("Response Code : " + con.getResponseCode());

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response;
    }
    
    @Override
    public List<Record> filterRecords(List<ArrestRecord> fullArrestRecords) {
    	List<Record> filteredArrestRecords = new ArrayList<>();
    	for (Record record : fullArrestRecords) {
    		boolean recordMatches = false;
    		String[] charges = ((ArrestRecord) record).getCharges();
    		for (String charge : charges) {
    			if (!recordMatches) {
    				recordMatches = ArrestRecordFilter.filter(charge, filter);
    			}
    		}
    		if (recordMatches) {
    			filteredArrestRecords.add(record);
    		}
    	}
    	return filteredArrestRecords;
    }
}
