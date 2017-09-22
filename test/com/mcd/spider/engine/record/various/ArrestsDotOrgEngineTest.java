package com.mcd.spider.engine.record.various;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
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
import com.mcd.spider.entities.site.OfflineResponse;
import com.mcd.spider.entities.site.SpiderWeb;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.util.io.RecordIOUtil;

public class ArrestsDotOrgEngineTest {
	
	private ArrestsDotOrgEngine mockEngine;
	private SpiderWeb mockWeb;
	private Record mockAlcoholRecordOne;
	private Record mockAlcoholRecordTwo;
	private Record mockViolentRecordOne;
	private Record mockViolentRecordTwo;
	private Record mockViolentRecordThree;
	private Document mockDetailDoc;
	private Document mockMainPageDoc;
	
	
	@BeforeClass
	public void beforeClass() throws IOException {
		mockMainPageDoc = Jsoup.parse(new File("test/resources/htmls/mainPageDoc.html"), "UTF-8");
		mockDetailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage.html"), "UTF-8");
		mockWeb = new SpiderWeb(9999, true, false, RecordFilterEnum.NONE, State.IA);
		mockEngine = new ArrestsDotOrgEngine(mockWeb);
		mockAlcoholRecordOne = new ArrestRecord();
		mockAlcoholRecordOne.setId("1231");
		((ArrestRecord)mockAlcoholRecordOne).setFullName("Suzie Q Public");
		((ArrestRecord)mockAlcoholRecordOne).setCharges(new String[]{"Resisting Arrest", "2#DUI"});

		mockAlcoholRecordTwo = new ArrestRecord();
		mockAlcoholRecordTwo.setId("1232");
		((ArrestRecord)mockAlcoholRecordTwo).setFullName("John Q Public");
		((ArrestRecord)mockAlcoholRecordTwo).setCharges(new String[]{"1: Public Consumption"});

		mockViolentRecordOne = new ArrestRecord();
		mockViolentRecordOne.setId("2231");
		((ArrestRecord)mockViolentRecordOne).setFullName("Will W. Williams");
		((ArrestRecord)mockViolentRecordOne).setCharges(new String[]{"1) Battery", "2) Assault", "Psodomy"});
		
		mockViolentRecordTwo = new ArrestRecord();
		mockViolentRecordTwo.setId("2232");
		((ArrestRecord)mockViolentRecordTwo).setFullName("Billy Shatner");
		((ArrestRecord)mockViolentRecordTwo).setCharges(new String[]{"Jay Walking", "Murder in the First"});
		
		mockViolentRecordThree = new ArrestRecord();
		mockViolentRecordThree.setId("2233");
		((ArrestRecord)mockViolentRecordThree).setFullName("JO Sonsimp");
		((ArrestRecord)mockViolentRecordThree).setCharges(new String[]{"Crusifixion"});
	}

	@AfterClass
	public void afterClass() {
	}


