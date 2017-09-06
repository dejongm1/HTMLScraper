package com.mcd.spider.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;

/**
 * 
 * @author u569220
 *
 */

public class SpiderEngineTest {
	
	private SpiderEngine engine;
	
	@BeforeClass
	public void beforeClass() {
		engine = new SpiderEngine();
	}

	@AfterClass
	public void afterClass() {

	}


	@Test
	public void customizeArrestOutputs() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void filterOutLexisNexisEligibleRecords() {
		ArrestRecord record1 = new ArrestRecord();
		record1.setArrestDate(Calendar.getInstance());
		record1.setFirstName("EligibleJohn");
		record1.setMiddleName("Q");
		record1.setLastName("Public");
		record1.setDob(new Date());
		ArrestRecord record2 = new ArrestRecord();
		record2.setArrestDate(Calendar.getInstance());
		record2.setLastName("Nelson");
		record2.setDob(new Date());
		ArrestRecord record3 = new ArrestRecord();
		record3.setArrestDate(Calendar.getInstance());
		record3.setFirstName("EligibleJoe");
		record3.setLastName("Gunny");
		record3.setDob(new Date());
		ArrestRecord record4 = new ArrestRecord();
		record4.setFirstName("Jack");
		record4.setLastName("Sprout");
		record4.setDob(new Date());
		
		Set<Record> recordSet1 = new HashSet<>();
		recordSet1.add(record1);
		recordSet1.add(record2);
		Set<Record> recordSet2 = new HashSet<>();
		recordSet2.add(record4);
		recordSet2.add(record3);

		List<Set<Record>> recordSetList = new ArrayList<>();
		recordSetList.add(recordSet2);
		recordSetList.add(recordSet1);

		List<Set<Record>> eligibleRecords = engine.filterOutLexisNexisEligibleRecords(recordSetList);
		Assert.assertEquals(eligibleRecords.size(), recordSetList.size());
		Assert.assertEquals(eligibleRecords.get(0).size(), 1);
		Assert.assertEquals(eligibleRecords.get(1).size(), 1);
		
	}

	@Test
	public void getArrestRecordsByState() {
		throw new RuntimeException("Test not implemented");
	}
}
