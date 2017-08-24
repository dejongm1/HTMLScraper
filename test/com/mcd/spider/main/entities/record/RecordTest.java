package com.mcd.spider.main.entities.record;

import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class RecordTest {

	private File testReadInputFile = new File("test/resources/RecordInputTest.xls");
	Sheet mainSheet;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		Assert.assertTrue(testReadInputFile.exists());
		Workbook workbook = Workbook.getWorkbook(testReadInputFile);
        if (workbook!=null) {
            mainSheet = workbook.getSheet("readRecordsIn");
        }
        Assert.assertNotNull(mainSheet);
	}


	@Test
	public void readRowIntoRecord_ArrestRecordComplete() {
		ArrestRecord record1 = new ArrestRecord();
		Record.readRowIntoRecord(ArrestRecord.class, mainSheet, record1, 1);

		Calendar testCalendar = convertStringToCalendar("Aug-20-2017 04:09 AM");
		
		Assert.assertEquals(record1.getId(), "Arlena_Ramirez_34029315");
		Assert.assertEquals(record1.getMiddleName(), null);
		Assert.assertEquals(record1.getFullName(), "Arlena  Ramirez");
		Assert.assertEquals(record1.getLastName(), "Ramirez");
		Assert.assertEquals(record1.getArrestDate().get(Calendar.MONTH), testCalendar.get(Calendar.MONTH));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.DAY_OF_MONTH), testCalendar.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.YEAR), testCalendar.get(Calendar.YEAR));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.HOUR), testCalendar.get(Calendar.HOUR));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		Assert.assertEquals(record1.getTotalBond(), new Long(800));
		Assert.assertEquals(record1.getArrestAge(), new Integer(28));
		Assert.assertEquals(record1.getGender(), "Female");
		Assert.assertEquals(record1.getCity(), "Urbandale");
		Assert.assertEquals(record1.getState(), "Iowa");
		Assert.assertEquals(record1.getHeight(), "5'05\"");
		Assert.assertEquals(record1.getWeight(), "200 lbs");
		Assert.assertEquals(record1.getCounty(), "Polk");
		Assert.assertEquals(record1.getHairColor(), "Black");
		Assert.assertEquals(record1.getEyeColor(), "Brown");
		Assert.assertEquals(record1.getBirthPlace(), "Mars");
		Assert.assertEquals(record1.getCharges()[0], "#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000");

	}

	@Test
	public void readRowIntoRecord_ArrestRecordMissingData() {
		ArrestRecord record4 = new ArrestRecord();
		Record.readRowIntoRecord(ArrestRecord.class, mainSheet, record4, 4);

		Assert.assertEquals(record4.getId(), "Fatima_Kenjar_34021731");
		Assert.assertEquals(record4.getFirstName(), "Fatima");
		Assert.assertEquals(record4.getFullName(), "Fatima  Kenjar");
		Assert.assertEquals(record4.getLastName(), "Kenjar");
		Assert.assertEquals(record4.getGender(), "Female");
		Assert.assertNull(record4.getTotalBond());
		Assert.assertNull(record4.getArrestAge());
		Assert.assertNull(record4.getState());
		Assert.assertNull(record4.getHairColor());
		Assert.assertNull(record4.getEyeColor());
		Assert.assertNull(record4.getHeight());
		Assert.assertNull(record4.getWeight());
		Assert.assertEquals(record4.getCounty(), "Johnson");
		Assert.assertEquals(record4.getEyeColor(), null);
		
	}

	@Test
	public void splitByField_HappyPath() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void splitByField_NullDelimiter() {
		throw new RuntimeException("Test not implemented");
	}


	@SuppressWarnings("deprecation")
	private Calendar convertStringToCalendar(String dateTimeString) {
		String dateString = dateTimeString.substring(0, dateTimeString.indexOf(' '));
		Date date = new Date(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar!=null) {
    		String timeString = dateTimeString.substring(dateTimeString.indexOf(' ')+1);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(0, timeString.indexOf(':'))));
            calendar.set(Calendar.HOUR, Integer.parseInt(timeString.substring(0, timeString.indexOf(':'))));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeString.substring(timeString.indexOf(':')+1, timeString.indexOf(' '))));
            calendar.set(Calendar.AM, timeString.substring(timeString.indexOf(' ')+1)=="AM"?1:0);
        }
        return calendar;
	}
}
