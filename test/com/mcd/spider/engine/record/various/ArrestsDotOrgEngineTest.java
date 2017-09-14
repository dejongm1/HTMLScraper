package com.mcd.spider.engine.record.various;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.SpiderWeb;

public class ArrestsDotOrgEngineTest {
	
	private ArrestsDotOrgEngine engine;
	private SpiderWeb web;
	private Record alcoholRecordOne;
	private Record alcoholRecordTwo;
	private Record violentRecordOne;
	private Record violentRecordTwo;
	private Record violentRecordThree;
	private Document detailDoc;
	
	
	@BeforeClass
	public void beforeClass() throws IOException {
		detailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage.html"), "UTF-8");
		web = new SpiderWeb(1, false, false, RecordFilterEnum.NONE, State.IA);
		engine = new ArrestsDotOrgEngine(web);
		alcoholRecordOne = new ArrestRecord();
		alcoholRecordOne.setId("1231");
		((ArrestRecord)alcoholRecordOne).setFullName("Suzie Q Public");
		((ArrestRecord)alcoholRecordOne).setCharges(new String[]{"Resisting Arrest", "2#DUI"});

		alcoholRecordTwo = new ArrestRecord();
		alcoholRecordTwo.setId("1232");
		((ArrestRecord)alcoholRecordTwo).setFullName("John Q Public");
		((ArrestRecord)alcoholRecordTwo).setCharges(new String[]{"1: Public Consumption"});

		violentRecordOne = new ArrestRecord();
		violentRecordOne.setId("2231");
		((ArrestRecord)violentRecordOne).setFullName("Will W. Williams");
		((ArrestRecord)violentRecordOne).setCharges(new String[]{"1) Battery", "2) Assault", "Psodomy"});
		
		violentRecordTwo = new ArrestRecord();
		violentRecordTwo.setId("2232");
		((ArrestRecord)violentRecordTwo).setFullName("Billy Shatner");
		((ArrestRecord)violentRecordTwo).setCharges(new String[]{"Jay Walking", "Murder in the First"});
		
		violentRecordThree = new ArrestRecord();
		violentRecordThree.setId("2233");
		((ArrestRecord)violentRecordThree).setFullName("JO Sonsimp");
		((ArrestRecord)violentRecordThree).setCharges(new String[]{"Crusifixion"});
	}

	@AfterClass
	public void afterClass() {
	}


	@Test
	public void ArrestsDotOrgEngine() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void compileRecordDetailUrlMapDocumentMapIntegerDocument() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void compileRecordDetailUrlMapDocumentSetString() {
		throw new RuntimeException("Test not implemented");
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
		web = new SpiderWeb(1, false, false, RecordFilterEnum.ALCOHOL, State.IA);
		engine = new ArrestsDotOrgEngine(web);
		List<Record> oneMatchingAlcoholRecord = new ArrayList<>();
		List<Record> twoMatchingAlcoholRecord = new ArrayList<>();
		List<Record> noMatchingAlcoholRecord = new ArrayList<>();
		
		oneMatchingAlcoholRecord.add(alcoholRecordOne);
		twoMatchingAlcoholRecord.add(alcoholRecordOne);
		twoMatchingAlcoholRecord.add(alcoholRecordTwo);
		twoMatchingAlcoholRecord.add(violentRecordOne);
		noMatchingAlcoholRecord.add(violentRecordOne);
		noMatchingAlcoholRecord.add(violentRecordThree);
		noMatchingAlcoholRecord.add(violentRecordTwo);
		

		Assert.assertEquals(engine.filterRecords(oneMatchingAlcoholRecord).size(), 1);
		Assert.assertTrue(engine.filterRecords(oneMatchingAlcoholRecord).contains(alcoholRecordOne));
		Assert.assertEquals(engine.filterRecords(twoMatchingAlcoholRecord).size(), 2);
		Assert.assertTrue(engine.filterRecords(twoMatchingAlcoholRecord).contains(alcoholRecordOne));
		Assert.assertTrue(engine.filterRecords(twoMatchingAlcoholRecord).contains(alcoholRecordTwo));
		Assert.assertFalse(engine.filterRecords(twoMatchingAlcoholRecord).contains(violentRecordOne));
		Assert.assertEquals(engine.filterRecords(noMatchingAlcoholRecord).size(), 0);
		Assert.assertFalse(engine.filterRecords(noMatchingAlcoholRecord).contains(violentRecordOne));
	}

	@Test
	public void finalizeOutput() {
		throw new RuntimeException("Test not implemented");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void formatArrestTime() {
		Element profileDetail = detailDoc.select(".info .section-content div").get(2);  //third div in test file is arrest time
		ArrestRecord record = new ArrestRecord();
		Calendar arrestCalendar = Calendar.getInstance();
		arrestCalendar.setTime(new Date("09/14/2017"));
		record.setArrestDate(arrestCalendar);
		
		engine.formatArrestTime(record, profileDetail);
		
		Assert.assertEquals(record.getArrestDate().get(Calendar.HOUR), 6);
		Assert.assertEquals(record.getArrestDate().get(Calendar.MINUTE), 15);
		Assert.assertEquals(record.getArrestDate().get(Calendar.AM_PM), Calendar.PM);
	}

	@Test
	public void formatName() {
		Element profileDetail = detailDoc.select(".info .section-content div").get(0); //first div in test file is name
		ArrestRecord record = new ArrestRecord();
		
		engine.formatName(record, profileDetail);
		
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
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void getSite() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void initializeIOUtil() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void initiateConnection() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void matchPropertyToField() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void obtainRecordDetailDoc() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void parseDocForUrls() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void populateArrestRecord() {
		throw new RuntimeException("Test not implemented");
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
	public void setCookies() {
		throw new RuntimeException("Test not implemented");
	}
}
