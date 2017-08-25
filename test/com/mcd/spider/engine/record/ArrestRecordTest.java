package com.mcd.spider.engine.record;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.RecordTest;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @author Michael De Jong
 *
 */

public class ArrestRecordTest {

	ArrestRecord mockRecordOne = new ArrestRecord();
	ArrestRecord mockRecordTwo = new ArrestRecord();
	private File testMergeFileOne = new File("test/resources/testOutputFileOne.xls");
	private File testMergeFileTwo = new File("test/resources/testOutputFileTwo.xls");
	static Sheet sheetOne;
	static Sheet sheetTwo;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		Assert.assertTrue(testMergeFileOne.exists());
		Workbook workbook = Workbook.getWorkbook(testMergeFileOne);
		if (workbook!=null) {
			sheetOne = workbook.getSheet(0);
        }
        Assert.assertNotNull(sheetOne);
		workbook = Workbook.getWorkbook(testMergeFileTwo);
        if (workbook!=null) {
        	sheetTwo = workbook.getSheet(0);
        }
        Assert.assertNotNull(sheetTwo);
	}
	
	@BeforeTest
	public void setUp() {
		mockRecordOne.setId("Arlena_Ramirez_34029315");
		mockRecordOne.setFullName("Arlena  Ramirez");
		mockRecordOne.setFirstName("Arlena");
		mockRecordOne.setLastName("Ramirez");
		Calendar arrestDate = Calendar.getInstance();
		Date date = new Date("Aug-20-2017");
		arrestDate.setTime(date);
		String arrestTimeText = "04:09 AM";
        arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
        arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
        arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
        mockRecordOne.setArrestDate(arrestDate);
        mockRecordOne.setArrestAge(28);
        mockRecordOne.setGender("Female");
        mockRecordOne.setCity("Urbandale");
        mockRecordOne.setState("Iowa");
        mockRecordOne.setCounty("Polk");
        mockRecordOne.setHeight("5'05");
        mockRecordOne.setWeight("200 lbs");
        mockRecordOne.setHairColor("Black");
        mockRecordOne.setEyeColor("Brown");
        mockRecordOne.setCharges(new String[]{"#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000;"});
		
		mockRecordTwo.setId("115922");
		mockRecordTwo.setFullName("ARLENA RAMIREZ");
		mockRecordTwo.setFirstName(null);
		mockRecordTwo.setLastName(null);
		arrestDate = Calendar.getInstance();
		date = new Date("Aug-20-2017");
		arrestDate.setTime(date);
		arrestTimeText = "04:09 AM";
        arrestDate.set(Calendar.HOUR, Integer.parseInt(arrestTimeText.substring(0, arrestTimeText.indexOf(':'))));
        arrestDate.set(Calendar.MINUTE, Integer.parseInt(arrestTimeText.substring(arrestTimeText.indexOf(':')+1, arrestTimeText.indexOf(' '))));
        arrestDate.set(Calendar.AM, arrestTimeText.substring(arrestTimeText.indexOf(' ')+1)=="AM"?1:0);
        mockRecordTwo.setArrestDate(arrestDate);
        mockRecordTwo.setArrestAge(28);
        mockRecordTwo.setGender("FEMALE");
        mockRecordTwo.setCity(null);
        mockRecordTwo.setState(null);
        mockRecordTwo.setCounty("POLK ");
        mockRecordTwo.setHeight("5 foot, 5 inches");
        mockRecordTwo.setWeight("200 pounds");
        mockRecordTwo.setHairColor("black");
        mockRecordTwo.setEyeColor("brown");
        mockRecordTwo.setCharges(new String[]{"ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS;"});
        
	}

	@AfterTest
	public void tearDown() {
		
	}

	@Test
	public void testMerge() {
		//multiple of these
	}
	
	@Test(dependsOnGroups={"RecordTest"})
	public void testMatch() {
		//this test must pass before attempting testMatch()
		RecordTest recordTester = new RecordTest();
		recordTester.readRowIntoRecord_ArrestRecordComplete();
		ArrestRecord one = new ArrestRecord();
		ArrestRecord two = new ArrestRecord();
		
		Record.readRowIntoRecord(ArrestRecord.class, sheetOne, one, 1);
		Record.readRowIntoRecord(ArrestRecord.class, sheetTwo, two, 1);

		Assert.assertEquals(one.getFullName(), "Arlena  Ramirez");
		Assert.assertEquals(two.getFullName(), "ARLENA RAMIREZ");
        Assert.assertTrue(one.matches(two));
        Assert.assertTrue(two.matches(one));
	}
	
	
	@Test(dependsOnGroups={"RecordTest"})
	public void testMatchOnlyFullName() {
		//this test must pass before attempting testMatch()
		RecordTest recordTester = new RecordTest();
		recordTester.readRowIntoRecord_ArrestRecordComplete();
		ArrestRecord one = new ArrestRecord();
		ArrestRecord two = new ArrestRecord();
		
		Record.readRowIntoRecord(ArrestRecord.class, sheetOne, one, 8);
		Record.readRowIntoRecord(ArrestRecord.class, sheetTwo, two, 9);
		
		Assert.assertEquals(one.getFullName(), "Michael De Jong");
		Assert.assertEquals(two.getFullName(), "MICHAEL DE JONG");
        Assert.assertFalse(one.matches(two));
        Assert.assertFalse(two.matches(one));
	}
	

}
