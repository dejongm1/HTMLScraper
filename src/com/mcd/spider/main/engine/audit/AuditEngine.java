package com.mcd.spider.main.engine.audit;

import com.mcd.spider.main.entities.audit.*;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.SpiderUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

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
	
	public void performSEOAudit(String baseUrl, String terms, Integer depth, boolean performanceTest, int sleepTime) {
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
			logger.info(result.getCode()==0?result.getUrl() + " didn't have an html file":result.prettyPrint());
		}
		//TODO spit out more data
	}

	public PageAuditResult auditPage(String url, ListIterator<String> iterator, AuditSpider spider) {
		Elements ahrefs;
		Elements linkhrefs;
		PageAuditResult result = new PageAuditResult(url);
		//TODO make it work offline
		long pageStartTime = System.currentTimeMillis();
		//need to catch response codes that don't return a document
		Connection.Response response = null;
		Document docToCheck = null;
		try {
			Connection conn = ConnectionUtil.getConnection(url, "");
			if (spiderUtil.offline()) {
				response = new OfflineResponse(200, url);
			} else {
				response = conn.execute();//create a dummy ResponseImpl for offline work
			}
			docToCheck = response.parse();
			result.setLoadTime(System.currentTimeMillis()-pageStartTime);
			result.setCode(response.statusCode());
		} catch (FileNotFoundException fnfe) {
            logger.error("FileNotFound exception (offline))");
            result.setCode(0);
        } catch (HttpStatusException hse) {
            result.setCode(hse.getStatusCode());
		} catch (IOException e) {
            result.setCode(999);
			logger.error("IOException caught getting document", e);
		}
		logger.debug("Trying to get hrefs from " + url);
		if (engineUtil.docWasRetrieved(docToCheck)) {
		    //get frequent words
            getPopularWords(docToCheck, 5, result);
            //get inbound and outbound links
			ahrefs = docToCheck.select("a[href]");
			for (Element element : ahrefs) {
				urlsToCrawl = addToUrlsToCheck(element, urlsToCrawl, iterator, result, spider.getBaseUrl());
			}
			urlsToCrawl.put(url, true);
		}
		return result;
	}
	
	private Map<String,Boolean> addToUrlsToCheck(Element element, Map<String, Boolean> urlsToCheck, ListIterator<String> iterator, PageAuditResult result, URL baseUrl) {
		//TODO needs a lot of refinement
		String url = element.attr("href");
		//filter out bogus stuff
		if (isInBound(url, baseUrl)) {
			String absoluteUrl = url.startsWith("http")?url:baseUrl.toExternalForm() + url;
			if (urlsToCheck.get(absoluteUrl)==null) {//make absolute before adding to urlsToCheck map to avoid checking same page twice
                urlsToCheck.put(absoluteUrl, false);
                iterator.add(absoluteUrl);
                result.addInBoundLink(url);//adding original url for now to demonstrate variations
            }
		} else /*if (isOutBound(url))*/ {
			urlsToCheck.put(url, true); //Adding to list so it doesn't get added again but not retrieving it to look for links
            result.addOutBoundLink(url);
		}
		return urlsToCheck;
	}
	
	private boolean isInBound(String url, URL baseUrl) {
		if (url.startsWith("http://") || url.startsWith("https://")) { //isAbsolute
			return url.contains(baseUrl.getHost());
		} else {
			return true;
		}
	}

	private void sleep(long milliSecondsToSleep) {
        try {
            Thread.sleep(milliSecondsToSleep);
        } catch (InterruptedException e) {
            logger.error("Error trying to sleep");
        }
    }

    public void getPopularWords(Document doc, int numberOfWords, PageAuditResult page) {
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
            Iterator iter = sortedMap.entrySet().iterator();
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
