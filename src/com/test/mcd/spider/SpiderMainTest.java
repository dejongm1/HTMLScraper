package com.test.mcd.spider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.main.mcd.spider.util.SpiderUtil;

public class SpiderMainTest {
	
	SpiderUtil spiderUtil = new SpiderUtil();
	
	
	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}
	
	@Test
	public void f() {
	}
	/**
	 * Test no main() input args
	 * 
	 * For each scrape type
	 * 		Test 1 input
	 * 		Test 2 inputs
	 * 		Test 3 inputs
	 * 
	 * Test all variations of state input
	 * 		TX, IA, IL
	 * 		IL, AI
	 * 		RI, IA 
	 * 		All
	 * 		All, IA, IL
	 * 
	 * Test online
	 * Test offline
	 * Test sorting
	 * 
	 * Test empty <html><head><body></body></head></html> response
	 * 
	 * 	Test validators: positive and negative
	 * 
	 * Test scraping results with offline pages
	 * 
	 * Test for vague results for online pages, to verify HTML structure unchanged
	 * 
	 */
    public void testRandomConnections(int numberOfTries) {
//        long time = System.currentTimeMillis();
//        int trie = 0;
//        while (trie<numberOfTries) {
//            Document doc = spiderUtil.getHtmlAsDocTest("http://www.whoishostingthis.com/tools/user-agent/");
//            if (docWasRetrieved(doc)) {
//                Elements tags = doc.select("#user-agent .user-agent, #user-agent .ip");
//                for (Element tag : tags) {
//                    logger.debug(tag.text());
//                }
//            } else {
//                logger.error("Failed to load html for testing connection");
//            }
//            trie++;
//        }
//        time = System.currentTimeMillis() - time;
//        logger.info("Took " + time + " ms");
    }

}
