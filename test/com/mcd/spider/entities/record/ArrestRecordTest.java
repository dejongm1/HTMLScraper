package com.mcd.spider.entities.record;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Michael De Jong
 *
 */

public class ArrestRecordTest {
	
	private static Logger logger = Logger.getLogger(ArrestRecordTest.class);

	ArrestRecord mockRecordOne = new ArrestRecord();
	ArrestRecord mockRecordTwo = new ArrestRecord();
	private File testMergeFileOne = new File("output/testing/testMergeFileOne.xls");
	private File testMergeFileTwo = new File("output/testing/testMergeFileTwo.xls");
	private File testOutputFile = new File("output/testing/testOutput.xls");
	private Sheet mergeSheetOne;
	private Sheet mergeSheetTwo;
	private WritableWorkbook outputWorkbook;
	private WritableSheet outputSheet;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		logger.info("********** Starting Test cases for ArrestRecord *****************");
		Assert.assertTrue(testMergeFileOne.exists());
		Files.copy(testMergeFileOne, testOutputFile);
		Workbook workbook = Workbook.getWorkbook(testMergeFileOne);
		if (workbook!=null) {
			mergeSheetOne = workbook.getSheet(0);
        }
        Assert.assertNotNull(mergeSheetOne);
		workbook = Workbook.getWorkbook(testMergeFileTwo);
        if (workbook!=null) {
        	mergeSheetTwo = workbook.getSheet(0);
        }
        Assert.assertNotNull(mergeSheetTwo);
        outputWorkbook = Workbook.createWorkbook(testOutputFile);
        if (workbook!=null) {
        	outputSheet = outputWorkbook.createSheet("testOutput", 0);
        }
        Assert.assertNotNull(outputSheet);
	}
	
	@BeforeTest
	public void setUp() {
		mockRecordOne.setId("Ashley_Graves_34029315");
		mockRecordOne.setFullName("Ashley  Graves");
		mockRecordOne.setFirstName("Ashley");
		mockRecordOne.setLastName("Graves");
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
        mockRecordOne.setHeight(null);
        mockRecordOne.setWeight("200 lbs");
        mockRecordOne.setHairColor("Black");
        mockRecordOne.setEyeColor("Brown");
        mockRecordOne.setCharges(new String[]{"#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000"});
		
		mockRecordTwo.setId("115922");
		mockRecordTwo.setFullName("ASHLEY GRAVES");
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
        mockRecordTwo.setCounty("POLK");
        mockRecordTwo.setHeight("5 foot, 5 inches");
        mockRecordTwo.setWeight("200 pounds");
        mockRecordTwo.setHairColor("black");
        mockRecordTwo.setEyeColor("brown");
        mockRecordTwo.setCharges(new String[]{"ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS"});
	}
	
	@AfterClass
	public void tearDown() {
		testOutputFile.delete();
		Assert.assertTrue(!testOutputFile.exists());
		logger.info("********** Finishing Test cases for ArrestRecord *****************");
	}

	@Test
	public void testMerge() {
		mockRecordTwo.merge(mockRecordOne);

		Assert.assertEquals(mockRecordTwo.getId(), "115922---Ashley_Graves_34029315");
		Assert.assertEquals(mockRecordTwo.getFullName(), "ASHLEY GRAVES");
		Assert.assertEquals(mockRecordTwo.getFirstName(), mockRecordOne.getFirstName());
		Assert.assertNull(mockRecordTwo.getMiddleName());
		Assert.assertEquals(mockRecordTwo.getLastName(), mockRecordOne.getLastName());
		Assert.assertEquals(mockRecordTwo.getArrestAge(), new Integer(28));
		Assert.assertEquals(mockRecordTwo.getGender(), "FEMALE");
		Assert.assertEquals(mockRecordTwo.getCity(), mockRecordOne.getCity());
		Assert.assertEquals(mockRecordTwo.getState(), mockRecordOne.getState());
		Assert.assertEquals(mockRecordTwo.getCounty(), "POLK");
		Assert.assertEquals(mockRecordTwo.getHeight(), "5 foot, 5 inches");
		Assert.assertEquals(mockRecordTwo.getWeight(), "200 pounds");
		Assert.assertEquals(mockRecordTwo.getHairColor(), "black");
		Assert.assertEquals(mockRecordTwo.getEyeColor(), "brown");
		Assert.assertEquals(mockRecordTwo.getCharges(), new String[]{"ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS"});
		Assert.assertNull(mockRecordTwo.getOffenderId());
		Assert.assertNull(mockRecordTwo.getRace());
	}
	
	@Test
	public void testAddToExcelSheet() throws Exception {
		int rowNumber = 1;
		outputSheet = mockRecordOne.addToExcelSheet(rowNumber, outputSheet);
		outputWorkbook.write();
		outputWorkbook.close();
		
		//an empty cell equals("") where and empty record field == null
		Cell[] row = outputSheet.getRow(rowNumber);
		String outputid = row[0].getContents();
		String outputfullname = row[1].getContents().equals("")?null:row[1].getContents();
		String outputfirstname = row[2].getContents().equals("")?null:row[2].getContents();
		String outputmiddlename = row[3].getContents().equals("")?null:row[3].getContents();
		String outputlastname = row[4].getContents().equals("")?null:row[4].getContents();
		Calendar outputarrestdate = row[5].getContents().equals("")?null:convertStringToCalendar(row[5].getContents());
		Long outputtotalbond = row[6].getContents().equals("")?null:Long.valueOf(row[6].getContents());
		Integer outputarrestage = row[7].getContents().equals("")?null:Integer.valueOf(row[7].getContents());
		String outputgender = row[8].getContents().equals("")?null:row[8].getContents();
		String outputcity = row[9].getContents().equals("")?null:row[9].getContents();
		String outputstate = row[10].getContents().equals("")?null:row[10].getContents();
		String outputcounty = row[11].getContents().equals("")?null:row[11].getContents();
		String outputheight = row[12].getContents().equals("")?null:row[12].getContents();
		String outputweight = row[13].getContents().equals("")?null:row[13].getContents();
		String outputhaircolor = row[14].getContents().equals("")?null:row[14].getContents();
		String outputeyecolor = row[15].getContents().equals("")?null:row[15].getContents();
		String outputbirthplace = row[16].getContents().equals("")?null:row[16].getContents();
		String outputcharges = row[17].getContents().equals("")?null:row[17].getContents().replace("; ", "");
		String outputoffenderid = row[18].getContents().equals("")?null:row[18].getContents();
		String outputrace = row[19].getContents().equals("")?null:row[19].getContents();
		
		//need to check each field?
		Assert.assertEquals(outputid, mockRecordOne.getId());
		Assert.assertEquals(outputfullname, mockRecordOne.getFullName());
		Assert.assertEquals(outputfirstname, mockRecordOne.getFirstName());
		Assert.assertEquals(outputmiddlename, mockRecordOne.getMiddleName());
		Assert.assertEquals(outputlastname, mockRecordOne.getLastName());
		Assert.assertEquals(outputarrestdate, mockRecordOne.getArrestDate());
		Assert.assertEquals(outputarrestdate.get(Calendar.MONTH), mockRecordOne.getArrestDate().get(Calendar.MONTH));
		Assert.assertEquals(outputarrestdate.get(Calendar.DAY_OF_MONTH), mockRecordOne.getArrestDate().get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(outputarrestdate.get(Calendar.YEAR), mockRecordOne.getArrestDate().get(Calendar.YEAR));
		Assert.assertEquals(outputarrestdate.get(Calendar.HOUR), mockRecordOne.getArrestDate().get(Calendar.HOUR));
		Assert.assertEquals(outputarrestdate.get(Calendar.MINUTE), mockRecordOne.getArrestDate().get(Calendar.MINUTE));
		Assert.assertEquals(outputtotalbond, mockRecordOne.getTotalBond());
		Assert.assertEquals(outputarrestage, mockRecordOne.getArrestAge());
		Assert.assertEquals(outputgender, mockRecordOne.getGender());
		Assert.assertEquals(outputcity, mockRecordOne.getCity());
		Assert.assertEquals(outputstate, mockRecordOne.getState());
		Assert.assertEquals(outputcounty, mockRecordOne.getCounty());
		Assert.assertEquals(outputheight, mockRecordOne.getHeight());
		Assert.assertEquals(outputweight, mockRecordOne.getWeight());
		Assert.assertEquals(outputhaircolor, mockRecordOne.getHairColor());
		Assert.assertEquals(outputeyecolor, mockRecordOne.getEyeColor());
		Assert.assertEquals(outputbirthplace, mockRecordOne.getBirthPlace());
		Assert.assertEquals(outputcharges, mockRecordOne.getCharges()[0]);
		Assert.assertEquals(outputoffenderid, mockRecordOne.getOffenderId());
		Assert.assertEquals(outputrace, mockRecordOne.getRace());
		
	}
	
	@Test
	public void testMatch() {
		//this test must pass before attempting testMatch()
		RecordTest recordTester = new RecordTest();
		recordTester.readRowIntoRecord_ArrestRecordComplete();
		ArrestRecord one = new ArrestRecord();
		ArrestRecord two = new ArrestRecord();
		
		Record.readRowIntoRecord(ArrestRecord.class, mergeSheetOne, one, 1, null);
		Record.readRowIntoRecord(ArrestRecord.class, mergeSheetTwo, two, 1, null);

		Assert.assertEquals(one.getFullName(), "Arlena  Ramirez");
		Assert.assertEquals(two.getFullName(), "ARLENA RAMIREZ");
        Assert.assertTrue(one.matches(two));
        Assert.assertTrue(two.matches(one));
	}
	
	
	@Test
	public void testMatchOnlyFullName() {
		//this test must pass before attempting testMatch()
		RecordTest recordTester = new RecordTest();
		recordTester.readRowIntoRecord_ArrestRecordComplete();
		ArrestRecord one = new ArrestRecord();
		ArrestRecord two = new ArrestRecord();
		
		Record.readRowIntoRecord(ArrestRecord.class, mergeSheetOne, one, 8, null);
		Record.readRowIntoRecord(ArrestRecord.class, mergeSheetTwo, two, 9, null);
		
		Assert.assertEquals(one.getFullName(), "Michael De Jong");
		Assert.assertEquals(two.getFullName(), "MICHAEL DE JONG");
        Assert.assertFalse(one.matches(two));
        Assert.assertFalse(two.matches(one));
	}

    @Test
    public void testConvertToInches() {
	    ArrestRecord arrestRecord = new ArrestRecord();
	    int heightOne = arrestRecord.convertToInches("5'04\"");
        int heightTwo = arrestRecord.convertToInches("5'4\"");
        int heightThree = arrestRecord.convertToInches("05'4\"");
        int heightFour = arrestRecord.convertToInches("05'04\"");

        int heightFive = arrestRecord.convertToInches("5feet4inches");
        int heightSix = arrestRecord.convertToInches("5 feet 4 inches");
        int heightSeven = arrestRecord.convertToInches("5ft 4 inches");
        int heightEight = arrestRecord.convertToInches("5 foot and 04 inches");

        Assert.assertEquals(heightOne, 64);
        Assert.assertEquals(heightTwo, 64);
        Assert.assertEquals(heightThree, 64);
        Assert.assertEquals(heightFour, 64);
        Assert.assertEquals(heightFive, 64);
        Assert.assertEquals(heightSix, 64);
        Assert.assertEquals(heightSeven, 64);
        Assert.assertEquals(heightEight, 64);
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
