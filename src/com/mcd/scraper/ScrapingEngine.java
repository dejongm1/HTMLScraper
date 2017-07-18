package com.mcd.scraper;

import com.mcd.scraper.entities.State;
import com.mcd.scraper.entities.Term;
import com.mcd.scraper.entities.site.Site;
import com.mcd.scraper.util.ScraperUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {

	ScraperUtil util = new ScraperUtil();

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
			
			Map<String, Term> sortedWords = util.sortByValue(termCountMap);
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
	
	protected void getArrestRecords(List<State> states, long maxNumberOfResults) {
		//split into more specific methods
		long totalTime = System.currentTimeMillis();
		long perRecordSleepTime = util.offline()?0:2000;
		long recordsProcessed = 0;
		//use maxNumberOfResults to stop processing once this method has been broken up
		for (State state : states){
			long stateTime = System.currentTimeMillis();
			System.out.println("----State: " + state.getName() + "----");
			Site[] sites = state.getSites();
			for(Site site : sites){
				long time = System.currentTimeMillis();
				String baseUrl = site.getBaseUrl(new String[]{state.getName()});
				//Add some retries if first connection to state site fails?
				Document recordListingDoc = getHtmlAsDoc(baseUrl);
				if (recordListingDoc!=null) {
					int numberOfPages = site.getPages(recordListingDoc);
					if (numberOfPages==0) {
						numberOfPages = 1;
					}
					for (int p=1; p<=numberOfPages;p++) {
						System.out.println("----State: " + state.getName() + ": Page " + p);
						//eventually output to spreadsheet
						Elements profileDetailTags = site.getRecordElements(recordListingDoc);
						for (Element pdTag : profileDetailTags) {
							Document profileDetailDoc = getHtmlAsDoc(site.getRecordDetailDocUrl(pdTag));
							recordsProcessed++;
							if(profileDetailDoc!=null){
								Elements profileDetails = site.getRecordDetailElements(profileDetailDoc);
								System.out.println(pdTag.text());
								for (Element profileDetail : profileDetails) {
									System.out.println("\t" + profileDetail.text());
								}
							} else {
								System.out.println("Couldn't load details for " + pdTag.text());
							}
							try {
								Thread.sleep(perRecordSleepTime);
							} catch (InterruptedException ie) {
								System.err.println(ie.getMessage());
							}
						}
					}
				}
				time = System.currentTimeMillis() - time;
				System.out.println(baseUrl + " took " + time + " ms");
			}

			stateTime = System.currentTimeMillis() - stateTime;
			System.out.println(state.getName() + " took " + stateTime + " ms");
			
		}

		totalTime = System.currentTimeMillis() - totalTime;
		System.out.println("Sleep time was " + recordsProcessed*perRecordSleepTime + " ms");
		System.out.println(states.size() + " states took " + totalTime + " ms");
		System.out.println("Processing time was " + (totalTime-(recordsProcessed*perRecordSleepTime)) + " ms");
	}

	private Document getHtmlAsDoc(String url) {
		try {
			if (util.offline()) {
				return util.getOfflinePage(url);
			} else {
				return util.getConnection(url).get();
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

}
