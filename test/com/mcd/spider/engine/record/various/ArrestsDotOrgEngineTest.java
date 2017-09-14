package com.mcd.spider.engine.record.various;

import java.util.ArrayList;
import java.util.List;

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
	
	
	@BeforeClass
	public void beforeClass() {
		this.web = new SpiderWeb(1, false, false, RecordFilterEnum.NONE, State.getState("IA"));
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
	public void filterRecords() {
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
		

		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void finalizeOutput() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void formatArrestTime() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void formatName() {
		throw new RuntimeException("Test not implemented");
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
