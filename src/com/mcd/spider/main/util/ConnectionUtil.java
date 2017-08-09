package com.mcd.spider.main.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mcd.spider.main.entities.audit.OfflineResponse;
import com.mcd.spider.main.entities.site.Site;

public class ConnectionUtil {
	
	private static final Logger logger = Logger.getLogger(ConnectionUtil.class);
	
	private static String userAgent;
	private static boolean offline = Boolean.parseBoolean(System.getProperty("offline"));
	
	public  ConnectionUtil(){}
	
	public ConnectionUtil(boolean useSameUserAgent){
		if (useSameUserAgent) {
			userAgent = getRandomUserAgent();
		}
	}
	
	public static Document getConnectionDocumentTest(String url) throws IOException {
        final URL website = new URL(url);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)); // set proxy server and port
        HttpURLConnection httpUrlConnetion = (HttpURLConnection) website.openConnection(proxy);
        httpUrlConnetion.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(httpUrlConnetion.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String str;

        while( (str = br.readLine()) != null )
        {
            buffer.append(str);
        }
        return Jsoup.parse(buffer.toString());
    }

	public Connection getConnection(String url, String referrer, Map<String,String> cookies) throws IOException {
	    if (referrer==null) {
            referrer = "https://google.com";
        }
	    if (cookies==null){
	    	cookies = new HashMap<>();
	    }
	    logger.trace("UserAgent: " + userAgent);
		return Jsoup.connect(url)
				.userAgent(userAgent!=null?userAgent:getRandomUserAgent())
				.referrer(referrer)
				.maxBodySize(0)
				.cookies(cookies)
				.timeout(30000);
	}
	public Connection getConnection(String url, String referrer) throws IOException {
		return getConnection(url, referrer, null);
	}

	public String getRandomUserAgent() { //to avoid getting blacklisted
		String[] userAgentList = System.getProperty("user.agents").split(", ");
		int r = getRandom(userAgentList.length-1, -1, false);
		logger.trace("User-agent: " + userAgentList[r]);
		return userAgentList[r];
	}
	
	public static int getSleepTime(Site site) {
		int[] sleepTimeRange = site.getPerRecordSleepRange();
		return offline?0:getRandom(sleepTimeRange[0], sleepTimeRange[1], true);
	}
	
	private static int getRandom(int from, int to, boolean inMilliseconds) {
	    int multiplier = inMilliseconds?1000:1;
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		if (to==-1) {
			return ThreadLocalRandom.current().nextInt(from*multiplier);
		} else {
			return ThreadLocalRandom.current().nextInt(from*multiplier, to*multiplier);
		}
	}
	
    public Connection.Response retrieveConnectionResponse(String url, String refferer, Map<String,String> cookies) throws IOException {
    	Connection.Response response;
    	Connection conn = getConnection(url, refferer, cookies);
		if (offline) {
			if (cookies!=null) {
				response = new OfflineResponse(200, url, cookies);
			} else {

				response = new OfflineResponse(200, url);
			}
		} else {
			response = conn.execute();
		}
		return response;
    }
    public Connection.Response retrieveConnectionResponse(String url, String refferer) throws IOException {
    	return retrieveConnectionResponse(url, refferer, null);
    }
    
}
