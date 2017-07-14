package com.mcd.scraper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
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
	
	protected void getRecords(List<State> states) {
		for (State state : states){
			long time = System.currentTimeMillis();
			Site[] sites = state.getSites();
			for(Site site : sites){
				String stateSpecificUrl = site.getProtocol()+state.getName().toLowerCase()+"."+site.getDomain();
				Document doc = getHtmlAsDoc(stateSpecificUrl+site.getExtensions()[0]);
				if (doc!=null) {
					//logic goes here
					//eventually output to spreadsheet
					String[] selectors = site.getSelectors();
					Elements profileDetailTags = doc.select(selectors[0]);
					for (Element pdTag : profileDetailTags) {
						String pdLink = pdTag.attr("href");
						Document profileDetailDoc = getHtmlAsDoc(stateSpecificUrl+pdLink);
						if(profileDetailDoc!=null){
							Elements profileDetails = profileDetailDoc.select(selectors[1]);
							System.out.println(pdTag.text());
							for (Element profileDetail : profileDetails) {
								System.out.println("\t" + profileDetail.text());	
							}
						} else {
							System.out.println("Couldn't load details for " + pdTag.text());
						}
					}

				}
			}

			time = System.currentTimeMillis() - time;
			System.out.println("Took " + time + " ms");
		}
	}

	private Document getHtmlAsDoc(String url) {
		try {
			if (HTMLScraperUtil.offline()) {
				return HTMLScraperUtil.getOfflinePage(url);
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
	
	protected boolean validURL(String url) throws IOException {
		String[] schemes = {"http","https"};
	    UrlValidator urlValidator = new UrlValidator(schemes);
	    return urlValidator.isValid(url);
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

	protected Object getInput(String prompt, int numberOfTries, String validationType) throws IOException {
		for(int t=1;t <= numberOfTries; t++){
			if (validationType!= null && validationType.equals(HTMLScraperConstants.URL_VALIDATION)) {
				String url = readLine(prompt);
				url = !url.startsWith("http")?"http://"+url:url;
				if (validURL(url)){
					return url;
				} else {
					System.out.println("That's not a valid url\n");
				}
			} else if (validationType!= null && validationType.equals(HTMLScraperConstants.NUMBER_VALIDATION)) {
				try {
					int number = Integer.parseInt(readLine(prompt));
					return number;
				} catch (NumberFormatException nfe) {
					System.out.println("That's not a number\n");
				}
			} else if (validationType!= null && validationType.equals(HTMLScraperConstants.STATE_VALIDATION)) {
				List<State> states = State.confirmState(readLine(prompt));
				if (states!=null) {
					return states;
				} else {
					System.out.println("That's not an American state\n");
				}
			} else {
				return readLine(prompt);
			}
		}
		System.exit(0);
		return null;
	}
	
	protected String readLine(String arg) throws IOException {
		if (System.console() != null) {
			String input = System.console().readLine(arg);
			if (!quitting(input)) {
				return input;
			} else {
				System.exit(0);
				return null;
			}
		}
		
		//for debugging in Eclipse
		System.out.print(arg);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String input = reader.readLine();
		if (!quitting(input)) {
			return input;
		} else {
			System.exit(0);
			return null;
		}
	}
	
	protected boolean quitting(String input) {
		if (input.equalsIgnoreCase("quit")) {
			return true;
		} else {
			return false;
		}
	}
}
