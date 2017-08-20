package com.mcd.spider.main.engine.record.iowa;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.ArrestRecord.RecordColumnEnum;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.main.entities.site.SpiderWeb;
import com.mcd.spider.main.entities.site.service.DesMoinesRegisterComSite;
import com.mcd.spider.main.exception.ExcelOutputException;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.SpiderUtil;
import com.mcd.spider.main.util.io.RecordIOUtil;
import com.mcd.spider.main.util.io.RecordOutputUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
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

//    private static final Logger logger = Logger.getLogger("dsmregcomLogger");
    public static final Logger logger = Logger.getLogger(DesMoinesRegisterComEngine.class);
    
    private SpiderUtil spiderUtil = new SpiderUtil();
    private RecordFilterEnum filter;
    private ConnectionUtil connectionUtil;
    private DesMoinesRegisterComSite site;
    private RecordIOUtil recordIOUtil;
    private SpiderWeb spiderWeb;

    @Override
    public void getArrestRecords(State state, long maxNumberOfResults, RecordFilterEnum filter) throws SpiderException {
        long totalTime = System.currentTimeMillis();
    	this.filter = filter;
    	spiderWeb = new SpiderWeb(maxNumberOfResults, false);
    	if (spiderWeb.isOffline()) {
    		logger.debug("Offline - can't scrape this php site. Try making an offline version");
    	} else {
	        int sleepTimeSum = 0;
	        site = new DesMoinesRegisterComSite(null);
            recordIOUtil = initializeIOUtil(state);
            connectionUtil = new ConnectionUtil(true);

	        logger.info("----Site: " + site.getName() + "----");
	        logger.debug("Sending spider " + (spiderWeb.isOffline()?"offline":"online" ));

	        int sleepTimeAverage = spiderWeb.isOffline()?0:(site.getPerRecordSleepRange()[0]+site.getPerRecordSleepRange()[1])/2;

	        scrapeSite(1);

            formatOutput(new ArrayList<>(recordIOUtil.getInputter().readDefaultSpreadsheet()), recordIOUtil.getOutputter());

	        //outputUtil.removeColumnsFromSpreadsheet(new int[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.index()});

	        spiderUtil.sendEmail(state);

	        totalTime = System.currentTimeMillis() - totalTime;
	        if (!spiderWeb.isOffline()) {
	            logger.info("Sleep time was approximately " + sleepTimeAverage*spiderWeb.getRecordsProcessed() + " ms");
	            logger.info("Processing time was approximately " + (totalTime-(sleepTimeAverage*spiderWeb.getRecordsProcessed())) + " ms");
	        }
            logger.info("Total time taken was " + totalTime + " ms");
            logger.info(spiderWeb.getRecordsProcessed() + " total records were processed");
    	}
    }

    @Override
    public void scrapeSite(int attemptCount) {
    	int maxAttempts = site.getMaxAttempts();
        for (String county : site.getCounties()) {
            site.setBaseUrl(new String[]{county});
            StringBuffer response = new StringBuffer();
            try {
            	response = (StringBuffer) initiateConnection(county);
            } catch (java.io.IOException e) {
                logger.error("IOException caught sending http request to " + site.getUrl(), e);
            }
            if (attemptCount<=maxAttempts) {
                logger.info("Retrieving details page urls");
                //build a list of details page urls by parsing results page docs
                Map<Object,String> recordDetailUrlMap = compileRecordDetailUrlMap(response);

                logger.info("Gathered links for " + recordDetailUrlMap.size() + " record profiles");

                spiderUtil.sleep(spiderWeb.isOffline()?0:10000, true);
                scrapeRecords(recordDetailUrlMap);
            } else {
            	 logger.error("Failed to load html doc from " + site.getBaseUrl()+ ". Trying again " + (maxAttempts-attemptCount) + " more times");
                 attemptCount++;
                 scrapeSite(attemptCount);
             }
        }
    }

    @Override
    public Map<String, String> parseDocForUrls(Object response){
        Map<String, String> detailUrlMap = new HashMap<>();
        try {
            JSONObject json = new JSONObject(response.toString());
            JSONArray mugShots = json.getJSONArray("mugs");
            for(int m=0;m<mugShots.length();m++) {
                JSONObject mugshot = (JSONObject) mugShots.get(m);
                //only add if we haven't already crawled it
                if (!spiderWeb.getCrawledIds().contains(mugshot.getString("id"))) {
                    detailUrlMap.put(mugshot.getString("id"), site.getBaseUrl() + "&id=" + mugshot.getString("id"));
                }
            }
        } catch (JSONException e) {
            logger.error("JSONExecption caught", e);
        }
        return detailUrlMap;
    }

    @Override
    public void scrapeRecords(Map<Object, String> recordsDetailsUrlMap){
        List<Record> arrestRecords = new ArrayList<>();
        ArrestRecord arrestRecord;
        RecordOutputUtil recordOutputUtil = recordIOUtil.getOutputter();
        String referer = "";
        for (Entry<Object, String> entry : recordsDetailsUrlMap.entrySet()) {
            String id = (String) entry.getKey();
            String url = recordsDetailsUrlMap.get(id);
            try {
                logger.debug("\nSending 'POST' request for : " + id);
                Document profileDetailDoc = obtainRecordDetailDoc(url, referer);

                if (spiderUtil.docWasRetrieved(profileDetailDoc)) {
                    if (site.isARecordDetailDoc(profileDetailDoc)) {
                        try {
                            arrestRecord = populateArrestRecord(profileDetailDoc);
                            arrestRecords.add(arrestRecord);
                            //save each record in case of application failures
                            recordOutputUtil.addRecordToMainWorkbook(arrestRecord);
                            spiderWeb.addToRecordsProcessed(1);
                            logger.debug("Record " + spiderWeb.getRecordsProcessed() + " saved");
                            spiderUtil.sleep(ConnectionUtil.getSleepTime(site), true);//sleep at random interval
                        } catch (Exception e) {
                            logger.error("Generic exception caught while trying to grab arrest record for " + profileDetailDoc.baseUri(), e);
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
            referer = url;
        }
        //don't format here since we only have 1 county's records
        //formatOutput(arrestRecords, recordOutputUtil);
    }

    @Override
    public ArrestRecord populateArrestRecord(Object profileDetailObj) {
        Elements profileDetails = site.getRecordDetailElements((Document) profileDetailObj);
        ArrestRecord record = new ArrestRecord();
        record.setId(site.generateRecordId(((Element) profileDetailObj).select("#permalink-url a").get(0).attr("href")));
        //made it here
        for (Element profileDetail : profileDetails) {
            matchPropertyToField(record, profileDetail);
            logger.debug("\t" + profileDetail.text());
        }
        return record;
    }

    @Override
    public RecordIOUtil initializeIOUtil(State state) throws SpiderException {
        RecordIOUtil ioUtil = new RecordIOUtil(state, new ArrestRecord(), site);
        try {
            //load previously written records IDs into memory
        	spiderWeb.setCrawledIds(ioUtil.getInputter().getPreviousIds());
            //load records in current spreadsheet into memory
        	spiderWeb.setCrawledRecords(ioUtil.getInputter().readDefaultSpreadsheet());
            ioUtil.getOutputter().createSpreadsheet();
        } catch (ExcelOutputException | IDCheckException e) {
            throw e;
        }
        return ioUtil;
    }

    @Override
    public Object initiateConnection(String county) throws IOException {
        //build http post request
        URL obj = new URL(site.getServiceUrl());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        String referer = "www.google.com";
        
        //add request header
        con.setRequestMethod(site.getRequestType());
        con.setRequestProperty("User-Agent", site.getUserAgent());
        con.setRequestProperty("Accept-Language", site.getAcceptLanguage());
        con.setRequestProperty("Accept-Encoding", site.getAcceptEncoding());
        con.setRequestProperty("Accept", site.getAcceptType());
        con.setRequestProperty("Content-Length", site.getContentLength());
        con.setRequestProperty("Content-Type", site.getContentType());
        con.setRequestProperty("Cookie", site.getCookie());
        con.setRequestProperty("Referer", referer);
        con.setRequestProperty("XRequested-With", site.getXRequestedWith());
        con.setRequestProperty("Proxy-Connection", site.getProxyConnection());
        con.setRequestProperty("Host", site.getHost());
        
        String urlParameters = site.getRequestBody(new String[]{county, "getmugs", "0", String.valueOf(spiderWeb.getMaxNumberOfResults()==0?10000:spiderWeb.getMaxNumberOfResults()/3)});

        // Send post request
        logger.debug("\nSending 'POST' request to URL : " + site.getUrl().getDomain());
        return sendRequest(con, urlParameters);

    }

    public Map<Object,String> compileRecordDetailUrlMap(StringBuffer response) {
        Map<Object, String> profileDetailUrlMap = new HashMap<>();
        profileDetailUrlMap.putAll(parseDocForUrls(response));
        return profileDetailUrlMap;
    }

    public Document obtainRecordDetailDoc(String url, String referer) throws IOException, JSONException {
    	return site.getDetailsDoc(url, this, referer);
    }
    
    @Override
    public void formatOutput(List<Record> arrestRecords, RecordOutputUtil recordOutputUtil) {
    	//format the output
        logger.info("Starting to output the results");
        Collections.sort(arrestRecords, ArrestRecord.CountyComparator);
        String delimiter = RecordColumnEnum.COUNTY_COLUMN.getColumnTitle();
        Class<ArrestRecord> clazz = ArrestRecord.class;
        if (filter!=null) {
            try {
                logger.info("Outputting filtered results");
                List<Record> filteredRecords = filterRecords(arrestRecords);
                List<List<Record>> splitRecords = Record.splitByField(filteredRecords, delimiter, clazz);
                //create a separate sheet with filtered results
                logger.info(filteredRecords.size()+" "+filter.filterName()+" "+"records were crawled");
                if (!filteredRecords.isEmpty()) {
                    recordOutputUtil.createFilteredSpreadsheet(filter, filteredRecords);
                    recordOutputUtil.splitIntoSheets(recordOutputUtil.getFilteredDocName(filter), delimiter, splitRecords, clazz);
                }
            } catch (Exception e) {
                logger.error("Error trying to create filtered spreadsheet", e);
            }
        }
        try {
            List<List<Record>> splitRecords = Record.splitByField(arrestRecords, delimiter, clazz);
            recordOutputUtil.splitIntoSheets(recordOutputUtil.getDocName(), delimiter, splitRecords, clazz);
        } catch (Exception e) {
            logger.error("Error trying to split full list of records", e);
        }
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
    	//only full name included
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
            } else if (!arrestDate.trim().equalsIgnoreCase("today")) {
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

	@Override
	public void setCookies(Response response) {
		for (Map.Entry<String,String> cookieEntry : response.cookies().entrySet()) {
    		spiderWeb.addSessionCookie(cookieEntry.getKey(), cookieEntry.getValue());
		}
    	int recordCap = spiderWeb.isOffline()?3:330;
		if (spiderWeb.getRecordsProcessed() % recordCap == 0 && spiderWeb.getRecordsProcessed() != 0) {
			connectionUtil.changeUserAgent();
        }
	}
}
