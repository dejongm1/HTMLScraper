package com.mcd.scraper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mcd.scraper.ScrapingEngine;

public class Util {
	
	private static final Logger logger = Logger.getLogger(Util.class);
	private Properties properties;
	
	public Util(){
		this.properties = loadProperties();
	}
	
	public Properties getProperties() {
		return this.properties;
	}

	public boolean offline(){
		try {
			return (Boolean.valueOf(System.getProperty("runInEclipse")) 
					|| Boolean.valueOf(System.getProperty("runOffline"))
					|| !InetAddress.getByName("google.com").isReachable(3000));
		} catch (IOException e) {
			return true;
		}
	}

	public Document getOfflinePage(String url) throws IOException {
		String htmlLocation;
		switch (url) {
			case "https://www.intoxalock.com/" : 									htmlLocation = "intoxalock-homepage.html";
				break;
			case "https://en.wikipedia.org/" : 										htmlLocation = "wikipedia-homepage.html";
				break;
			case "https://www.intoxalock.com/iowa/installation-locations" : 		htmlLocation = "intoxalock-iowa-locations.html";
				break;
			case "http://iowa.arrests.org" : 										htmlLocation = "iowa-arrests.htm";
				break;
			case "http://iowa.arrests.org/?page=1&results=14" : 					htmlLocation = "iowa-arrests-page1.htm";
				break;
			case "http://iowa.arrests.org/?page=2&results=14" : 					htmlLocation = "iowa-arrests-page2.htm";
				break;
			case "http://iowa.arrests.org/?page=1&results=56" :						htmlLocation = "iowa-arrests-56-results.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Charles_Ross_33669899/" : 		htmlLocation = "iowa-arrests-Charles-Ross.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Shelley_Bridges_33669900/" : 		htmlLocation = "iowa-arrests-Shelley-Bridges.htm";
				break;
			case "http://iowa.arrests.org/Arrests/David_Edwards_33669901/" : 		htmlLocation = "iowa-arrests-David-Edwards.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Jason_Burk_33706163/" : 			htmlLocation = "iowa-arrests-jason-burk.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Nicholas_Lynch_33706164/" : 		htmlLocation = "iowa-arrests-nicholas-lynch.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Jordan_Fetter_33706672/" : 		htmlLocation = "iowa-arrests-jordan-fetter.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Tricia_Garvin-brown_33706569/" : 	htmlLocation = "iowa-arrests-Tricia Garvin-brown.htm";
				break;
			case "http://illinois.arrests.org/Arrests/Paul_Rinaldi_33672705/" : 	htmlLocation = "illinois-arrests-Paul-Rinaldi.htm";
				break;
			case "http://illinois.arrests.org/Arrests/Nicole_Shula_33672706/" : 	htmlLocation = "illinois-arrests-Nicole-Shula.htm";
				break;
			case "http://illinois.arrests.org" : 									htmlLocation = "illinois-arrests.htm";
				break;
			case "http://illinois.arrests.org/?page=1&results=56" :					htmlLocation = "illinois-arrests-56-results.htm";
				break;
			case "http://www.browser-info.net/useragents" :							htmlLocation = "BROWSER-INFO - User-Agents.html";
				break;
			default : 																htmlLocation = "";
				break;
		}
		File input = new File("htmls/" + htmlLocation);
		return Jsoup.parse(input, "UTF-8", url);
	}

	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return ( o1.getValue() ).compareTo( o2.getValue() );
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public Document getHtmlAsDoc(String url) {
		try {
			if (offline()) {
				return getOfflinePage(url);
			} else {
				return getConnection(url).get();
			}
		} catch (FileNotFoundException fne) {
			ScrapingEngine.logger.error("I couldn't find an html file for " + url);
		} catch (ConnectException ce) {
			ScrapingEngine.logger.error("I couldn't connect to " + url + ". Please be sure you're using a site that exists and are connected to the interweb.");
		} catch (IOException ioe) {
			ScrapingEngine.logger.error("I tried to scrape that site but had some trouble. \n" + ioe.getMessage());
		}
		return null;
	}
	
	public Connection getConnection(String url) {
		return Jsoup.connect(url)
				.userAgent(getRandomUserAgent())
//				.userAgent("findlinks/1.1.2-a5 (+http\\://wortschatz.uni-leipzig.de/findlinks/)")
				.maxBodySize(0)
				.timeout(30000);
	}

	protected String getRandomUserAgent() { //to avoid getting blacklisted
		String[] crawlerList = getProperty("user.agent").split(", ");
		Random random = new Random();
		int r = random.nextInt(crawlerList.length-1);
		logger.debug("Crawler: " + crawlerList[r]);
		return crawlerList[r];
	}
	
	public Properties loadProperties() {
		InputStream input = null;
		Properties tempProperties = new Properties();
		try {
			// load a properties file
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (Boolean.valueOf(System.getProperty("runInEclipse"))) {
				input = loader.getResourceAsStream("config.properties");
			} else {
				input = Util.class.getResourceAsStream("/resources/config.properties");
			}
			tempProperties.load(input);
//			Properties systemProperties = System.getProperties();
//			for (String propertyName : properties.stringPropertyNames()) {
//				systemProperties.setProperty(propertyName, properties.getProperty(propertyName));
//			}
//			System.setProperties(systemProperties);
		} catch (IOException ioe) {
			logger.info("Error getting properties file", ioe);
		} catch (NullPointerException np) {
			logger.info("Properties file cannot be found", np);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioe) {
					logger.info("Error getting properties file", ioe);
				}
			}
		}
		return tempProperties;
	}
	
	public String getProperty(String propertyKey) {
		return getProperties().getProperty(propertyKey);
	}
}
