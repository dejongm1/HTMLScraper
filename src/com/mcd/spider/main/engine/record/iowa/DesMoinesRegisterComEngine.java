package com.mcd.spider.main.engine.record.iowa;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.entities.site.service.DesMoinesRegisterComSite;
import com.mcd.spider.main.entities.site.service.SiteService;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.OutputUtil;
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
import java.util.Map.Entry;

/**
 *
 * @author MikeyDizzle
 *
 */
public class DesMoinesRegisterComEngine implements ArrestRecordEngine{

    private static final Logger logger = Logger.getLogger(DesMoinesRegisterComEngine.class);
    private SpiderUtil spiderUtil = new SpiderUtil();
    private Set<String> crawledIds;
    private RecordFilterEnum filter;
    private boolean offline;

    @Override
    public Site getSite(String[] args) {
    	return new DesMoinesRegisterComSite(args);
    }
    
    @Override
    public void getArrestRecords(State state, long maxNumberOfResults, RecordFilterEnum filter) throws SpiderException {
        offline = System.getProperty("offline").equals("true");
    	this.filter = filter;
    	if (offline) {
    		logger.debug("Offline - can't scrape this php site. Try making an offline version");
    	} else {
	        //split into more specific methods
	        long totalTime = System.currentTimeMillis();
	        long recordsProcessed = 0;
	        int sleepTimeSum = 0;
	
	        //while(recordsProcessed <= maxNumberOfResults) {
	        DesMoinesRegisterComSite site = (DesMoinesRegisterComSite) getSite(null);
            OutputUtil outputUtil = initializeOutputter(state, site);
            
	        logger.info("----Site: " + site.getName() + "----");
	        logger.debug("Sending spider " + (offline?"offline":"online" ));
	        
	        int sleepTimeAverage = (site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;
	        sleepTimeSum += offline?0:sleepTimeAverage;
	        long time = System.currentTimeMillis();
	        
	        recordsProcessed += scrapeSite(site, outputUtil, 1, maxNumberOfResults);
	        
	        time = System.currentTimeMillis() - time;
	        logger.info(site.getBaseUrl() + " took " + time + " ms");

	        //outputUtil.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});
	
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
    }

    @Override
    public int scrapeSite(Site site, OutputUtil outputUtil, int attemptCount, long maxNumberOfResults) {
        int recordsProcessed = 0;
        SiteService serviceSite = (DesMoinesRegisterComSite) site;
        for (String county : ((DesMoinesRegisterComSite) site).getCounties()) {
            site.setBaseUrl(new String[]{county});
            try {
                //build http post request
                URL obj = new URL(serviceSite.getServiceUrl());
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add request header
                con.setRequestMethod(serviceSite.getRequestType());
                con.setRequestProperty("User-Agent", serviceSite.getUserAgent());
                con.setRequestProperty("Accept-Language", serviceSite.getAcceptLanguage());
                con.setRequestProperty("Accept-Encoding", serviceSite.getAcceptEncoding());
                con.setRequestProperty("Accept", serviceSite.getAcceptType());
                con.setRequestProperty("Content-Length", serviceSite.getContentLength());
                con.setRequestProperty("Content-Type", serviceSite.getContentType());
                con.setRequestProperty("Cookie", serviceSite.getCookie());
                //con.setRequestProperty("Referer", serviceSite.getReferer());
                con.setRequestProperty("XRequested-With", ((DesMoinesRegisterComSite) serviceSite).getXRequestedWith());
                con.setRequestProperty("Proxy-Connection", serviceSite.getProxyConnection());
                con.setRequestProperty("Host", serviceSite.getHost());
//                String urlParameters = serviceSite.getRequestBody(new String[]{county, "getmugs", "0", "10000"});
                String urlParameters = serviceSite.getRequestBody(new String[]{county, "getmugs", "0", String.valueOf(maxNumberOfResults/3)});

                // Send post request
                logger.debug("\nSending 'POST' request to URL : " + site.getUrl().getDomain());
                StringBuffer response = sendRequest(con, urlParameters);

                Map<Object, String> profileDetailUrlMap = new HashMap<>();
                profileDetailUrlMap.putAll(parseDocForUrls(response, site));

                recordsProcessed += scrapeRecords(profileDetailUrlMap, site, outputUtil, null);

            } catch (java.io.IOException e) {
                logger.error("IOException caught sending http request to " + site.getUrl(), e);
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
                JSONObject mugshot = (JSONObject) mugShots.get(m);
                //only add if we haven't already crawled it
                if (!crawledIds.contains(mugshot.getString("id"))) {
                    detailUrlMap.put(mugshot.getString("id"), site.getBaseUrl() + "&id=" + mugshot.getString("id"));
                }
            }
        } catch (JSONException e) {
            logger.error("JSONExecption caught", e);
        }
        return detailUrlMap;
    }

    @Override
    public int scrapeRecords(Map<Object, String> recordsDetailsUrlMap, Site site, OutputUtil outputUtil, Map<String,String> cookies){
        int recordsProcessed = 0;
        List<Record> arrestRecords = new ArrayList<>();
        ArrestRecord arrestRecord;
        for (Entry<Object, String> entry : recordsDetailsUrlMap.entrySet()) {
            String id = (String) entry.getKey();
            String url = recordsDetailsUrlMap.get(id);
            try {
                logger.debug("\nSending 'POST' request for : " + id);
                Document profileDetailDoc = ((DesMoinesRegisterComSite) site).getDetailsDoc(url, this);

                if (spiderUtil.docWasRetrieved(profileDetailDoc)) {
                    if (((DesMoinesRegisterComSite) site).isARecordDetailDoc(profileDetailDoc)) {
                        recordsProcessed++;
                        arrestRecord = populateArrestRecord(profileDetailDoc, site);
                        arrestRecords.add(arrestRecord);
                        //save each record in case of application failures
                        outputUtil.addRecordToMainWorkbook(arrestRecord);
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
        
        if (filter!=null) {
	        List<Record> filteredRecords = filterRecords(arrestRecords);
	        //create a separate sheet with filtered results
	        logger.info(filteredRecords.size() + " " + filter.filterName() + " " + "records were crawled");
        }
        return recordsProcessed;
    }

    @Override
    public ArrestRecord populateArrestRecord(Object profileDetailObj, Site site) {
        Elements profileDetails = ((DesMoinesRegisterComSite) site).getRecordDetailElements((Document) profileDetailObj);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.generateRecordId(((Element) profileDetailObj).select("#permalink-url a").get(0).attr("href")));
        //made it here
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
            crawledIds = outputUtil.getPreviousIds();
            outputUtil.createSpreadsheet();
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return outputUtil;
    }
    
    @Override
    public void matchPropertyToField(ArrestRecord record, Object profileDetail) {
    	Element profileDetailElement = (Element) profileDetail;
        String label = profileDetailElement.select("strong").text().toLowerCase();
        if (!label.equals("")) {
            try {
                if (label.contains("booked")) {
                    formatArrestTime(record, profileDetailElement);
                } else if (label.contains("age")) {
                    record.setArrestAge(Integer.parseInt(extractValue(profileDetailElement)));
                } else if (label.contains("charges")) {
                    record.setCharges(extractValue(profileDetailElement).split(";"));
                } else if (label.contains("sex")) {
                    record.setGender(extractValue(profileDetailElement));
                } else if (label.contains("city")) {
//					record.setCity(city);
					record.setState("IA");
                } else if (label.contains("bond")) {
                    String bondAmount = extractValue(profileDetailElement);
                    int totalBond = Integer.parseInt(bondAmount.replace("$", "").replace(",", ""));
                    record.setTotalBond(totalBond);
                } else if (label.contains("height")) {
                    record.setHeight(extractValue(profileDetailElement));
                } else if (label.contains("weight")) {
                    record.setWeight(extractValue(profileDetailElement));
                } else if (label.contains("hair")) {
                    record.setHairColor(extractValue(profileDetailElement));
                } else if (label.contains("eye")) {
                    record.setEyeColor(extractValue(profileDetailElement));
                } else if (label.contains("county")) {
                    record.setCounty(extractValue(profileDetailElement));
                }
            } catch (NumberFormatException nfe) {
                logger.error("Couldn't parse a numeric value from " + profileDetailElement.text());
            }
        } else if (profileDetailElement.select("h1").hasText()) {
            record.setFullName(profileDetailElement.select("h1").text().trim());
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
    public List<Record> filterRecords(List<Record> fullArrestRecords) {
    	List<Record> filteredArrestRecords = new ArrayList<>();
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
}
