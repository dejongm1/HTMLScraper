package com.mcd.spider.main;

import com.mcd.spider.util.SpiderUtil;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Michael De Jong
 *
 */

public class SpiderMainTest {
	
	@Mock
	SpiderUtil spiderUtil;
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@BeforeClass
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	    System.getProperties().setProperty("runInEclipse", "true");
	}

	@AfterClass
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test(enabled=false)
	public void testMainNoArguments() throws IOException {
//		SpiderMain.main(new String[]{});
//		Assert.assertEquals(SpiderConstants.PROMPT, outContent.toString());
	}
	
	@Test(groups="offline")
	public void testMainFrequentWordArgsPassed() throws IOException {
		SpiderMain.main(new String[]{"1", "https://www.wikipedia.org", "10"});
		Assert.assertNotNull(outContent.toString());
		Assert.assertTrue(outContent.toString().contains("17	Wikipedia"));
		Assert.assertTrue(outContent.toString().contains("46	of"));
		Assert.assertTrue(outContent.toString().contains("35	in"));
	}
	
	/**
	 * test VM args??
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
	
	
	//belongs in a different test class 
    public void testRandomConnections(int numberOfTries) {
//        long time = System.currentTimeMillis();
//        int try = 0;
//        while (try<numberOfTries) {
//            Document doc = spiderUtil.getHtmlAsDocTest("http://www.whoishostingthis.com/tools/user-agent/");
//            if (docWasRetrieved(doc)) {
//                Elements tags = doc.select("#user-agent .user-agent, #user-agent .ip");
//                for (Element tag : tags) {
//                    logger.debug(tag.text());
//                }
//            } else {
//                logger.error("Failed to load html for testing connection");
//            }
//            try++;
//        }
//        time = System.currentTimeMillis() - time;
//        logger.info("Took " + time + " ms");
    }

}
