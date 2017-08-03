package com.mcd.spider.main.engine.audit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.spider.main.entities.audit.AuditParameters;
import com.mcd.spider.main.entities.audit.AuditResults;
import com.mcd.spider.main.entities.audit.AuditSpider;
import com.mcd.spider.main.entities.audit.OfflineResponse;
import com.mcd.spider.main.entities.audit.PageAuditResult;
import com.mcd.spider.main.entities.audit.SearchResults;
import com.mcd.spider.main.entities.audit.Term;
import com.mcd.spider.main.util.ConnectionUtil;
import com.mcd.spider.main.util.SpiderUtil;
import com.redfin.sitemapgenerator.WebSitemapGenerator;

/**
 * 
 * @author u569220
 *
 */

public class AuditEngine {
	public static final Logger logger = Logger.getLogger(AuditEngine.class);
	
	private SpiderUtil spiderUtil = new SpiderUtil();
	private Map<String, Boolean> urlsToCrawl = new HashMap<>();
    private AuditSpider spider;
    
	
	public void performSEOAudit(AuditParameters auditParams) {
		urlsToCrawl = new HashMap<>();
		long timeSpent = 0;
		long startTime = 0;
		//empty string checks because parameters are optional
		//<url, checked>
		try {
			startTime = System.currentTimeMillis();
			spider = new AuditSpider(auditParams.getUrlToAudit(), spiderUtil.offline());
			spider.setSleepTime(auditParams.getSleepTime()==0?spiderUtil.offline()?0:2000:auditParams.getSleepTime()*1000);
			timeSpent+=System.currentTimeMillis()-startTime;
            sleep(spider.getSleepTime());
		} catch (IOException ioe) {
			logger.error("Exception initializing audit spider for " + auditParams.getUrlToAudit(), ioe);
			System.exit(0);
		}
		urlsToCrawl.put(auditParams.getUrlToAudit(),  false);
		spider.setTermsToSearch(auditParams.getTerms());
		
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
		retrieveSiteMap(auditParams.getUrlToAudit(), auditResults);
		//previous because adding to a listIterator adds before current elements
		while (iterator.hasPrevious()) {
			url = iterator.previous();
			checked = urlsToCrawl.get(url);
			if (url.contains("sitemap") && auditResults.getActualSiteMap()==null) {
	    		retrieveSiteMap(url, auditResults);
			} else if (!checked) {
				auditResults.addResponse(auditPage(url, iterator, spider));
                sleep(spider.getSleepTime());
			}
		}
		//}
		
		auditResults.setGeneratedSiteMap(generateSiteMap(spider.getBaseUrl().toString(), auditResults.getAllResponses()));
		
		spider.setAuditResults(auditResults);
		spider.setAveragePageLoadTime(timeSpent/urlsToCrawl.size());
		logger.info("\n\n\t\t\t****YOUR AUDIT RESULTS****\n\n" + spider.getAuditResults().prettyPrint(auditParams.isFullReportFlag()));
		
	}

	public PageAuditResult auditPage(String urlString, ListIterator<String> iterator, AuditSpider spider) {
		Elements ahrefs;
		Elements linkhrefs;
		PageAuditResult result = new PageAuditResult(urlString);
		long pageStartTime = System.currentTimeMillis();
		//need to catch response codes that don't return a document
		Connection.Response response = null;
		Document docToCheck = null;
		try {
			Connection conn = ConnectionUtil.getConnection(urlString, "");
			if (spiderUtil.offline()) {
				response = new OfflineResponse(200, urlString);
			} else {
				response = conn.execute();
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
		if (spiderUtil.docWasRetrieved(docToCheck)) {
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
			//TODO if the link is a sitemap and sitemap is not already set, set it here
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
    
    private void retrieveSiteMap(String url, AuditResults auditResults) {
    	Document actualSitemap = null;
		if (spider.getBaseUrl().toString().equals(url)) {
			//try to get from root domain first
			logger.debug("Checking for sitemap.xml at root");
	    	//try with either protocol
			Document sitemapDoc = spiderUtil.getHtmlAsDoc(url+"/sitemap.xml");
			if (!spiderUtil.docWasRetrieved(sitemapDoc)) {
				sitemapDoc = spiderUtil.getHtmlAsDoc(url.replace("https", "http")+"/sitemap.xml");
			}
			if (spiderUtil.docWasRetrieved(sitemapDoc) && (!sitemapDoc.select("sitemapindex").isEmpty() || !sitemapDoc.select("sitemap").isEmpty())) {
				actualSitemap = sitemapDoc;
				Elements sitemapTags = actualSitemap.select("sitemap");
				for (Element tag : sitemapTags) {
					Document specificSitemap = spiderUtil.getHtmlAsDoc(tag.getElementsByTag("loc").text());
					if (spiderUtil.docWasRetrieved(specificSitemap)) {
						tag.appendElement("sitemap").html(specificSitemap.html());
					}
				}
			} else if (spiderUtil.docWasRetrieved(sitemapDoc) && (!sitemapDoc.select("urlset").isEmpty() || !sitemapDoc.select("url").isEmpty())) {
				actualSitemap = sitemapDoc;
			}
		} else {
			//url contains sitemap
			
		}
		auditResults.setActualSiteMap(outputActualSitemap(actualSitemap));
    }
    
    private File generateSiteMap(String baseUrl, Set<PageAuditResult> resultPages) {
    	WebSitemapGenerator sitemapGenerator;
    	File sitemapDirectory = new File("output");
		try {
			sitemapGenerator = WebSitemapGenerator
			        .builder(baseUrl, sitemapDirectory)
			        .gzip(false).build();
		

    	    //WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options("").build();
    	    //sitemapGenerator.addUrl(sitemapUrl);
    	    
			for (PageAuditResult resultPage : resultPages) {
				if (resultPage.getUrl().startsWith(baseUrl)) {
					sitemapGenerator.addUrl(resultPage.getUrl());
				} else {
					
				}
			}
				
    	    sitemapGenerator.write();
	    } catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return new File(sitemapDirectory + "\\sitemap.xml");
    }
    
    private Document outputActualSitemap(Document actualSitemap) {
	    BufferedWriter  writer = null;
	    try {
	        writer = new BufferedWriter( new FileWriter("output//actualSitemap.xml"));
	        writer.write(actualSitemap.toString());
	    } catch ( IOException e) {
	    	logger.error("Error trying to save actual site map", e);
	    }
	    return actualSitemap;
    }

    public void getPopularWords(String url, int numberOfWords) {
		//TODO redirect to private method?
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
		if (spiderUtil.docWasRetrieved(doc)) {
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
	
    public void search(String url, String word, int levelOfGenerosity) {
    	//TODO redirect to private method?
    	Term term = new Term(word, 0);
    	String textToSearch;
    	Pattern p;
    	Matcher m = null;
    	Document doc = spiderUtil.getHtmlAsDoc(url);
		if (spiderUtil.docWasRetrieved(doc)) {
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
		}
    	if (m!=null){
    		while (m.find()) {
    			term.increment();
    		}
    	}
    	logger.debug(term.getWord() + " was found " + term.getCount() + " times");
    }
    
	public void getTextBySelector(String url, String selector) {
		//TODO redirect to private method?
		long time = System.currentTimeMillis();
		Document doc = spiderUtil.getHtmlAsDoc(url);
		if (spiderUtil.docWasRetrieved(doc)) {
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
