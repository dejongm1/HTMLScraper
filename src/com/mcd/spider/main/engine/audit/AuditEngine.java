package com.mcd.spider.main.engine.audit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.entities.audit.AuditResults;
import com.mcd.spider.main.entities.audit.AuditSpider;
import com.mcd.spider.main.entities.audit.OfflineResponse;
import com.mcd.spider.main.entities.audit.PageAuditResult;
import com.mcd.spider.main.entities.audit.SearchResults;
import com.mcd.spider.main.entities.audit.Term;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.SpiderUtil;

/**
 * 
 * @author u569220
 *
 */

public class AuditEngine {
	public static final Logger logger = Logger.getLogger(AuditEngine.class);
	
	private SpiderUtil spiderUtil = new SpiderUtil();
	private EngineUtil engineUtil = new EngineUtil();
	private Map<String, Boolean> urlsToCrawl = new HashMap<>();
    AuditSpider spider;
	
	public void performSEOAudit(String baseUrl, List<Term> terms, Integer depth, boolean performanceTest, int sleepTime) {
		urlsToCrawl = new HashMap<>();
		long timeSpent = 0;
		long startTime = 0;
		//empty string checks because parameters are optional
		//<url, checked>
		try {
			startTime = System.currentTimeMillis();
			spider = new AuditSpider(baseUrl, spiderUtil.offline());
			spider.setSleepTime(sleepTime==0?spiderUtil.offline()?0:2000:sleepTime*1000);
			timeSpent+=System.currentTimeMillis()-startTime;
            sleep(spider.getSleepTime());
		} catch (IOException ioe) {
			logger.error("Exception initializing audit spider for " + baseUrl, ioe);
			System.exit(0);
		}
		urlsToCrawl.put(baseUrl,  false);
		spider.setTermsToSearch(terms);
		
		//int depthLevel = 0;
		//while (depthLevel<=depth && allChecked(urlsToCheck)) { //allchecked() is inefficient for large sites
			//logger.debug("Depth = " + depthLevel);
		ListIterator<String> iterator = new ArrayList<>(urlsToCrawl.keySet()).listIterator();
		AuditResults auditResults = new AuditResults();
		String url = iterator.next();
		boolean checked = urlsToCrawl.get(url);
		if (!checked) {
			auditResults.addResponse(auditPage(url, iterator, spider));
		}
		//previous because adding to a listIterator adds before current elements
		while (iterator.hasPrevious()) {
			url = iterator.previous();
			checked = urlsToCrawl.get(url);
			if (!checked) {
				auditResults.addResponse(auditPage(url, iterator, spider));
                sleep(spider.getSleepTime());
			}
		}
		//}
		spider.setAuditResults(auditResults);
		spider.setAveragePageLoadTime(timeSpent/urlsToCrawl.size());
		for (PageAuditResult result : spider.getAuditResults().getAllResponses()) {
			//logger.info(result.getCode()==0?result.getUrl() + " didn't have an html file":result.prettyPrint()));
			logger.info(result.prettyPrint());
		}
		//TODO spit out more data
	}

	public PageAuditResult auditPage(String urlString, ListIterator<String> iterator, AuditSpider spider) {
		Elements ahrefs;
		Elements linkhrefs;
		PageAuditResult result = new PageAuditResult(urlString);
		//TODO make it work offline
		long pageStartTime = System.currentTimeMillis();
		//need to catch response codes that don't return a document
		Connection.Response response = null;
		Document docToCheck = null;
		try {
			Connection conn = ConnectionUtil.getConnection(urlString, "");
			if (spiderUtil.offline()) {
				response = new OfflineResponse(200, urlString);
			} else {
				response = conn.execute();//create a dummy ResponseImpl for offline work
			}
			docToCheck = response.parse();
			result.setLoadTime(System.currentTimeMillis()-pageStartTime);
			Map<String,String> responseHeaders = response.headers();
			for (Entry<String,String> headerEntry : responseHeaders.entrySet()) {
				logger.debug("Header=Value: " + headerEntry.getKey() + "=" + headerEntry.getValue());
			}
			result.setFullResponseCode(response.headers().get(null));
			result.setCode(response.statusCode());
		} catch (FileNotFoundException fnfe) {
            result.setCode(0);
            result.setFullResponseCode("HTTP/1.1 0 FileNotFound (offline)");
        } catch (HttpStatusException hse) {
            result.setCode(hse.getStatusCode());
		} catch (IOException e) {
            result.setCode(999);
            result.setFullResponseCode("HTTP/1.1 999 IOException");
			logger.error("IOException caught getting document", e);
		}
		logger.debug("Trying to get hrefs from " + urlString);
		if (engineUtil.docWasRetrieved(docToCheck)) {
		    //get frequent words
            getPopularWords(docToCheck, 5, result);
            //search for given word
            if (spider.getTermsToSearch()!=null) {
            	search(docToCheck, spider.getTermsToSearch(), result, 0, false);
            }
            //get inbound and outbound links
			ahrefs = docToCheck.select("a[href]");
			for (Element element : ahrefs) {
				urlsToCrawl = addToUrlsToCheck(element, iterator, result, spider.getBaseUrl());
			}
			urlsToCrawl.put(urlString, true);
		}
		return result;
	}
	
