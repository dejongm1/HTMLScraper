package com.mcd.spider.engine.record.various;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.OfflineResponse;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.util.io.RecordIOUtil;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ArrestsDotOrgEngineTest {

	private static Logger logger = Logger.getLogger(ArrestsDotOrgEngineTest.class);
	private ArrestsDotOrgEngine mockEngine;
	private SpiderWeb mockWeb;
	private Record mockAlcoholRecordOne;
	private Record mockAlcoholRecordTwo;
	private Record mockViolentRecordOne;
	private Record mockViolentRecordTwo;
	private Record mockViolentRecordThree;
	private Document mockDetailDoc;
	private Document mockMainPageDoc;
	private String mainOutputPath;
	private String filteredOutputPath;
	private RecordIOUtil ioUtil;
	
	
	@BeforeClass
	public void setUpClass() throws IOException {
		logger.info("********** Starting Test cases for ArrestsDotOrgEngine *****************");
		System.setProperty("TestingSpider", "true");
		mockMainPageDoc = Jsoup.parse(new File("test/resources/htmls/mainPageDoc_ArrestsDotOrg.html"), "UTF-8");
		mockDetailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage_ArrestsDotOrg.html"), "UTF-8");
		mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setSessionCookies(new HashMap<>());
		mockWeb.setCrawledIds(new HashSet<>());
		mockEngine = new ArrestsDotOrgEngine(mockWeb);
		ioUtil = new RecordIOUtil(State.IA.getName(), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"IA"}), true);
		mainOutputPath = ioUtil.getMainDocPath();
		filteredOutputPath = ioUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL);

		
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

		mockViolentRecordOne = new ArrestRecord();
		mockViolentRecordOne.setId("2231");
		((ArrestRecord)mockViolentRecordOne).setFullName("Will W. Williams");
		((ArrestRecord)mockViolentRecordOne).setCounty("Guthrie");
		((ArrestRecord)mockViolentRecordOne).setCharges(new String[]{"1) Battery", "2) Assault", "Psodomy"});
		
		mockViolentRecordTwo = new ArrestRecord();
		mockViolentRecordTwo.setId("2232");
		((ArrestRecord)mockViolentRecordTwo).setFullName("Billy Shatner");
		((ArrestRecord)mockViolentRecordTwo).setCounty("Polk");
		((ArrestRecord)mockViolentRecordTwo).setCharges(new String[]{"Jay Walking", "Murder in the First"});
		
		mockViolentRecordThree = new ArrestRecord();
		mockViolentRecordThree.setId("2233");
		((ArrestRecord)mockViolentRecordThree).setFullName("JO Sonsimp");
		((ArrestRecord)mockViolentRecordThree).setCounty("Reed");
		((ArrestRecord)mockViolentRecordThree).setCharges(new String[]{"Crusifixion"});
	}

	@AfterClass
	public void tearDownClass() {
		new File(mainOutputPath).delete();
		new File(filteredOutputPath).delete();
		ioUtil.getCrawledIdFile().delete();
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for ArrestsDotOrgEngine *****************");
	}

	@Test
	public void ArrestsDotOrgEngine_ConstructorWeb() {
	    ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);

	    Assert.assertNotNull(mockEngine.getSpiderWeb());
	    Assert.assertNotNull(mockEngine.getSite());
	    Assert.assertEquals(mockEngine.getSpiderWeb().getMaxNumberOfResults(), mockWeb.getMaxNumberOfResults());
	    Assert.assertEquals(mockEngine.getSpiderWeb().getFilter(), mockWeb.getFilter());
	    Assert.assertEquals(mockEngine.getSpiderWeb().getState(), mockWeb.getState());
	    Assert.assertEquals(mockEngine.getSpiderWeb().getMisc(), mockWeb.getMisc());
	    Assert.assertEquals(mockEngine.getSpiderWeb().retrieveMissedRecords(), mockWeb.retrieveMissedRecords());
	    Assert.assertNotNull(mockEngine.getSite());
	    Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://iowa.arrests.org");
	}

	@Test
	public void ArrestsDotOrgEngine_ConstructorStateName() {
	    ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
	    ArrestsDotOrgEngine mockTestEngine = new ArrestsDotOrgEngine(State.IA.getName());
		
	    Assert.assertNotNull(mockEngine.getSite());
	    Assert.assertNotNull(mockTestEngine.getSite());
	    Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://iowa.arrests.org");
	    Assert.assertEquals(mockEngine.getSite().getBaseUrl(), mockTestEngine.getSite().getBaseUrl());
	}

	@Test
	public void compileRecordDetailUrlMap() throws IOException {
		Map<Integer, Document> resultsPageDocMap = new HashMap<>();
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=2&results=56"), "UTF-8"));
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		
		Map<Object, String> resultMap = mockEngine.compileRecordDetailUrlMap(mockMainPageDoc, resultsPageDocMap);
		
		Assert.assertTrue(resultMap.size() > resultsPageDocMap.size()*56);
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileRecordDetailUrlMap_NoMisc() throws IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, false, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setCrawledIds(new HashSet<>());
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Integer, Document> resultsPageDocMap = new HashMap<>();
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=2&results=56"), "UTF-8"));
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		
		Map<Object, String> resultMap = mockEngine.compileRecordDetailUrlMap(mockMainPageDoc, resultsPageDocMap);
		
		Assert.assertEquals(resultMap.size(), resultsPageDocMap.size()*56);
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileRecordDetailUrlMap_SomeAlreadyCrawled() throws IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, false, false, RecordFilterEnum.NONE, State.IA);
		Set<String> crawledIds = new HashSet<>();
		crawledIds.add("Justin_Wilde_33799480");
		crawledIds.add("Craig_Mitchell_33793872");
		crawledIds.add("Brett_Wilkins_33797797");
		mockWeb.setCrawledIds(crawledIds);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(new RecordIOUtil(State.IA.getName(), new ArrestRecord(), mockEngine.getSite(), true));
		Map<Integer, Document> resultsPageDocMap = new HashMap<>();
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpsiowa.arrests.orgpage=2&results=56"), "UTF-8"));
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		
		Map<Object, String> resultMap = mockEngine.compileRecordDetailUrlMap(mockMainPageDoc, resultsPageDocMap);
		
		Assert.assertEquals(resultMap.size(), resultsPageDocMap.size()*56-crawledIds.size());
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileRecordDetailUrlMapFromBackup() throws IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setCrawledIds(new HashSet<>());
		Set<String> uncrawledIds = new HashSet<>();
		uncrawledIds.add("Joe_Blow_123123");
		uncrawledIds.add("Stacy_Cooper_0923023");
		uncrawledIds.add("Ferdinand_Egnacios_891382");
		mockWeb.setUncrawledIds(uncrawledIds);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(new RecordIOUtil(State.IA.getName(), new ArrestRecord(), mockEngine.getSite(), true));
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		File uncrawledIDFile = new File(mockEngine.getRecordIOUtil().getUncrawledIdFile().getPath());
		uncrawledIDFile.createNewFile();
		
		Map<Object, String> resultMap = mockEngine.compileRecordDetailUrlMapFromBackup(mockMainPageDoc, uncrawledIds);
		
		Assert.assertTrue(resultMap.size() > uncrawledIds.size());
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
		Assert.assertFalse(mockEngine.getRecordIOUtil().getUncrawledIdFile().exists());
	}

	@Test
	public void compileResultsUrlMap() {
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(mockEngine.getNumberOfResultsPages(mockMainPageDoc));
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Object,String> resultMap = mockEngine.compileResultsUrlMap(mockMainPageDoc);
		
		Assert.assertTrue(resultMap.size() > mockWeb.getNumberOfPages());
		Assert.assertTrue(resultMap.size() <= mockWeb.getNumberOfPages()*2);
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileResultsUrlMap_Limit4ResultsPages() {
		SpiderWeb mockWeb = new SpiderWeb(56*4, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(4);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		Map<Object,String> resultMap = mockEngine.compileResultsUrlMap(mockMainPageDoc);
		
		Assert.assertTrue(resultMap.size() > 4);
		Assert.assertTrue(resultMap.size() <= mockWeb.getNumberOfPages()*2);
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileResultsUrlMap_NoMisc() {
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});		
		SpiderWeb mockWeb = new SpiderWeb(9999, false, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(mockEngine.getNumberOfResultsPages(mockMainPageDoc));
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Object,String> resultMap = mockEngine.compileResultsUrlMap(mockMainPageDoc);
		
		Assert.assertTrue(resultMap.size() == mockWeb.getNumberOfPages());
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void compileResultsDocMap_Online() {
		//override sleeptime to expedite tests
		

//		Assert.assertEquals(resultUrlMap.size(), resultUrlMap.size());
		throw new RuntimeException("Test not implemented");
	}
	
	@Test
	public void compileResultsDocMap_NoneCrawled() {
	    OfflineResponse mockResponse = new OfflineResponse(200, "www.google.com");
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(mockEngine.getNumberOfResultsPages(mockMainPageDoc));
		mockWeb.setSessionCookies(mockResponse.cookies());
		mockWeb.setCrawledIds(new HashSet<>());
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Object,String> resultUrlMap = mockEngine.compileResultsUrlMap(mockMainPageDoc);
		Map<Integer,Document> resultDocMap = mockEngine.compileResultsDocMap(resultUrlMap);
		
		Assert.assertEquals(resultDocMap.size(), 2);//only have 2 html pages saved
		Assert.assertEquals(mockEngine.getSpiderWeb().getFurthestPageToCheck(), 9999);
		Assert.assertEquals(mockEngine.getSpiderWeb().getAttemptCount(), 1);
	}
	
	@Test
	public void compileResultsDocMap_PreviousRecordCrawled() {
		OfflineResponse mockResponse = new OfflineResponse(200, "www.google.com");
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(mockEngine.getNumberOfResultsPages(mockMainPageDoc));
		mockWeb.setSessionCookies(mockResponse.cookies());
		Set<String> crawledRecords = new HashSet<>();
		crawledRecords.add("Mark_Pitt_33798910"); //in results page 1
		mockWeb.setCrawledIds(crawledRecords);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Object,String> resultUrlMap = mockEngine.compileResultsUrlMap(mockMainPageDoc);
		Map<Integer,Document> resultDocMap = mockEngine.compileResultsDocMap(resultUrlMap);
		
		Assert.assertEquals(mockEngine.getSpiderWeb().getFurthestPageToCheck(), 1);
		Assert.assertEquals(mockEngine.getSpiderWeb().getAttemptCount(), 1);
	}
	
	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void compileResultsDocMap_FailedAttemptsMaxReached_Online() {
		OfflineResponse mockResponse = new OfflineResponse(200, "www.google.com");
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockWeb.setNumberOfPages(mockEngine.getNumberOfResultsPages(mockMainPageDoc));
		mockWeb.setSessionCookies(mockResponse.cookies());
		mockWeb.setCrawledIds(new HashSet<>());
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		Map<Object,String> resultUrlMap = new HashMap<>();
		resultUrlMap.put(1, "https://www.thisisabadURL.com");
		resultUrlMap.put(2, "https://www.thisisanotherbadURL.com");
		resultUrlMap.put(3, "https://www.badURL3.com");
		resultUrlMap.put(4, "https://www.fourthbadURL.html");
		resultUrlMap.put(5, "https://www.lastbadURL.html");
		Map<Integer,Document> resultDocMap = mockEngine.compileResultsDocMap(resultUrlMap);

		Assert.assertEquals(resultDocMap.size(), 0);
		Assert.assertEquals(mockEngine.getSpiderWeb().getFurthestPageToCheck(), 9999);
		Assert.assertEquals(mockEngine.getSpiderWeb().getAttemptCount(), 5);
	}

	@Test
	public void extractValue() {
		Element mockElement = new Element("div");
		mockElement.append("<b>Full Name:</b> <span itemprop=\"name\"><span itemprop=\"givenName\">Brad</span><span itemprop=\"additionalName\"> Roderick</span><span itemprop=\"familyName\"> Smith</span></span>");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "Brad Roderick Smith");
		mockElement = new Element("div");
		mockElement.append("<b>Date:</b><time datetime=\"2017-08-24\">08/24/2017</time>");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "08/24/2017");
		mockElement = new Element("div");
		mockElement.append("<b>Time:</b> 6:15 PM");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "6:15 PM");
		mockElement = new Element("div");
		mockElement.append("<b>Total Bond:</b> $300");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "$300");
		mockElement = new Element("div");
		mockElement.append("<b class=\"property\">Arrest Age:</b><span itemprop=\"age\">21</span>");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "21");
		mockElement = new Element("div");
		mockElement.append("<b class=\"property\">Height:</b> 5&#39;10&quot;");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "5'10\"");
		mockElement = new Element("div");
		mockElement.append("<b class=\"property\">Hair Color:</b> Black");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "Black");
		mockElement = new Element("div");
		mockElement.append("<b class=\"property\">Weight:</b> 200 lbs");
		Assert.assertEquals(mockEngine.extractValue(mockElement), "200 lbs");
	}

	@Test
	public void filterRecords_Alcohol() {
		SpiderWeb web = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine engine = new ArrestsDotOrgEngine(web);
		List<Record> oneMatchingAlcoholRecord = new ArrayList<>();
		List<Record> twoMatchingAlcoholRecord = new ArrayList<>();
		List<Record> noMatchingAlcoholRecord = new ArrayList<>();
		
		oneMatchingAlcoholRecord.add(mockAlcoholRecordOne);
		twoMatchingAlcoholRecord.add(mockAlcoholRecordOne);
		twoMatchingAlcoholRecord.add(mockAlcoholRecordTwo);
		twoMatchingAlcoholRecord.add(mockViolentRecordOne);
		noMatchingAlcoholRecord.add(mockViolentRecordOne);
		noMatchingAlcoholRecord.add(mockViolentRecordThree);
		noMatchingAlcoholRecord.add(mockViolentRecordTwo);
		

		Assert.assertEquals(engine.filterRecords(oneMatchingAlcoholRecord).size(), 1);
		Assert.assertTrue(engine.filterRecords(oneMatchingAlcoholRecord).contains(mockAlcoholRecordOne));
		Assert.assertEquals(engine.filterRecords(twoMatchingAlcoholRecord).size(), 2);
		Assert.assertTrue(engine.filterRecords(twoMatchingAlcoholRecord).contains(mockAlcoholRecordOne));
		Assert.assertTrue(engine.filterRecords(twoMatchingAlcoholRecord).contains(mockAlcoholRecordTwo));
		Assert.assertFalse(engine.filterRecords(twoMatchingAlcoholRecord).contains(mockViolentRecordOne));
		Assert.assertEquals(engine.filterRecords(noMatchingAlcoholRecord).size(), 0);
		Assert.assertFalse(engine.filterRecords(noMatchingAlcoholRecord).contains(mockViolentRecordOne));
	}

	@Test(groups="output")
	public void finalizeOutput() throws SpiderException {
		List<Record> mockRecords = new ArrayList<>();
		mockRecords.add(mockAlcoholRecordOne);
		mockRecords.add(mockAlcoholRecordTwo);
		mockRecords.add(mockViolentRecordOne);
		mockRecords.add(mockViolentRecordTwo);
		mockRecords.add(mockViolentRecordThree);
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
		long mainDocLengthBefore = new File(mockEngine.getRecordIOUtil().getMainDocPath()).length();
		mockEngine.finalizeOutput(mockRecords);
		
		Assert.assertTrue(new File(mockEngine.getRecordIOUtil().getMainDocPath()).exists());
		Assert.assertTrue(mainDocLengthBefore < new File(mockEngine.getRecordIOUtil().getMainDocPath()).length());
		Assert.assertTrue(new File(mockEngine.getRecordIOUtil().getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL)).exists());
		
		new File(mockEngine.getRecordIOUtil().getMainDocPath()).delete();
		new File(mockEngine.getRecordIOUtil().getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL)).delete();
	}

	@Test(groups="output")
	public void finalizeOutput_NoFilter() throws SpiderException {
		List<Record> mockRecords = new ArrayList<>();
		mockRecords.add(mockAlcoholRecordOne);
		mockRecords.add(mockAlcoholRecordTwo);
		mockRecords.add(mockViolentRecordOne);
		mockRecords.add(mockViolentRecordTwo);
		mockRecords.add(mockViolentRecordThree);
		
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
		long mainDocLengthBefore = new File(mockEngine.getRecordIOUtil().getMainDocPath()).length();
		mockEngine.finalizeOutput(mockRecords);
		
		Assert.assertTrue(new File(mockEngine.getRecordIOUtil().getMainDocPath()).exists());
		Assert.assertTrue(mainDocLengthBefore < new File(mockEngine.getRecordIOUtil().getMainDocPath()).length());
		Assert.assertFalse(new File(mockEngine.getRecordIOUtil().getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL)).exists());
		
		new File(mockEngine.getRecordIOUtil().getMainDocPath()).delete();
	}

	@Test(groups="output")
	public void finalizeOutput_NoFilteredRecords() throws SpiderException {
		List<Record> mockRecords = new ArrayList<>();
//		Record mockRecord4 = new ArrestRecord();
//		Record mockRecord5 = new ArrestRecord();
//		mockRecord4.setId("232re32");
//		mockRecord5.setId("232redsgsf");
		mockRecords.add(mockViolentRecordOne);
		mockRecords.add(mockViolentRecordTwo);
		mockRecords.add(mockViolentRecordThree);
//		mockRecords.add(mockRecord4);
//		mockRecords.add(mockRecord5);
		
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
		long mainDocLengthBefore = new File(mockEngine.getRecordIOUtil().getMainDocPath()).length();
		mockEngine.finalizeOutput(mockRecords);
		
		long temp = new File(mockEngine.getRecordIOUtil().getMainDocPath()).length();
		Assert.assertTrue(new File(mockEngine.getRecordIOUtil().getMainDocPath()).exists());
		Assert.assertTrue(mainDocLengthBefore < temp);
		Assert.assertFalse(new File(mockEngine.getRecordIOUtil().getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL)).exists());

		new File(mockEngine.getRecordIOUtil().getMainDocPath()).delete();
	}

	@Test(groups="output")
	public void finalizeOutput_NoRecords() throws SpiderException {
		List<Record> mockRecords = new ArrayList<>();
		
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
		long mainDocLengthBefore = new File(mockEngine.getRecordIOUtil().getMainDocPath()).length();
		mockEngine.finalizeOutput(mockRecords);

		Assert.assertEquals(mainDocLengthBefore, new File(mockEngine.getRecordIOUtil().getMainDocPath()).length());
		Assert.assertFalse(new File(mockEngine.getRecordIOUtil().getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL)).exists());

		new File(mockEngine.getRecordIOUtil().getMainDocPath()).delete();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void formatArrestTime() {
		Element profileDetail = mockDetailDoc.select(".info .section-content div").get(2);  //third div in test file is arrest time
		ArrestRecord record = new ArrestRecord();
		Calendar arrestCalendar = Calendar.getInstance();
		arrestCalendar.setTime(new Date("09/14/2017"));
		record.setArrestDate(arrestCalendar);
		
		mockEngine.formatArrestTime(record, profileDetail);
		
		Assert.assertEquals(record.getArrestDate().get(Calendar.HOUR), 6);
		Assert.assertEquals(record.getArrestDate().get(Calendar.MINUTE), 15);
		Assert.assertEquals(record.getArrestDate().get(Calendar.AM_PM), Calendar.PM);
	}

	@Test
	public void formatName() {
		Element profileDetail = mockDetailDoc.select(".info .section-content div").get(0); //first div in test file is name
		ArrestRecord record = new ArrestRecord();
		
		mockEngine.formatName(record, profileDetail);
		
		Assert.assertEquals(record.getFullName(), "Brad Roderick Smith");
		Assert.assertEquals(record.getFirstName(), "Brad");
		Assert.assertEquals(record.getLastName(), "Smith");
		Assert.assertEquals(record.getMiddleName(), "Roderick");
	}

	@Test
	public void getArrestRecords() {
        Assert.fail("Test not implemented");
	}

	@Test
	public void getNumberOfResultsPages() {
		Assert.assertEquals(mockEngine.getNumberOfResultsPages(mockMainPageDoc), 18);
	}

	@Test
	public void getNumberOfResultsPages_maxResults50() {
		SpiderWeb web = new SpiderWeb(50, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine engine = new ArrestsDotOrgEngine(web);

		Assert.assertEquals(engine.getNumberOfResultsPages(mockMainPageDoc), 1);
	}

	@Test
	public void getNumberOfResultsPages_retrieveMissedMax50() {
		SpiderWeb web = new SpiderWeb(50, true, true, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine engine = new ArrestsDotOrgEngine(web);
		
		Assert.assertEquals(engine.getNumberOfResultsPages(mockMainPageDoc), 18);
	}

	@Test
	public void getSite() {
		Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://iowa.arrests.org");
		Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "https://" + mockWeb.getState().getName().toLowerCase() + ".arrests.org");
	}

	@Test
	public void initializeIOUtil() throws SpiderException {
		//mock a tracking file with 3 records
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(ioUtil.getCrawledIdFile(), true);
			bw = new BufferedWriter(fw);
			bw.write("Dominic_Lacava_34065316");
			bw.newLine();
			bw.write("Justin_Wilde_33799480");
			bw.newLine();
			bw.write("Joe_Smith_32791487");
			bw.newLine();
		} catch (IOException e) {
			Assert.fail("Failure setting up tracking file for test");
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				Assert.fail("Failure closing tracking file for test");
			}
		}
		
	    ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
	    RecordIOUtil mockIOUtil = mockEngine.initializeIOUtil(State.IA.getName());

		Assert.assertTrue(mockIOUtil.getMainDocPath().contains(State.IA.getName()));
		Assert.assertEquals(mockEngine.getSpiderWeb().getCrawledIds().size(), 3);
		Assert.assertNull(mockEngine.getSpiderWeb().getUncrawledIds());
		Assert.assertNotNull(mockEngine.getSpiderWeb().getCrawledRecords());
		Assert.assertTrue(new File(mockIOUtil.getMainDocPath()).exists());
		Assert.assertTrue(new File(mockIOUtil.getMainDocPath()).delete());
        Assert.assertTrue(mockIOUtil.getCrawledIdFile().delete());
    }

	@Test
	public void initializeIOUtil_NoneCrawledRetrieveUncrawled() throws SpiderException {
		//create uncrawled IDs file with 1 record
		RecordIOUtil ioUtil = new RecordIOUtil(State.OK.getName(), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"OK"}), true);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(ioUtil.getUncrawledIdFile(), true);
			bw = new BufferedWriter(fw);
			bw.write("Thomas_Reiling_33796661");
			bw.newLine();
		} catch (IOException e) {
			Assert.fail("Failure setting up tracking file for test");
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				Assert.fail("Failure closing tracking file for test");
			}
		}
				
		SpiderWeb mockWeb = new SpiderWeb(9999, true, true, RecordFilterEnum.NONE, State.OK);
	    ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
	    RecordIOUtil mockIOUtil = mockEngine.initializeIOUtil(State.OK.getName());
	    
		Assert.assertTrue(mockIOUtil.getMainDocPath().contains(State.OK.getName()));
		Assert.assertEquals(mockEngine.getSpiderWeb().getCrawledIds().size(), 0);
		Assert.assertEquals(mockEngine.getSpiderWeb().getUncrawledIds().size(), 1);
		Assert.assertNotNull(mockEngine.getSpiderWeb().getCrawledRecords());
		Assert.assertTrue(new File(mockIOUtil.getMainDocPath()).exists());
		Assert.assertTrue(new File(mockIOUtil.getMainDocPath()).delete());
		Assert.assertTrue(mockIOUtil.getUncrawledIdFile().delete());
		Assert.assertTrue(mockIOUtil.getCrawledIdFile().delete());
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void initiateConnection_Online() throws IOException {
		Object mainDoc = mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
		
		Assert.assertTrue(mainDoc instanceof Document);
		Assert.assertEquals(mockEngine.getSpiderWeb().getHeaders().size(), 7);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().size()>0);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("PHPSESSID")!=null);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("views_session")!=null);
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordElements((Document)mainDoc).size()>0);
		Assert.assertFalse(mainDoc.toString().contains("flibberdigibit"));//should not exist on live site
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void initiateConnection() throws IOException {
		Object mainDoc = mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
		
		Assert.assertTrue(mainDoc instanceof Document);
		Assert.assertEquals(mockEngine.getSpiderWeb().getHeaders().size(), 7);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().size()>0);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("PHPSESSID")!=null);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("views_session")!=null);
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordElements((Document)mainDoc).size()>0);
		Assert.assertTrue(mainDoc.toString().contains("flibberdigibit"));
	}

	@Test
	public void matchPropertyToField_Misc() {
		ArrestRecord recordToMap = new ArrestRecord();
		//pulled the selector from Site.getRecordDetailElements()
		Elements profileDetailSections = mockDetailDoc.select(".info .section-content div");
		Calendar mockCalendar = Calendar.getInstance(); //setting a test date of 08/24/2017 6:15 PM
		mockCalendar.setTime(new Date("08/24/2017"));
		mockCalendar.set(Calendar.HOUR, 6);
		mockCalendar.set(Calendar.MINUTE, 15);
		mockCalendar.set(Calendar.AM_PM, Calendar.PM);
		for (Element profileElement : profileDetailSections) {
			mockEngine.matchPropertyToField(recordToMap, profileElement);
		}
		
		Assert.assertEquals(recordToMap.getFullName(), "Brad Roderick Smith");
		Assert.assertEquals(recordToMap.getFirstName(), "Brad");
		Assert.assertEquals(recordToMap.getMiddleName(), "Roderick");
		Assert.assertEquals(recordToMap.getLastName(), "Smith");
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.HOUR), mockCalendar.get(Calendar.HOUR));
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.MINUTE), mockCalendar.get(Calendar.MINUTE));
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.AM_PM), mockCalendar.get(Calendar.AM_PM));
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.MONTH), mockCalendar.get(Calendar.MONTH));
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.DAY_OF_MONTH), mockCalendar.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(recordToMap.getArrestDate().get(Calendar.YEAR), mockCalendar.get(Calendar.YEAR));
		Assert.assertEquals(recordToMap.getArrestAge(), new Integer(21));
		Assert.assertEquals(recordToMap.getGender(), "Male");
		Assert.assertEquals(recordToMap.getHeight(), "5\'10\"");
		Assert.assertEquals(recordToMap.getWeight(), "200 lbs");
		Assert.assertEquals(recordToMap.getHairColor(), "Black");
		Assert.assertEquals(recordToMap.getEyeColor(), "Brown");
	}

	@Test
	public void matchPropertyToField_Charges() {
		ArrestRecord recordToMap = new ArrestRecord();
		//pulled the selector from Site.getRecordDetailElements()
		Element profileDetailChargesSection = mockDetailDoc.select(".section-content.charges").get(0);
		mockEngine.matchPropertyToField(recordToMap, profileDetailChargesSection);
		
		Assert.assertEquals(recordToMap.getCharges().length, 2);
		Assert.assertEquals(recordToMap.getCharges()[0], "#1 POSSESSION OF DRUG PARAPHERNALIA (SMMS) BOND: $300");
		Assert.assertEquals(recordToMap.getCharges()[1], "#2 VIOLATION OF PROBATION - 1985 STATUTE: FE300086");
	}

	@Test
	public void matchPropertyToField_County() {
		ArrestRecord recordToMap = new ArrestRecord();
		//pulled the selector from Site.getRecordDetailElements()
		Element profileDetailCountySection = mockDetailDoc.select("img[src^=\"/mugs/\"]").get(0);
		mockEngine.matchPropertyToField(recordToMap, profileDetailCountySection);
		
		Assert.assertEquals(recordToMap.getCounty(), "Polk");
	}

	@Test
	public void matchPropertyToField_CountyAlternate() {
		ArrestRecord recordToMap = new ArrestRecord();
		//pulled the selector from Site.getRecordDetailElements()
		Element profileDetailCountySection = mockDetailDoc.select(".content-box.profile.profile-full h3").get(0);
		mockEngine.matchPropertyToField(recordToMap, profileDetailCountySection);
		
		Assert.assertEquals(recordToMap.getCounty(), "Polk");
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void obtainRecordDetailDoc_Online() throws IOException {
		Elements recordUrl = ((ArrestsDotOrgSite) mockEngine.getSite()).getRecordElements(mockMainPageDoc);
		String detailUrl = ((ArrestsDotOrgSite) mockEngine.getSite()).getRecordDetailDocUrl(recordUrl.get(0));
		
		Document detailDoc = mockEngine.obtainRecordDetailDoc(detailUrl, "www.google.com");
		
		Assert.assertNotNull(detailDoc);
		Assert.assertFalse(detailDoc.text().equals(""));
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordDetailElements(detailDoc).size()>0);
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void obtainRecordDetailDoc() throws IOException {
		Document detailDoc = mockEngine.obtainRecordDetailDoc("https://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1", "www.google.com");

		Assert.assertFalse(detailDoc.text().equals(""));
		Assert.assertEquals(detailDoc.title(), "Justin Wilde Mugshot | 07/27/17 Iowa Arrest");
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordDetailElements(detailDoc).size()>0);
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void parseDocForUrls_Online() {
		mockWeb.setCrawledIds(new HashSet<>());
		mockEngine.setSpiderWeb(mockWeb);
		Map<String,String> recordUrlMap = mockEngine.parseDocForUrls(mockMainPageDoc);

		Assert.assertEquals(recordUrlMap.size(), 56);
		Assert.assertEquals(mockMainPageDoc.select(".profile-card").size(), recordUrlMap.size());
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void parseDocForUrls_SomeAlreadyCrawled() {
		//add some records that exist in test maindoc
		Set<String> mockCrawledIds = new HashSet<>();
		mockCrawledIds.add("Brett_Wilkins_33797797");
		mockCrawledIds.add("Craig_Mitchell_33793872");
		mockCrawledIds.add("Justin_Wilde_33799480");
		mockWeb.setCrawledIds(mockCrawledIds);
		mockEngine.setSpiderWeb(mockWeb);

		Map<String,String> recordUrlMap = mockEngine.parseDocForUrls(mockMainPageDoc);

		Assert.assertEquals(recordUrlMap.size(), 53);
		Assert.assertTrue(mockMainPageDoc.select(".search-results .profile-card").size() > recordUrlMap.size());
	}

	@Test
	public void parseDocForUrls() {
		mockWeb.setCrawledIds(new HashSet<>());
		mockEngine.setSpiderWeb(mockWeb);
		Map<String,String> recordUrlMap = mockEngine.parseDocForUrls(mockMainPageDoc);
		
		Assert.assertEquals(recordUrlMap.size(), 56);
		Assert.assertEquals(mockMainPageDoc.select(".search-results .profile-card").size(), recordUrlMap.size());
	}
	
	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void populateArrestRecord_Online() {
		//just test that certain values are populated, not the specific values
		ArrestRecord resultRecord = mockEngine.populateArrestRecord(mockDetailDoc);
		
		Assert.assertNotNull(resultRecord.getId());		
		Assert.assertNotNull(resultRecord.getFullName());	
		Assert.assertNotNull(resultRecord.getFirstName());	
		Assert.assertNotNull(resultRecord.getLastName());	
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void populateArrestRecord() {
		mockDetailDoc.setBaseUri("https://iowa.arrests.org/Arrests/Brad_Smith_12323232/?d=1");
		ArrestRecord resultRecord = mockEngine.populateArrestRecord(mockDetailDoc);

		Assert.assertEquals(resultRecord.getId(), "Brad_Smith_12323232");		
		Assert.assertEquals(resultRecord.getFullName(), "Brad Roderick Smith");	
		Assert.assertEquals(resultRecord.getFirstName(), "Brad");	
		Assert.assertEquals(resultRecord.getMiddleName(), "Roderick");	
		Assert.assertEquals(resultRecord.getLastName(), "Smith");	
		Assert.assertEquals(resultRecord.getCounty(), "Polk");	
		Assert.assertEquals(resultRecord.getGender(), "Male");	
		Assert.assertEquals(resultRecord.getArrestAge(), new Integer(21));	
		Assert.assertEquals(resultRecord.getHeight(), "5\'10\"");	
		Assert.assertEquals(resultRecord.getWeight(), "200 lbs");	
		Assert.assertEquals(resultRecord.getHairColor(), "Black");	
		Assert.assertEquals(resultRecord.getEyeColor(), "Brown");	
		Assert.assertEquals(resultRecord.getTotalBond(), new Long(300));	
		Assert.assertEquals(resultRecord.getCharges().length, 2);	
		Assert.assertEquals(resultRecord.getCharges()[0], "#1 POSSESSION OF DRUG PARAPHERNALIA (SMMS) BOND: $300");
	}

	@Test
	public void scrapeRecords() throws SpiderException, IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
        mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
        Map<Object,String> recordsDetailsUrlMap = new HashMap<>();
        recordsDetailsUrlMap.put(1, "https://iowa.arrests.org/Irrevlavent/page");
        recordsDetailsUrlMap.put("33799480", "https://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1");
        recordsDetailsUrlMap.put("34065316", "https://iowa.arrests.org/Arrests/Dominic_Lacava_34065316/?d=1");
        recordsDetailsUrlMap.put(2, "https://iowa.arrests.org/County/AdSpam");

		mockEngine.scrapeRecords(recordsDetailsUrlMap);

		File mainOutput = new File(mockEngine.getRecordIOUtil().getMainDocPath());

		Assert.assertTrue(mainOutput.exists());
		Assert.assertTrue(recordsDetailsUrlMap.containsValue("CRAWLEDhttps://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1"));
        Assert.assertTrue(recordsDetailsUrlMap.containsValue("CRAWLEDhttps://iowa.arrests.org/Arrests/Dominic_Lacava_34065316/?d=1"));
        Assert.assertFalse(recordsDetailsUrlMap.containsKey("CRAWLED1"));
        Assert.assertFalse(recordsDetailsUrlMap.containsKey("CRAWLED2"));
        Assert.assertTrue(mainOutput.delete());
	}

	@Test
	public void scrapeRecords_MiscPageCrawl() throws SpiderException, IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
        mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
        Map<Object,String> recordsDetailsUrlMap = new HashMap<>();
        recordsDetailsUrlMap.put(1, "https://iowa.arrests.org/Irrevlavent/page");
        recordsDetailsUrlMap.put("33799480", "https://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1");

		mockEngine.scrapeRecords(recordsDetailsUrlMap);

		File mainOutput = new File(mockEngine.getRecordIOUtil().getMainDocPath());

		Assert.assertTrue(mainOutput.exists());
		Assert.assertTrue(recordsDetailsUrlMap.containsValue("CRAWLEDhttps://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1"));
        Assert.assertFalse(recordsDetailsUrlMap.containsKey("CRAWLED1"));
        Assert.assertTrue(mainOutput.delete());
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void scrapeRecords_OnlineIOException() throws SpiderException, IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
        mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
        Map<Object,String> recordsDetailsUrlMap = new HashMap<>();
        recordsDetailsUrlMap.put("", ""); //pass in a detail page that exists
        recordsDetailsUrlMap.put(2, "https://iowa.arrests.org/County/AdSpam");//this should cause exception

		mockEngine.scrapeRecords(recordsDetailsUrlMap);

		File mainOutput = new File(mockEngine.getRecordIOUtil().getMainDocPath());

		Assert.assertTrue(mainOutput.exists());
		Assert.assertTrue(recordsDetailsUrlMap.containsValue("CRAWLED"));//existing page
		Assert.assertEquals(mockEngine.getSpiderWeb().getAttemptCount(), 2);
        Assert.assertTrue(mainOutput.delete());
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void scrapeRecords_OnlineIOExceptionMaxFailures() throws SpiderException, IOException {
		SpiderWeb mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.ALCOHOL, State.IA);
		ArrestsDotOrgEngine mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockEngine.setRecordIOUtil(mockEngine.initializeIOUtil(State.IA.getName()));
        mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl("1"));
        Map<Object,String> recordsDetailsUrlMap = new HashMap<>();
        recordsDetailsUrlMap.put(1, "https://iowa.arrests.org/County/NotReal");//this should cause exception
        recordsDetailsUrlMap.put(2, "https://iowa.arrests.org/County/AdSpam");
        recordsDetailsUrlMap.put(3, "https://iowa.arrests.org/Page/NotFound");
        recordsDetailsUrlMap.put(4, "https://iowa.arrests.org/Error/Error");
        recordsDetailsUrlMap.put("34065316", "https://iowa.arrests.org/Arrests/Dominic_Lacava_34065316/?d=1");
        recordsDetailsUrlMap.put("34065317", "https://iowa.arrests.org/Arrests/Cominid_Aacavl_34065317/?d=1");
		mockEngine.scrapeRecords(recordsDetailsUrlMap);

		File mainOutput = new File(mockEngine.getRecordIOUtil().getMainDocPath());

		Assert.assertTrue(mainOutput.exists());
		Assert.assertTrue(recordsDetailsUrlMap.isEmpty());
		//confirm uncrawled records were backed up 
        Assert.assertTrue(mainOutput.delete());
	}

	@Test
	public void scrapeSite() {
        Assert.fail("Test not implemented");
	}

	@Test
	public void setCookies_Initial() {
		mockWeb.getSessionCookies().clear();
		Connection.Response response = new OfflineResponse(200, "https://iowa.arrests.org");
		mockEngine.setCookies(response);
		
		Assert.assertNotNull(mockWeb.getSessionCookies().get("PHPSESSID"));
		Assert.assertNotNull(mockWeb.getSessionCookies().get("__cfduid"));
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_session"), "1");
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_24"), "1");
	}
	
	@Test
	public void setCookies_Overwrite() {
		Map<String,String> currentCookies = new HashMap<>();
		currentCookies.put("PHPSESSID", "1283unlnc-wq0932e");
		currentCookies.put("views_session", "2");
		currentCookies.put("views_24", "4");
		Connection.Response response = new OfflineResponse(200, "https://iowa.arrests.org", currentCookies);
		mockWeb.setSessionCookies(currentCookies);
		
		mockEngine.setCookies(response);
		
		Assert.assertEquals(mockWeb.getSessionCookies().get("PHPSESSID"), response.cookie("PHPSESSID"));
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_session"), response.cookie("views_session"));
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_24"), response.cookie("views_24"));
	}
	
	@Test
	public void setCookies_RecordCap() {
		Map<String,String> currentCookies = new HashMap<>();
		currentCookies.put("PHPSESSID", "1283unlnc-wq0932e");
		currentCookies.put("views_session", "99");
		currentCookies.put("views_24", "99");
		Connection.Response response = new OfflineResponse(200, "https://iowa.arrests.org");
		mockWeb.setSessionCookies(currentCookies);
		mockWeb.setRecordCap(100);
		mockWeb.addToRecordsProcessed(100);
		
		mockEngine.setCookies(response);
		
		Assert.assertNull(mockWeb.getSessionCookies().get("PHPSESSID"));
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_session"), "1");
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_24"), "1");
	}
	
	@Test
	public void findAvailableCounties() {
		Assert.fail("Test not implemented");
	}
}
