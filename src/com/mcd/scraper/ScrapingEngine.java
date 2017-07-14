package com.mcd.scraper;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {

	protected void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
		long time = System.currentTimeMillis();
		Document doc = getHtmlAsDoc(url);
		if (doc!=null) {
			//give option to leave in numbers? 
			Map<String, Term> termCountMap = new CaseInsensitiveMap<String, Term>();
			String bodyText = doc.body().text();
			String[] termsInBody = bodyText.split("\\s+");
			for (String term : termsInBody) {
				term = term.replaceAll("[[^\\p{L}\\p{Nd}]+]", "");
				//instead of get, can this be a generous match?
				if (!term.equals("")) {
					Term termObj = termCountMap.get(term);
					if (termObj == null) {
						termObj = new Term(term, 1);
						termCountMap.put(term, termObj);
					} else {
						termObj.increment();
					}
				}
			}
			
			Map<String, Term> sortedWords = HTMLScraperUtil.sortByValue(termCountMap);
			int i = 0;
			Iterator<Entry<String, Term>> iter = sortedWords.entrySet().iterator();
			while (iter.hasNext()) {
				Term term =  iter.next().getValue();
				if (i < numberOfWords && !term.getWord().equals("")) {
					System.out.println(term.getCount() + "\t" + term.getWord());
					i++;
				}
			}
			time = System.currentTimeMillis() - time;
			System.out.println("Took " + time + " ms");
			
		}
	}

	protected void getTextBySelector(String url, String selector) {
		long time = System.currentTimeMillis();
		Document doc = getHtmlAsDoc(url);
		if (doc!=null) {
			Elements tags = doc.select(selector);
			for (Element tag : tags) {
				System.out.println(tag.text());
			}
		}

		time = System.currentTimeMillis() - time;
		System.out.println("Took " + time + " ms");
	}

	private Document getHtmlAsDoc(String url) {
		try {
			if (HTMLScraperUtil.offline()) {
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
				File input = new File("htmls/" + htmlLocation);
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
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (!urlValidator.isValid(url)) {
			url = readLine("Please enter a valid URL, including protocol (http://, https://)): ");
			if (!urlValidator.isValid(url)) {
				url = readLine("Still no good. Try again or I'm kicking you out: ");
				if (!urlValidator.isValid(url)) {
					System.err.println("I'm not sure what you did but I don't like it. I quit.");
					System.exit(0);
				}
			}
		}
		return url;
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
