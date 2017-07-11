package com.mcd.scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {

	protected void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		
	}

	protected void getTextBySelector(String url, String selector) {
		Document doc = getHtmlAsDoc(url);
		if (doc!=null) {
			Elements tags = doc.select(selector);
			for (Element tag : tags) {
				System.out.println(tag.text());
			}
		}
	}

	private Document getHtmlAsDoc(String url) {
		try {
			if (Boolean.valueOf(System.getProperty("runInEclipse"))) {
				String htmlLocation;
				switch (url) {
					case "https://www.intoxalock.com/" : 									htmlLocation = "intoxalock-homepage.html";
																							break;
					case "https://en.wikipedia.org/" : 										htmlLocation = "wikipedia-homepage.html";
																							break;
					case "https://www.intoxalock.com/iowa/installation-locations" : 		htmlLocation = "intoxalock-iowa-locations.html";
																							break;
					default : 																htmlLocation = "";
																							break;
				}
				File input = new File("Scraper/htmls/" + htmlLocation);
				return Jsoup.parse(input, "UTF-8", url);
	
			} else {
				return Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36").get();
			}
		} catch (FileNotFoundException fne) {
			System.err.println("I couldn't find an html file for " + url);
		} catch (ConnectException ce) {
			System.err.println("I couldn't connect to " + url + ". Please be sure you're using a site that exists and are connected to the interweb.");
		} catch (IOException ioe) {
			System.err.println("I tried to scrape that site but had some trouble. \n" + ioe.getMessage());
		} 
		return null;
	}
	
	protected String validateURL(String url) throws IOException {
		String validUrl;
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (!urlValidator.isValid(url)) {
			validUrl = readLine("Please enter a valid URL, including protocol (http://, https://, etc)): ");
			if (!urlValidator.isValid(validUrl)) {
				validUrl = readLine("Still no good. Try again or I'm kicking you out: ");
				if (!urlValidator.isValid(validUrl)) {
					System.exit(0);
				}
			}
		} else {
			return url;
		}
		return validUrl;
	}

	protected int validateNumber(String numberOfWordsString) throws IOException {
		int numberOfWords = 0;
		try {
			numberOfWords = Integer.valueOf(numberOfWordsString);
		} catch (NumberFormatException nfe) {
			try {
				numberOfWords = Integer.valueOf(readLine("Please enter a enter a number between 1 and 100: "));
			} catch (NumberFormatException nfe2) {
				try {
					numberOfWords = Integer.valueOf(readLine("Last chance. Enter a number or you're just gonna get 5. "));
				} catch (NumberFormatException nfe3) {
					numberOfWords = 5;
				}
			}
		}
		return numberOfWords;
	}

	protected String readLine(String arg) throws IOException {
		if (System.console() != null) {
			return System.console().readLine(arg);
		}
		System.out.print(arg);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		return reader.readLine();
	}

}
