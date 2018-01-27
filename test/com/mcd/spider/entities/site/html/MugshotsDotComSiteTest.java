package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.record.State;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MugshotsDotComSiteTest {

    static final Logger logger = Logger.getLogger(MugshotsDotComSiteTest.class);
    MugshotsDotComSite mockTexasSite = new MugshotsDotComSite(new String[]{State.TX.getName(), State.TX.getAbbreviation()});
    MugshotsDotComSite mockArizonaSite = new MugshotsDotComSite(new String[]{State.AZ.getName(), State.AZ.getAbbreviation()});

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
    public void testConstructor_Texas() {
        Assert.assertEquals(mockTexasSite.getBaseUrl(), "https://mugshots.com/US-Counties/Texas/-County-TX/");
    }

    @Test
    public void testConstructor_Arizona() {
        Assert.assertEquals(mockArizonaSite.getBaseUrl(), "https://mugshots.com/US-Counties/Arizona/-County-AZ/");
    }

    @Test
    public void testObtainRecordId() {
        Assert.fail("Test not implemented");
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
    public void testGenerateResultsPageUrl() {
        Assert.fail("Test not implemented");
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
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetNextResultsPageUrl() {
        Assert.fail("Test not implemented");
    }
}