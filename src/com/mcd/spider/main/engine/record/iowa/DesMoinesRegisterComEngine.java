package com.mcd.spider.main.engine.record.iowa;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.service.DesMoinesRegisterComService;
import com.mcd.spider.main.entities.site.DesMoinesRegisterComSite;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.ExcelWriter;
import com.mcd.spider.main.util.SpiderUtil;
import common.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 *
 * @author MikeyDizzle
 *
 */
public class DesMoinesRegisterComEngine implements ArrestRecordEngine{

    private static final Logger logger = Logger.getLogger(DesMoinesRegisterComEngine.class);
    private SpiderUtil spiderUtil = new SpiderUtil();
    private EngineUtil engineUtil = new EngineUtil();
    DesMoinesRegisterComService service = new DesMoinesRegisterComService();
    private Set<String> scrapedIds;

    public Site getSite() {
    	return new DesMoinesRegisterComSite();
    }
    
    @Override
    public void getArrestRecords(State state, long maxNumberOfResults) throws ExcelOutputException, IDCheckException {
    	if ((System.getProperty("offline").equals("true"))) {
    		logger.debug("Offline - can't scrape this php site");
    	} else {
	        //split into more specific methods
	        long totalTime = System.currentTimeMillis();
	        long recordsProcessed = 0;
	        int sleepTimeSum = 0;
	        int sitesScraped = 0;
	
	        //use maxNumberOfResults to stop processing once this method has been broken up
	        //this currently won't stop a single site from processing more than the max number of records
	        //while(recordsProcessed <= maxNumberOfResults) {
	
	        long siteTime = System.currentTimeMillis();
	        logger.info("----State: " + state.getName() + "----");
	        logger.debug("Sending spider " + (System.getProperty("offline").equals("true")?"offline":"online" ));
	        //Site[] sites = state.getSites();
	//        for(Site site : sites){
	        DesMoinesRegisterComSite site = new DesMoinesRegisterComSite();
	        ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord(), site);
            try {
                //this will get previously written IDs but then overwrite the spreadsheet
                scrapedIds = excelWriter.getPreviousIds();
                excelWriter.createSpreadsheet();
            } catch (ExcelOutputException | IDCheckException e) {
                throw e;
            }
	        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
	        sleepTimeSum += spiderUtil.offline()?0:sleepTimeAverage;
	        long time = System.currentTimeMillis();
	        recordsProcessed += scrapeSite(state, site, excelWriter);
	        sitesScraped++;
	        time = System.currentTimeMillis() - time;
	        logger.info(site.getBaseUrl(new String[]{state.getName()}) + " took " + time + " ms");
	//        }
	
	        //remove ID column on final save?
	        //or use for future processing? check for ID and start where left off
	        //excelWriter.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
	        siteTime = System.currentTimeMillis() - siteTime;
	        logger.info(site.getName() + " took " + siteTime + " ms");
	
	        //extract to util class
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
    }

    @Override
    public int scrapeSite(State state, Site site, ExcelWriter excelWriter) {
        int recordsProcessed = 0;

        for (String county : ((DesMoinesRegisterComSite)site).getCounties()) {
            site.getBaseUrl(new String[]{county});
            try {
                //build http post request
                URL obj = new URL(service.getUrl());
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add request header
                con.setRequestMethod(service.getRequestType());
                con.setRequestProperty("User-Agent", service.getUserAgent());
                con.setRequestProperty("Accept-Language", service.getAcceptLanguage());
                con.setRequestProperty("Accept-Encoding", service.getAcceptEncoding());
                con.setRequestProperty("Accept", service.getAcceptType());
                con.setRequestProperty("Content-Length", service.getContentLength());
                con.setRequestProperty("Content-Type", service.getContentType());
                con.setRequestProperty("Cookie", service.getCookie());
                //con.setRequestProperty("Referer", service.getReferer());
                con.setRequestProperty("XRequested-With", service.getXRequestedWith());
                con.setRequestProperty("Host", service.getHost());

                //iterate over counties and add to list
//                String urlParameters = service.getRequestBody(new String[]{county, "getmugs", "0", "10000"});
                String urlParameters = service.getRequestBody(new String[]{county, "getmugs", "0", "12"});

                // Send post request
                logger.debug("\nSending 'POST' request to URL : " + service.getUrl());
                StringBuffer response = sendRequest(con, urlParameters);

                Map<String, String> profileDetailUrlMap = new HashMap<>();
                profileDetailUrlMap.putAll(parseDocForUrls(response, site));

                recordsProcessed += scrapeRecords(profileDetailUrlMap, site, excelWriter);

            } catch (java.io.IOException e) {
                logger.error("IOException caught sending http request to " + service.getUrl(), e);
            }

        }
        return recordsProcessed;
    }