	private Map<String,Boolean> addToUrlsToCheck(Element element, ListIterator<String> iterator, PageAuditResult result, URL baseUrl) {
		//TODO needs a lot of refinement
		String url = element.attr("href");
		//filter out bogus stuff - xmls, pdfs, txt, images, etc
		if (isInBound(url, baseUrl)) {
			String absoluteUrl = url.startsWith("http")?url:baseUrl.toExternalForm() + url;
			if (!urlAlreadyChecked(absoluteUrl)) {//make absolute before adding to urlsToCheck map to avoid checking same page twice
                urlsToCrawl.put(absoluteUrl, false);
                iterator.add(absoluteUrl);
                result.addInBoundLink(url);//adding original url for now to demonstrate variations
            }
		} else {
			urlsToCrawl.put(url, true); //Adding to list so it doesn't get added again but not retrieving it to look for links
            result.addOutBoundLink(url);
		}
		return urlsToCrawl;
	}
	
	private boolean isInBound(String url, URL baseUrl) {
		if (url.startsWith("http://") || url.startsWith("https://")) { //isAbsolute
			return url.contains(baseUrl.getHost());
		} else {
			return true;
		}
	}
	
	private boolean urlAlreadyChecked(String absoluteUrl) {
		if (absoluteUrl.endsWith("/")) {
			return urlsToCrawl.get(absoluteUrl)!=null;
		} else {
			 return urlsToCrawl.get(absoluteUrl)!=null || urlsToCrawl.get(absoluteUrl+"/")!=null;
		}
	}

	private void sleep(long milliSecondsToSleep) {
        try {
            Thread.sleep(milliSecondsToSleep);
        } catch (InterruptedException e) {
            logger.error("Error trying to sleep");
        }
    }

    private void getPopularWords(Document doc, int numberOfWords, PageAuditResult page) {
        //give option to leave in numbers?
        Map<String, Term> termCountMap = new CaseInsensitiveMap<String, Term>();
        //TODO determine document type here before parsing - html, xml, etc
        //TODO look in other areas besides body?
        if (doc.body()!=null) {
            String bodyText = doc.body().text();
            String[] termsInBody = bodyText.split("\\s+");
            for (String term : termsInBody) {
                term = term.replaceAll("[[^\\p{L}\\p{Nd}]+]", "");
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
            Map<String,Term> sortedMap = spiderUtil.sortByValue(termCountMap);
            Iterator<Entry<String, Term>> iter = sortedMap.entrySet().iterator();
            Map<String,Term> mostPopularTerms = new LinkedHashMap<>();
            int i = 0;
            while (iter.hasNext() && i < numberOfWords) {
                Entry<String,Term> entry = (Entry<String,Term>) iter.next();
                mostPopularTerms.put(entry.getKey(), entry.getValue());
                i++;
            }

            page.setFrequentWords(mostPopularTerms);
        }
    }

    public void getPopularWords(String url, int numberOfWords) {
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
		if (engineUtil.docWasRetrieved(doc)) {
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
			Map<String, Term> sortedWords = spiderUtil.sortByValue(termCountMap);
			int i = 0;
			Iterator<Entry<String, Term>> iter = sortedWords.entrySet().iterator();
			while (iter.hasNext()) {
				Term term =  iter.next().getValue();
				if (i < numberOfWords && !term.getWord().equals("")) {
					logger.info(term.getCount() + "\t" + term.getWord());
					i++;
				}
			}
			time = System.currentTimeMillis() - time;
			logger.info("Took " + time + " ms");
		} else {
			logger.error("Failed to load html doc from " + url);
		}
	}
	
    private void search(Document doc, List<Term> terms, PageAuditResult page, int levelOfGenerosity, boolean tagsAndText) { //TODO add tags to textToSearch
    	SearchResults results = page.getSearchResults();
    	for (Term termToSearch : terms) {
    		Term term = new Term(termToSearch.getWord(), 0);
    		String textToSearch;
    		Pattern p;
    		Matcher m = null;
    		if (levelOfGenerosity == 0) { //not generous at all, strict word matching
    			textToSearch = doc.text().toLowerCase();
    			p = Pattern.compile(term.getWord().toLowerCase());//TODO put these into Term
    			m = p.matcher( textToSearch );//TODO put these into Term
    			
    			
    			
    		} else if (levelOfGenerosity == 1) { //semi-generous, words and possible variations
    			//    		textToSearch = doc.text().toLowerCase();
    			//    		p = Pattern.compile(word.toLowerCase());
    			//        	m = p.matcher( textToSearch );
    		} else if (levelOfGenerosity == 2) { //very generous, words and context in case of phrasing
    			//    		textToSearch = doc.text().toLowerCase();
    			//    		p = Pattern.compile(word.toLowerCase());
    			//        	m = p.matcher( textToSearch );
    		}
    		if (m!=null){
    			while (m.find()) {
    				term.increment();
    			}
    		}
    		logger.debug(term.getWord() + " was found " + term.getCount() + " times");
        	results.add(term);
    	}
    }
    
    public void search(String url, String word) {
//    	String in = "i have a male cat. the color of male cat is Black";
//    	int i = 0;
//    	Pattern p = Pattern.compile("male cat");
//    	Matcher m = p.matcher( in );
//    	while (m.find()) {
//    	    i++;
//    	}
    }
    
	public void getTextBySelector(String url, String selector) {
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
		if (engineUtil.docWasRetrieved(doc)) {
			Elements tags = doc.select(selector);
			for (Element tag : tags) {
				logger.info(tag.text());
			}
		} else {
			logger.error("Failed to load html doc from " + url);
		}
		time = System.currentTimeMillis() - time;
		logger.info("Took " + time + " ms");
	}
	
}
