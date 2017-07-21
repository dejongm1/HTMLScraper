package com.main.mcd.spider.util;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.main.mcd.spider.engine.SpiderEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.*;

public class SpiderUtil {
	
	private static final Logger logger = Logger.getLogger(SpiderUtil.class);
//	private Properties properties;
	
	public SpiderUtil(){
		loadProperties();
		//this.properties = loadProperties();
	}
//	public Properties getProperties() {
//		return this.properties;
//	}
//	public String getProperty(String propertyKey) {
//		return getProperties().getProperty(propertyKey);
//	}

	
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
		String htmlLocation = url.replace("/", "").replace(":", "").replace("?", "");
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

    public Document getHtmlAsDocTest(String url) {
        try {
            return ConnectionUtil.getConnectionDocumentTest(url);
        } catch (FileNotFoundException fne) {
            SpiderEngine.logger.error("I couldn't find an html file for " + url);
        } catch (ConnectException ce) {
            SpiderEngine.logger.error("I couldn't connect to " + url + ". Please be sure you're using a site that exists and are connected to the interweb.");
        } catch (IOException ioe) {
            SpiderEngine.logger.error("I tried to scrape that site but had some trouble. \n" + ioe.getMessage());
        }
        return null;
    }

    public Document getHtmlAsDoc(String url) {
        try {

            if (offline()) {
                return getOfflinePage(url);
            } else {
                return ConnectionUtil.getConnection(url).get();
            }
        } catch (FileNotFoundException fne) {
            SpiderEngine.logger.error("I couldn't find an html file for " + url);
        } catch (ConnectException ce) {
            SpiderEngine.logger.error("I couldn't connect to " + url + ". Please be sure you're using a site that exists and are connected to the interweb.");
        } catch (IOException ioe) {
            SpiderEngine.logger.error("I tried to scrape " + url + " but had some trouble. \n" + ioe.getMessage());
        }
        return null;
    }

    public void loadProperties() {
//	public Properties loadProperties() {
		InputStream input = null;
//		Properties tempProperties = new Properties();
		Properties properties = new Properties();
		try {
			// load a properties file
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (Boolean.valueOf(System.getProperty("runInEclipse"))) {
				input = loader.getResourceAsStream("config.properties");
			} else {
				input = SpiderUtil.class.getResourceAsStream("/resources/config.properties");
			}
//			tempProperties.load(input);
			properties.load(input);
			Properties systemProperties = System.getProperties();
			if (properties.get("offline") == null) {
				systemProperties.setProperty("offline", String.valueOf(offline()));
			}
			for (String propertyName : properties.stringPropertyNames()) {
				systemProperties.setProperty(propertyName, properties.getProperty(propertyName));
			}
			System.setProperties(systemProperties);
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
//		return tempProperties;
	}
}