    @Override
    public Map<String, String> parseDocForUrls(Object response, Site site){
        Map<String, String> detailUrlMap = new HashMap<>();
        try {
            JSONObject json = new JSONObject(response.toString());
            JSONArray mugShots = json.getJSONArray("mugs");
            for(int m=0;m<mugShots.length();m++) {
                //not sure if the url will work, might need to call service
                JSONObject mugshot = (JSONObject) mugShots.get(m);
                //only add it if we haven't already scraped it
                if (!scrapedIds.contains(mugshot.getString("id"))) {
                    detailUrlMap.put(mugshot.getString("id"), site.getBaseUrl(null) + "&id=" + mugshot.getString("id"));
                }
                //http://data.desmoinesregister.com/iowa-mugshots/index.php?co=Polk&id=113936
            }
        } catch (JSONException e) {
            logger.error("JSONExecption caught", e);
        }
        return detailUrlMap;
    }

    @Override
    public int scrapeRecords(Map<String, String> recordsDetailsUrlMap, Site site, ExcelWriter excelWriter){
        int recordsProcessed = 0;
        List<Record> arrestRecords = new ArrayList<>();
        Record arrestRecord = new ArrestRecord();
//        List<String> keys = new ArrayList<>(recordsDetailsUrlMap.keySet());
//        Collections.shuffle(keys);
//        for (String k : keys) {
        for (Map.Entry<String,String> entry : recordsDetailsUrlMap.entrySet()) {
            String id = entry.getKey();
            String url = recordsDetailsUrlMap.get(id);
            try {
                logger.debug("\nSending 'POST' request for : " + id);
                Document profileDetailDoc = service.getDetailsDoc(url, this);

                if (engineUtil.docWasRetrieved(profileDetailDoc)) {
                    if (site.isARecordDetailDoc(profileDetailDoc)) {
                        recordsProcessed++;
                        //should we check for ID first or not bother unless we see duplicates??
                        arrestRecord = populateArrestRecord(profileDetailDoc, site);
                        arrestRecords.add(arrestRecord);
                        //save each record in case of failures
                        excelWriter.addRecordToWorkbook(arrestRecord);
                        spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
                        
                    } else {
                        logger.debug("This doc doesn't have any record details: " + id);
                    }
                } else {
                    logger.error("Failed to load html doc from " + url);
                }
            } catch (IOException e) {
                logger.error("IOException caught sending http request to " + url, e);
            } catch (JSONException e) {
                logger.error("JSONExecption caught on id " + id, e);
            }
        }
        //save the whole thing at the end
        //order?? and save to overwrite the spreadsheet
        //this copies, not overwrites
        //excelWriter.saveRecordsToWorkbook(arrestRecords);
        return recordsProcessed;
    }

    @Override
    public ArrestRecord populateArrestRecord(Document profileDetailDoc, Site site) {
        Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.getRecordId(profileDetailDoc.select("#permalink-url a").get(0).attr("href")));
        //made it here
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.info("\t" + profileDetail.text());
        }
        return record;
    }

    @Override
    public void matchPropertyToField(ArrestRecord record, Element profileDetail) {
        String label = profileDetail.select("strong").text().toLowerCase();
        if (!label.equals("")) {
            try {
                if (label.contains("booked")) {
                    formatArrestTime(record, profileDetail);
                } else if (label.contains("age")) {
                    record.setArrestAge(Integer.parseInt(extractValue(profileDetail)));
                } else if (label.contains("charges")) {
                    record.setCharges(extractValue(profileDetail).split(";"));
                } else if (label.contains("sex")) {
                    record.setGender(extractValue(profileDetail));
                } else if (label.contains("city")) {
//					record.setCity(city);
					record.setState("IA");
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
                } else if (label.contains("eye")) {
                    record.setEyeColor(extractValue(profileDetail));
                } else if (label.contains("county")) {
                    record.setCounty(extractValue(profileDetail));
                }
            } catch (NumberFormatException nfe) {
                logger.error("Couldn't parse a numeric value from " + profileDetail.text());
            }
        } else if (profileDetail.select("h1").hasText()) {
            record.setFullName(profileDetail.select("h1").text().trim());
        }
    }

    @Override
    public void formatName(ArrestRecord record, Element profileDetail) {

    }

    @Override
    public String extractValue(Element profileDetail) {
        return profileDetail.text().substring(profileDetail.text().indexOf(':')+1).trim();
    }

    @Override
    public void formatArrestTime(ArrestRecord record, Element profileDetail) {
        String arrestDate = extractValue(profileDetail).replace("at", "");
        try {
            //replace today
            if (!arrestDate.toLowerCase().contains("today")) {
                Date date = new Date(arrestDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                record.setArrestDate(calendar);
            } else {
                String hour = arrestDate.substring(arrestDate.toLowerCase().indexOf("today") + 5, arrestDate.indexOf(':')).trim();
                String minutes = arrestDate.substring(arrestDate.indexOf(':')+1, arrestDate.length()-3).trim();
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR, Integer.parseInt(hour));
                calendar.set(Calendar.MINUTE, Integer.parseInt(minutes));
                record.setArrestDate(calendar);
            }
        } catch (Exception iae) {
            logger.error("Error converting " + arrestDate + " for record id " + record.getId());
        }
    }

    public StringBuffer sendRequest(HttpURLConnection con, String urlParameters) throws IOException {
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
}
