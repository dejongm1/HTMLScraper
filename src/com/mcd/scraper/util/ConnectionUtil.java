package com.mcd.scraper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mcd.scraper.entities.site.Site;

public class ConnectionUtil {
	
	private static final Logger logger = Logger.getLogger(ConnectionUtil.class);
	
	private ConnectionUtil(){}
	
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

	public static Connection getConnection(String url) throws IOException {
		return Jsoup.connect(url)
				.userAgent(getRandomUserAgent())
//				.userAgent("findlinks/1.1.2-a5 (+http\\://wortschatz.uni-leipzig.de/findlinks/)")
				.maxBodySize(0)
				.timeout(30000);
	}

	private static String getRandomUserAgent() { //to avoid getting blacklisted
		String[] userAgentList = System.getProperty("user.agents").split(", ");
		int r = getRandom(userAgentList.length-1, -1);
		logger.debug("User-agent: " + userAgentList[r]);
		return userAgentList[r];
	}
	
	public static int getSleepTime(Site site) {
		boolean offline = Boolean.parseBoolean(System.getProperty("offline"));
		int[] sleepTimeRange = site.getPerRecordSleepRange();
		return offline?0:getRandom(sleepTimeRange[0], sleepTimeRange[1]);
	}
	
	private static int getRandom(int from, int to) {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		if (to==-1) {
			return ThreadLocalRandom.current().nextInt(from*1000);
		} else {
			return ThreadLocalRandom.current().nextInt(from*1000, (to + 1)*1000);
		}
	}

}
