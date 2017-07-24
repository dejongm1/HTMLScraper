package com.mcd.spider.main.engine.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.entities.audit.AuditSpider;
import com.mcd.spider.main.entities.audit.LinkResponse;
import com.mcd.spider.main.entities.audit.Term;
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
	
	
	public void performSEOAudit(String baseUrl, String terms, Integer depth) {
		long timeSpent = 0;
		long startTime = 0;
		int pagesAudited = 0;
		//empty string checks because all parameters are optional
		AuditSpider spider = null;
		//<url, checked>
		Map<String, Boolean> urlsToCheck = new HashMap<>();
		try {
			startTime = System.currentTimeMillis();
			spider = new AuditSpider(baseUrl, spiderUtil.offline());
			timeSpent+=System.currentTimeMillis()-startTime;
		} catch (IOException ioe) {
			logger.error("Exception initializing audit spider for " + baseUrl, ioe);
			System.exit(0);
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
				startTime = System.currentTimeMillis();
				Document docToCheck = spiderUtil.getHtmlAsDoc(url);
				timeSpent+=System.currentTimeMillis()-startTime;
				
				if (engineUtil.docWasRetrieved(docToCheck)) {
					hrefs = docToCheck.getElementsByAttribute("href");
					for (Element element : hrefs) {
						urlsToCheck = addToUrlsToCheck(element, urlsToCheck, iterator, spider);
					}
					urlsToCheck.put(url, true);
					pagesAudited++;
				}
			}
		}
		//}
		spider.setAveragePageLoadTime(timeSpent/pagesAudited);
		logger.info("Inbound links count: " + spider.getInBoundLinksCount());
		for (String link : spider.getInBoundLinks()) {
			logger.info("Inbound link: " + link);
		}
		logger.info("Outbound links count: " + spider.getOutBoundLinksCount());
		for (String link : spider.getOutBoundLinks()) {
			logger.info("Outbound link: " + link);
		}
		logger.info("Average load time per page: " + spider.getAveragePageLoadTime());
		logger.info("Number of responses: " + spider.getLinkResponses().count());
	}
	
	private Map<String,Boolean> addToUrlsToCheck(Element element, Map<String, Boolean> urlsToCheck, ListIterator<String> iterator, AuditSpider spider) {
		//TODO if they're inbound, add to urlsToCheck and increment inboundLinks
		//TODO if outbound increment outboundLinks
		String url = element.attr("href");
		if (!element.attr("rel").toLowerCase().equals("stylesheet") && urlsToCheck.get(url)==null) {
			if (url.startsWith("/") || url.contains(spider.getBaseUrl().getHost())) {
				urlsToCheck.put(url, false);
				iterator.add(url);
				spider.addInBoundLink(url);
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
