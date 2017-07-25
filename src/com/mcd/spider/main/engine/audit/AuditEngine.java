package com.mcd.spider.main.engine.audit;

import com.mcd.spider.main.entities.audit.AuditSpider;
import com.mcd.spider.main.entities.audit.LinkResponse;
import com.mcd.spider.main.entities.audit.Term;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.EngineUtil;
import com.mcd.spider.main.util.SpiderUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ConnectException;
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
	
	
	public void performSEOAudit(String baseUrl, String terms, Integer depth, boolean performanceTest, int sleepTime) {
		long timeSpent = 0;
		long startTime = 0;
		int pagesAudited = 0;
		//empty string checks because parameters are optional
		AuditSpider spider = null;
		//<url, checked>
		Map<String, Boolean> urlsToCheck = new HashMap<>();
		try {
			startTime = System.currentTimeMillis();
			spider = new AuditSpider(baseUrl, spiderUtil.offline());
			spider.setSleepTime(sleepTime==0?spiderUtil.offline()?0:2000:sleepTime*1000);
			timeSpent+=System.currentTimeMillis()-startTime;
			Thread.sleep(spider.getSleepTime());
		} catch (IOException ioe) {
			logger.error("Exception initializing audit spider for " + baseUrl, ioe);
			System.exit(0);
		} catch (InterruptedException e) {
			logger.error("Error trying to sleep");
		}
		urlsToCheck.put(baseUrl,  true);
		pagesAudited++;
		
		spider.getLinkResponses().addResponse(new LinkResponse(spiderUtil.offline()?200:spider.getRootResponse().statusCode(), baseUrl));
		Elements hrefs = spider.getRootDocument().getElementsByAttribute("href");
		//create initial list of urlsToCheck
		for (Element element : hrefs) {
			//TODO if they're inbound, add to urlsToCheck and increment inboundLinks
			//TODO if outbound increment outboundLinks
			String url = element.attr("href");
			if (element.attr("rel")==null || element.attr("rel").equals("")) {
				if (url.startsWith("/") || url.contains(spider.getBaseUrl().getHost())) {
                    UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
                    if (!urlValidator.isValid(url)) {
                        url = spider.getBaseUrl()+url;
                    }
					urlsToCheck.put(url, false);
					spider.addInBoundLink(url);
				} else {
					//filter out some other bogus links
					urlsToCheck.put(url, true); //Adding to list so it doesn't get added again but not retrieving it to look for links
					spider.addOutBoundLink(url);
				}
			}
		}
		
		//STOP HERE if only checking first page
		
		//int depthLevel = 0;
		//while (depthLevel<=depth && allChecked(urlsToCheck)) { //allchecked() is inefficient for large sites
			//logger.debug("Depth = " + depthLevel);
		ListIterator<String> iterator = new ArrayList<>(urlsToCheck.keySet()).listIterator();
		while (iterator.hasNext()) {
			String url = iterator.next();
			boolean checked = urlsToCheck.get(url);
			if (!checked) {
				//TODO extract logic to get connection, response, doc and store results
				//TODO make it work offline
				startTime = System.currentTimeMillis();
				//need to catch response codes that don't return a page
				Connection.Response response = null;
				Document docToCheck = null;
				try {
					try {
						Connection conn = ConnectionUtil.getConnection(url, "");
						response = conn.execute();//create a dummy ResponseImpl for offline work
					} catch (ConnectException e) { //need to make more specific to avoid nullpointer
						logger.error("Exception caught getting response", e);
						spider.getLinkResponses().addResponse(new LinkResponse(509, url));
					}
					docToCheck = ConnectionUtil.getDocFromConnectionResponse(response, url);
					timeSpent+=System.currentTimeMillis()-startTime;
				} catch (IOException e) {
					logger.error("IOException caught getting document", e);
					spider.getLinkResponses().addResponse(new LinkResponse(509, url));
				}
				logger.debug("Trying to get hrefs from " + url);
				if (engineUtil.docWasRetrieved(docToCheck)) {
					hrefs = docToCheck.getElementsByAttribute("href");
					for (Element element : hrefs) {
						urlsToCheck = addToUrlsToCheck(element, urlsToCheck, iterator, spider);
					}
					urlsToCheck.put(url, true);
					pagesAudited++;
					if (response!=null) {//create a dummy ResponseImpl for offline work 
						spider.getLinkResponses().addResponse(new LinkResponse(response.statusCode(), url));
					}
				}
				
			}
		}
		//}
		spider.setAveragePageLoadTime(timeSpent/pagesAudited);
		logger.info("Inbound links count: " + spider.getInBoundLinksCount());
		for (String link : spider.getInBoundLinks()) {
			logger.debug("Inbound link: " + link);
		}
		logger.info("Outbound links count: " + spider.getOutBoundLinksCount());
		for (String link : spider.getOutBoundLinks()) {
			logger.debug("Outbound link: " + link);
		}
		logger.info("Number of responses: " + spider.getLinkResponses().count());
		for (LinkResponse response : spider.getLinkResponses().getAllResponses()) {
			logger.info("Response - " + response.getCode() + " - " + response.getUrl());
		}
		logger.info("Average load time per page: " + spider.getAveragePageLoadTime());
	}
	
	private Map<String,Boolean> addToUrlsToCheck(Element element, Map<String, Boolean> urlsToCheck, ListIterator<String> iterator, AuditSpider spider) {
		String url = element.attr("href");
		if (!element.attr("rel").toLowerCase().equals("stylesheet")) {
			if (url.startsWith("/") || url.contains(spider.getBaseUrl().getHost())) {
                UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
                if (!urlValidator.isValid(url)) {
                    url = spider.getBaseUrl()+url;
                    if  (!urlValidator.isValid(url)) {
                        return urlsToCheck;
                    }
                }
                if (urlsToCheck.get(url)==null) {
                    urlsToCheck.put(url, false);
                    iterator.add(url);
                    spider.addInBoundLink(url);
                }
			} else {
				//filter out some other bogus links
				urlsToCheck.put(url, true); //Adding to list so it doesn't get added again but not retrieving it to look for links
                spider.addOutBoundLink(url);
			}
		}
		return urlsToCheck;
	}
	
	public void getPopularWords(String url, int numberOfWords /*, int levelsDeep*/) {
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
