package com.mcd.spider.main.engine.record.iowa;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.ArrestRecord;
import com.mcd.spider.main.entities.Record;
import com.mcd.spider.main.entities.State;
import com.mcd.spider.main.entities.service.DesMoinesRegisterComService;
import com.mcd.spider.main.entities.site.DesMoinesRegisterComSite;
import com.mcd.spider.main.entities.site.Site;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void getArrestRecords(State state, long maxNumberOfResults) {
        logger.debug("Sending spider " + (System.getProperty("offline").equals("true")?"offline":"online" ));
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
        //Site[] sites = state.getSites();
        ExcelWriter excelWriter  = new ExcelWriter(state, new ArrestRecord());
        excelWriter.createSpreadhseet();
//        for(Site site : sites){
        DesMoinesRegisterComSite site = new DesMoinesRegisterComSite();
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
        stateTime = System.currentTimeMillis() - stateTime;
        logger.info(state.getName() + " took " + stateTime + " ms");

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

    @Override
    public int scrapeSite(State state, Site site, ExcelWriter excelWriter) {
        int recordsProcessed = 0;

        Map<String, String> profileDetailUrlMap = new HashMap<>();
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
                //try something other than getmugs
                String urlParameters = service.getRequestBody(new String[]{county, "getmugs", "0", "10000"});

                // Send post request
                logger.debug("\nSending 'POST' request to URL : " + service.getUrl());
                StringBuffer response = sendRequest(con, urlParameters);

                profileDetailUrlMap.putAll(parseDocForUrls(response, site));

                scrapeRecords(profileDetailUrlMap, site, excelWriter);

            } catch (java.io.IOException e) {
                logger.error("IOException caught sending http request to " + service.getUrl(), e);
            }

        }
        return 0;
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
                detailUrlMap.put(mugshot.getString("id"), site.getBaseUrl(null) + "&id=" + mugshot.getString("id"));
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
                        try {
                            arrestRecords.add(populateArrestRecord(profileDetailDoc, site));
                            //save each record in case of failures
                            excelWriter.saveRecordToWorkbook(arrestRecord);
                            int sleepTime = ConnectionUtil.getSleepTime(site);
                            logger.debug("Sleeping for: " + sleepTime);
                            Thread.sleep(sleepTime);//sleep at random interval
                        } catch (InterruptedException ie) {
                            logger.error(ie);
                        }
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
        excelWriter.saveRecordsToWorkbook(arrestRecords);
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
