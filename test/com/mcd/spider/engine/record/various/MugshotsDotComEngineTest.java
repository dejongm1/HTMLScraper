package com.mcd.spider.engine.record.various;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.entities.site.html.MugshotsDotComSite;
import com.mcd.spider.util.io.RecordIOUtil;

public class MugshotsDotComEngineTest {

    private static Logger logger = Logger.getLogger(MugshotsDotComEngineTest.class);
    private MugshotsDotComEngine mockEngine;
    private SpiderWeb mockWeb;
    private Record mockAlcoholRecordOne;
    private Record mockAlcoholRecordTwo;
    private Record mockTrafficRecordOne;
    private Record mockTrafficRecordTwo;
    private Record mockTrafficRecordThree;
    private Document mockDetailDoc;
    private Document mockDetailDocVariation;
    private Document mockMainPageDoc;
    private String mainOutputPath;
    private String filteredOutputPath;
    private RecordIOUtil ioUtil;

    @BeforeClass
    public void setUpClass() throws IOException {
        logger.info("********** Starting Test cases for MugshotsDotComEngine *****************");
        System.setProperty("TestingSpider", "true");
        mockMainPageDoc = Jsoup.parse(new File("test/resources/htmls/mainPageDoc_MugshotsDotCom.html"), "UTF-8");
        mockDetailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage_MugshotsDotCom.html"), "UTF-8");
        mockDetailDocVariation = Jsoup.parse(new File("test/resources/htmls/recordDetailPage2_MugshotsDotCom.html"), "UTF-8");
        mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
        mockWeb.setSessionCookies(new HashMap<>());
        mockWeb.setCrawledIds(new HashSet<>());
        mockEngine = new MugshotsDotComEngine(mockWeb);
        ioUtil = new RecordIOUtil(State.IA.getName(), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"IA"}), true);
        mainOutputPath = ioUtil.getMainDocPath();
        filteredOutputPath = ioUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL);

        /*
        mockAlcoholRecordOne = new ArrestRecord();
        mockAlcoholRecordOne.setId("1231");
        ((ArrestRecord)mockAlcoholRecordOne).setFullName("Suzie Q Public");
        ((ArrestRecord)mockAlcoholRecordOne).setCounty("Reed");
        ((ArrestRecord)mockAlcoholRecordOne).setCharges(new String[]{"Resisting Arrest", "2#DUI"});

        mockAlcoholRecordTwo = new ArrestRecord();
        mockAlcoholRecordTwo.setId("1232");
        ((ArrestRecord)mockAlcoholRecordTwo).setFullName("John Q Public");
        ((ArrestRecord)mockAlcoholRecordTwo).setCounty("Polk");
        ((ArrestRecord)mockAlcoholRecordTwo).setCharges(new String[]{"1: Public Consumption"});

        mockTrafficRecordOne = new ArrestRecord();
        mockTrafficRecordOne.setId("2231");
        ((ArrestRecord) mockTrafficRecordOne).setFullName("Will W. Williams");
        ((ArrestRecord) mockTrafficRecordOne).setCounty("Guthrie");
//        ((ArrestRecord) mockTrafficRecordOne).setCharges(new String[]{"1) Battery", "2) Assault", "Psodomy"});

        mockTrafficRecordTwo = new ArrestRecord();
        mockTrafficRecordTwo.setId("2232");
        ((ArrestRecord) mockTrafficRecordTwo).setFullName("Billy Shatner");
        ((ArrestRecord) mockTrafficRecordTwo).setCounty("Polk");
//        ((ArrestRecord) mockTrafficRecordTwo).setCharges(new String[]{"Jay Walking", "Murder in the First"});

        mockTrafficRecordThree = new ArrestRecord();
        mockTrafficRecordThree.setId("2233");
        ((ArrestRecord) mockTrafficRecordThree).setFullName("JO Sonsimp");
        ((ArrestRecord) mockTrafficRecordThree).setCounty("Reed");
//        ((ArrestRecord) mockTrafficRecordThree).setCharges(new String[]{"Crusifixion"});*/
    }

    @AfterClass
    public void tearDownClass() {
        new File(mainOutputPath).delete();
        new File(filteredOutputPath).delete();
        ioUtil.getCrawledIdFile().delete();
        System.setProperty("TestingSpider", "false");
        logger.info("********** Finishing Test cases for MugshotsDotComEngine *****************");
    }

    @Test
    public void MugshotsDotComEngine_ConstructorWeb() {
        MugshotsDotComEngine mockEngine = new MugshotsDotComEngine(mockWeb);

        Assert.assertNotNull(mockEngine.getSpiderWeb());
        Assert.assertNotNull(mockEngine.getSite());
        Assert.assertEquals(mockEngine.getSpiderWeb().getMaxNumberOfResults(), mockWeb.getMaxNumberOfResults());
        Assert.assertEquals(mockEngine.getSpiderWeb().getFilter(), mockWeb.getFilter());
        Assert.assertEquals(mockEngine.getSpiderWeb().getState(), mockWeb.getState());
        Assert.assertEquals(mockEngine.getSpiderWeb().getMisc(), mockWeb.getMisc());
        Assert.assertEquals(mockEngine.getSpiderWeb().retrieveMissedRecords(), mockWeb.retrieveMissedRecords());
        Assert.assertNotNull(mockEngine.getSite());
        Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://mugshots.com");
    }

    @Test
    public void MugshotsDotComEngine_ConstructorStateName() {
        MugshotsDotComEngine mockEngine = new MugshotsDotComEngine(mockWeb);
        MugshotsDotComEngine mockTestEngine = new MugshotsDotComEngine(State.IA.getName());

        Assert.assertNotNull(mockEngine.getSite());
        Assert.assertNotNull(mockTestEngine.getSite());
        Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://mugshots.com");
        Assert.assertEquals(mockEngine.getSite().getBaseUrl(), mockTestEngine.getSite().getBaseUrl());
    }

    @Test
    public void testSetSpiderWeb() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetSpiderWeb() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetSite() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetRecordIOUtil() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testGetArrestRecords() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testScrapeSite() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testCompileResultsDocMap() {
        //TODO code is ready
        Assert.fail("Test not implemented");
    }

    @Test
    public void testParseDocForUrls() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testScrapeRecords() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testPopulateArrestRecord() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testMatchPropertyToField() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testInitiateConnection() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testInitializeIOUtil() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testSetCookies() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testFinalizeOutput() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testFormatName() {
    	Element profileDetail = mockDetailDoc.select("div.graybox > div.fieldvalues > div.field").get(1); //second div in test file is name
		ArrestRecord record = new ArrestRecord();
		
		mockEngine.formatName(record, profileDetail);
		
		Assert.assertEquals(record.getFullName(), "Salim NMN Visah");
		Assert.assertEquals(record.getFirstName(), "Salim");
		Assert.assertEquals(record.getLastName(), "Visah");
		Assert.assertEquals(record.getMiddleName(), "NMN");
    }

    @Test
    public void testFormatNameVariation() {
    	Element profileDetail = mockDetailDocVariation.select("div.graybox > div.fieldvalues > div.field").get(1); //second div in test file is name
		ArrestRecord record = new ArrestRecord();
		
		mockEngine.formatName(record, profileDetail);
		
		Assert.assertEquals(record.getFullName(), "Brain Clark Stan");
		Assert.assertEquals(record.getFirstName(), "Brain");
		Assert.assertEquals(record.getLastName(), "Stan");
		Assert.assertEquals(record.getMiddleName(), "Clark");
    }

    @Test
    public void testFormatName_VariationSuffix() {
		ArrestRecord record = new ArrestRecord();
    	Element profileDetail = new Element("div");
    	profileDetail.attr("class", "field");
    	profileDetail.html("<span class=\"name\">Name</span>: <span class=\"value\">Brain Clark Stan Jr</span>");
		
		mockEngine.formatName(record, profileDetail);

		Assert.assertEquals(record.getFullName(), "Brain Clark Stan Jr");
		Assert.assertEquals(record.getFirstName(), "Brain");
		Assert.assertEquals(record.getLastName(), "Stan Jr");
		Assert.assertEquals(record.getMiddleName(), "Clark");
    }

    @Test
    public void testFormatName_Suffix() {
		ArrestRecord record = new ArrestRecord();
    	Element profileDetail = new Element("div");
    	profileDetail.attr("class", "field");
    	profileDetail.html("<span class=\"name\">Name</span>: <span class=\"value\">Visah,Salim NMN Sr</span>");
		mockEngine.formatName(record, profileDetail);
		
		Assert.assertEquals(record.getFullName(), "Salim NMN Visah Sr");
		Assert.assertEquals(record.getFirstName(), "Salim");
		Assert.assertEquals(record.getLastName(), "Visah Sr");
		Assert.assertEquals(record.getMiddleName(), "NMN");
    }

    @Test
    public void testFormatName_Split() {
    	ArrestRecord record = new ArrestRecord();
    	Element profileDetail = new Element("div");
    	profileDetail.attr("class", "field");
    	profileDetail.html("<span class=\"name\">Last Name</span>: <span class=\"value\">JONES</span>");
    	Element profileDetail2 = new Element("div");
    	profileDetail2.attr("class", "field");
    	profileDetail2.html("<span class=\"name\">First Name</span>: <span class=\"value\">ROBERT</span>");
    	Element profileDetail3 = new Element("div");
    	profileDetail3.attr("class", "field");
    	profileDetail3.html("<span class=\"name\">Middle Name</span>: <span class=\"value\"></span>");
    	Element profileDetail4 = new Element("div");
    	profileDetail4.attr("class", "field");
    	profileDetail4.html("<span class=\"name\">Suffix</span>: <span class=\"value\">JR</span>");
    	mockEngine.formatName(record, profileDetail);
    	mockEngine.formatName(record, profileDetail2);
    	mockEngine.formatName(record, profileDetail3);
    	mockEngine.formatName(record, profileDetail4);

		Assert.assertEquals(record.getFullName(), "ROBERT JONES JR");
		Assert.assertEquals(record.getFirstName(), "ROBERT");
		Assert.assertEquals(record.getLastName(), "JONES JR");
		Assert.assertEquals(record.getMiddleName(), "");
    }
    
    @Test
    public void testFormatArrestTime() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testExtractValue() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testFilterRecords() {
        Assert.fail("Test not implemented");
    }
	
	@Test
	public void findAvailableCounties() {
		List<String> countiesList = mockEngine.findAvailableCounties();
		
		Assert.assertEquals(countiesList.size(), 2);
		Assert.assertEquals(countiesList.get(0), "Benton");
		Assert.assertEquals(countiesList.get(1), "Black Hawk");
	}
}