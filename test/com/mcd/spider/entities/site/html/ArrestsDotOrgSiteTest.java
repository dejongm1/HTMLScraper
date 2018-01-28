package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.record.State;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ArrestsDotOrgSiteTest {

    static final Logger logger = Logger.getLogger(ArrestsDotOrgSiteTest.class);
    ArrestsDotOrgSite mockTexasSite = new ArrestsDotOrgSite(new String[]{State.TX.getName()});
    ArrestsDotOrgSite mockArizonaSite = new ArrestsDotOrgSite(new String[]{State.AZ.getName()});

    @BeforeClass
    public void setUpClass() {
        logger.info("********** Starting Test cases for ArrestsDotOrgSite *****************");
        System.setProperty("TestingSpider", "true");
    }

    @AfterClass
    public void tearDownClass() {
        System.setProperty("TestingSpider", "false");
        logger.info("********** Finishing Test cases for ArrestsDotOrgSite *****************");
    }

    @Test
    public void testConstructor_Texas() {
        Assert.assertEquals(mockTexasSite.getBaseUrl(), "https://texas.arrests.org");
    }

    @Test
    public void testConstructor_Arizona() {
        Assert.assertEquals(mockArizonaSite.getBaseUrl(), "https://arizona.arrests.org");
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
    public void testGetTotalPages() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGenerateResultsPageUrl_TexasPage2() {
        Assert.assertEquals(mockTexasSite.generateResultsPageUrl("2"), "https://texas.arrests.org/?page=2&results=56");
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
    public void testObtainRecordId() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testObtainDetailUrl() {
        Assert.fail("Test not implemented");
    }
}