package com.mcd.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

public class HTMLScraperUtil {

	public HTMLScraperUtil(){
		loadProperties();
	}

	protected boolean offline(){
		try {
			return (Boolean.valueOf(System.getProperty("runInEclipse")) || !InetAddress.getByName("google.com").isReachable(5000));
		} catch (IOException e) {
			return true;
		}
	}

	protected Document getOfflinePage(String url) throws IOException {
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
			case "http://iowa.arrests.org/?page=1&results=56" :						htmlLocation = "iowa-arrests-56-results.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Charles_Ross_33669899/?d=1" : 	htmlLocation = "iowa-arrests-Charles-Ross.htm";
				break;
			case "http://iowa.arrests.org/Arrests/Shelley_Bridges_33669900/?d=1" : 	htmlLocation = "iowa-arrests-Shelley-Bridges.htm";
				break;
			case "http://iowa.arrests.org/Arrests/David_Edwards_33669901/?d=1" : 	htmlLocation = "iowa-arrests-David-Edwards.htm";
				break;
			case "http://illinois.arrests.org/Arrests/Paul_Rinaldi_33672705/?d=1" : htmlLocation = "iowa-arrests-Paul-Rinaldi.htm";
				break;
			case "http://illinois.arrests.org/Arrests/Nicole_Shula_33672706/?d=1" : htmlLocation = "iowa-arrests-Nicole-Shula.htm";
				break;
			case "http://illinois.arrests.org" : 									htmlLocation = "illinois-arrests.htm";
				break;
			case "http://illinois.arrests.org/?page=1&results=56" :					htmlLocation = "illinois-arrests-56-results.htm";
				break;
			default : 																htmlLocation = "";
				break;
		}
		File input = new File("htmls/" + htmlLocation);
		return Jsoup.parse(input, "UTF-8", url);
	}

	protected <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
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

	protected Connection getConnection(String url) {
		return Jsoup.connect(url)
				.userAgent(getRandomUserAgent())
//				.userAgent("findlinks/1.1.2-a5 (+http\\://wortschatz.uni-leipzig.de/findlinks/)")
				.maxBodySize(0)
				.timeout(30000);
	}

	protected String getRandomUserAgent() { //to avoid getting blacklisted
		String[] crawlerList = System.getProperties().getProperty("user.agent.crawlers").split(", ");
		Random random = new Random();
		int r = random.nextInt(crawlerList.length-1);
		System.out.println("Crawler: " + crawlerList[r]);
		return crawlerList[r];
	}

	private void loadProperties() {
		InputStream input = null;
		Properties properties = new Properties();
		try {
			// load a properties file
			properties.load(HTMLScraperUtil.class.getResourceAsStream("/config.properties"));
			Properties systemProperties = System.getProperties();
			for (String propertyName : properties.stringPropertyNames()) {
				systemProperties.setProperty(propertyName, properties.getProperty(propertyName));
			}
			System.setProperties(systemProperties);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
