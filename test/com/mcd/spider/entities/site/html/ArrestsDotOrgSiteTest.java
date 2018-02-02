package com.mcd.spider.entities.site.html;

import com.mcd.spider.entities.record.State;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class ArrestsDotOrgSiteTest {

    static final Logger logger = Logger.getLogger(ArrestsDotOrgSiteTest.class);
    ArrestsDotOrgSite mockTexasSite = new ArrestsDotOrgSite(new String[]{State.TX.getName()});
    ArrestsDotOrgSite mockArizonaSite = new ArrestsDotOrgSite(new String[]{State.AZ.getName()});
    Document mockMainPageDoc;
    Document mockDetailDoc;

    @BeforeClass
    public void setUpClass() throws IOException {
        logger.info("********** Starting Test cases for ArrestsDotOrgSite *****************");
        System.setProperty("TestingSpider", "true");
        mockMainPageDoc = Jsoup.parse(new File("test/resources/htmls/mainPageDoc_ArrestsDotOrg.html"), "UTF-8");
        mockDetailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage_ArrestsDotOrg.html"), "UTF-8");
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
        Element mockRecordElement = new Element("a");
        mockRecordElement.attr("href", "/Arrests/Mikey_Johansen_33799480/?d=1");
        mockRecordElement.html("Mikey<br/>Johansen");
        Assert.assertEquals(mockTexasSite.getRecordDetailDocUrl(mockRecordElement), "https://texas.arrests.org/Arrests/Mikey_Johansen_33799480/?d=1");
    }

    @Test
    public void testGetRecordDetailDocUrl_NotAProperRecordElement() {
        Element mockRecordElement = new Element("a");
        mockRecordElement.attr("value", "/Arrests/Mikey_Johansen_33799480/?d=1");
        mockRecordElement.html("Mikey<br/>Johansen");
        Assert.assertEquals(mockTexasSite.getRecordDetailDocUrl(mockRecordElement), "");
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
    public void testObtainRecordId_Success() {
        String mockUrl = "https://texas.arrests.org/Arrests/Egroeg_Autsir_35373867/?d=1";
        Assert.assertEquals(mockTexasSite.obtainRecordId(mockUrl), "Egroeg_Autsir_35373867");
    }

    @Test
    public void testObtainRecordId_NoRecordIdinUrl() {
        String mockUrl = "https://texas.arrests.org/Celebrities/Will-Smith/";
        Assert.assertNull(mockTexasSite.obtainRecordId(mockUrl));
    }

    @Test
    public void testObtainDetailUrl() {
    	Assert.assertEquals(mockArizonaSite.obtainDetailUrl("Joe_Blow_32132342"), "https://arizona.arrests.org/Arrests/Joe_Blow_32132342/?d=1");
    }
}