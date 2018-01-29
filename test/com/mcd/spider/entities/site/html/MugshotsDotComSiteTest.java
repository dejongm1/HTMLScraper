package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.record.State;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MugshotsDotComSiteTest {

    static final Logger logger = Logger.getLogger(MugshotsDotComSiteTest.class);
    MugshotsDotComSite mockTexasSite = new MugshotsDotComSite(new String[]{State.TX.getName()});
    MugshotsDotComSite mockArizonaSite = new MugshotsDotComSite(new String[]{State.AZ.getAbbreviation()});

    @BeforeClass
    public void setUpClass() {
        logger.info("********** Starting Test cases for MugshotsDotComSite *****************");
        System.setProperty("TestingSpider", "true");
    }

    @AfterClass
    public void tearDownClass() {
        System.setProperty("TestingSpider", "false");
        logger.info("********** Finishing Test cases for MugshotsDotComSite *****************");
    }
    @Test
    public void testConstructor_TexasFullName() {
        Assert.assertEquals(mockTexasSite.getBaseUrl(), "https://mugshots.com/US-Counties/Texas");
    }

    @Test
    public void testConstructor_ArizonaAbbreviation() {
        Assert.assertEquals(mockArizonaSite.getBaseUrl(), "https://mugshots.com/US-Counties/Arizona");
    }

    @Test
    public void testObtainRecordId_Success() {
        String mockUrl = "http://mugshots.com/US-Counties/Arizona/Scott-County-AZ/Bill-Allen-Manfree-Jr.141436546.html";
        Assert.assertEquals(mockArizonaSite.obtainRecordId(mockUrl), "Bill-Allen-Manfree-Jr.141436546");
    }

    @Test
    public void testObtainRecordId_NoRecordIDinUrl() {
        String mockUrl = "http://mugshots.com/Most-Wanted/Arizona/Scott-County-AZ/Bill-Allen-Manfree-Jr.141436546.html";
        Assert.assertNull(mockArizonaSite.obtainRecordId(mockUrl));
    }

    @Test
    public void testGetPerRecordSleepRange() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetRecordElements() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetRecordDetailDocUrl() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetRecordDetailElements() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGenerateResultsPageUrl_BlackHawkCountyIA() {
        MugshotsDotComSite mockIowaSite = new MugshotsDotComSite(new String[]{"Iowa"});
        Assert.assertEquals(mockIowaSite.generateResultsPageUrl("Black Hawk"), "https://mugshots.com/US-Counties/Iowa/Black-Hawk-County-IA");
    }

    @Test
    public void testGenerateResultsPageUrl_TravisCountyTX() {
        MugshotsDotComSite mockIowaSite = new MugshotsDotComSite(new String[]{"TX"});
        Assert.assertEquals(mockIowaSite.generateResultsPageUrl("Travis"), "https://mugshots.com/US-Counties/Texas/Travis-County-TX");
    }

    @Test
    public void testGetMiscSafeUrlsFromDoc() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testIsAResultsDoc() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testIsARecordDetailDoc() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testObtainDetailUrl() {
    	Assert.assertEquals(mockTexasSite.obtainDetailUrl("Joe-Blow-32132342"), "https://mugshots.com/US-Counties/Texas/Joe-Blow-32132342.html");
    }

    @Test
    public void testGetNextResultsPageUrl() {
        Assert.fail("Test not implemented");
    }
}