	@Test
	public void ArrestsDotOrgEngine_ConstructorWeb() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void ArrestsDotOrgEngine_ConstructorStateName() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void compileRecordDetailUrlMap() throws IOException {
		Map<Integer, Document> resultsPageDocMap = new HashMap<>();
		mockWeb.setCrawledIds(new HashSet<>());
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=2&results=56"), "UTF-8"));
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
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=2&results=56"), "UTF-8"));
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
		resultsPageDocMap.put(1, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=1&results=56"), "UTF-8"));
		resultsPageDocMap.put(2, Jsoup.parse(new File("test/resources/htmls/httpiowa.arrests.orgpage=2&results=56"), "UTF-8"));
		UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
		
		Map<Object, String> resultMap = mockEngine.compileRecordDetailUrlMap(mockMainPageDoc, resultsPageDocMap);
		
		Assert.assertEquals(resultMap.size(), resultsPageDocMap.size()*56-crawledIds.size());
		for (Map.Entry<Object, String> urlEntry : resultMap.entrySet()) {
			Assert.assertTrue(urlValidator.isValid(urlEntry.getValue()));
		}
	}

	@Test
	public void compileRecordDetailUrlMapFromBackup() throws IOException {
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
	public void compileResultsDocMap() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void compileResultsUrlMap() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void extractValue() {
		throw new RuntimeException("Test not implemented");
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

	@Test
	public void finalizeOutput() {
		throw new RuntimeException("Test not implemented");
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
		throw new RuntimeException("Test not implemented");
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
		Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "http://iowa.arrests.org");
		Assert.assertEquals(mockEngine.getSite().getBaseUrl(), "http://" + mockWeb.getState().getName().toLowerCase() + ".arrests.org");
	}

	@Test
	public void initializeIOUtil() {
		throw new RuntimeException("Test not implemented");
	}

	@Test(groups="online", enabled=false) //make these dependent on a test that gathers a handful of docs on class load instead of each test creating a new connection
	public void initiateConnection_Online() throws IOException {
		Object mainDoc = mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl(1));
		
		Assert.assertTrue(mainDoc instanceof Document);
		Assert.assertEquals(mockEngine.getSpiderWeb().getHeaders().size(), 7);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().size()>0);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("PHPSESSID")!=null);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("views_session")!=null);
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordElements((Document)mainDoc).size()>0);
		Assert.assertFalse(((Document)mainDoc).toString().contains("flibberdigibit"));//should not exist on live site
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void initiateConnection() throws IOException {
		Object mainDoc = mockEngine.initiateConnection(((ArrestsDotOrgSite)mockEngine.getSite()).generateResultsPageUrl(1));
		
		Assert.assertTrue(mainDoc instanceof Document);
		Assert.assertEquals(mockEngine.getSpiderWeb().getHeaders().size(), 7);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().size()>0);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("PHPSESSID")!=null);
		Assert.assertTrue(mockEngine.getSpiderWeb().getSessionCookies().get("views_session")!=null);
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordElements((Document)mainDoc).size()>0);
		Assert.assertTrue(((Document)mainDoc).toString().contains("flibberdigibit"));
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
		String detaiLUrl = ((ArrestsDotOrgSite) mockEngine.getSite()).getRecordDetailDocUrl(recordUrl.get(0));
		
		Document detailDoc = mockEngine.obtainRecordDetailDoc(detaiLUrl, "www.google.com");
		
		Assert.assertNotNull(detailDoc);
		Assert.assertFalse(detailDoc.text().equals(""));
		Assert.assertTrue(((ArrestsDotOrgSite) mockEngine.getSite()).getRecordDetailElements(detailDoc).size()>0);
		throw new RuntimeException("Test not finished");
	}

	@Test
	public void obtainRecordDetailDoc() throws IOException {
		Document detailDoc = mockEngine.obtainRecordDetailDoc("http://iowa.arrests.org/Arrests/Justin_Wilde_33799480/?d=1", "www.google.com");

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

		Assert.assertEquals(recordUrlMap.size(), 52);
		Assert.assertTrue(mockMainPageDoc.select(".search-results .profile-card").size() > recordUrlMap.size());
	}

	@Test
	public void parseDocForUrls() {
		mockWeb.setCrawledIds(new HashSet<>());
		mockEngine.setSpiderWeb(mockWeb);
		Map<String,String> recordUrlMap = mockEngine.parseDocForUrls(mockMainPageDoc);
		
		Assert.assertEquals(recordUrlMap.size(), 55);
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
		mockDetailDoc.setBaseUri("http://iowa.arrests.org/Arrests/Brad_Smith_12323232/?d=1");
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
	public void scrapeRecords() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void scrapeSite() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void setCookies_Initial() {
		mockWeb.getSessionCookies().clear();
		Connection.Response response = new OfflineResponse(200, "http://iowa.arrests.org");
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
		Connection.Response response = new OfflineResponse(200, "http://iowa.arrests.org", currentCookies);
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
		Connection.Response response = new OfflineResponse(200, "http://iowa.arrests.org");
		mockWeb.setSessionCookies(currentCookies);
		mockWeb.setRecordCap(100);
		mockWeb.addToRecordsProcessed(100);
		
		mockEngine.setCookies(response);
		
		Assert.assertNull(mockWeb.getSessionCookies().get("PHPSESSID"));
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_session"), "1");
		Assert.assertEquals(mockWeb.getSessionCookies().get("views_24"), "1");
	}
}
