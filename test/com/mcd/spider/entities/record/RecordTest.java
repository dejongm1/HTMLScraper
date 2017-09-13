package com.mcd.spider.entities.record;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord.RecordColumnEnum;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.util.io.RecordIOUtil;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @author Michael De Jong
 *
 */

public class RecordTest {

	private static Logger logger = Logger.getLogger(RecordTest.class);
	
	private File testReadInputFile = new File("output/testing/ArrestRecordInputTest.xls");
	static Sheet mainSheet;
	private RecordIOUtil ioUtil;
	private Workbook workbook;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		logger.info("********** Starting Test cases for Record *****************");
		System.setProperty("TestingSpider", "true");
		Assert.assertTrue(testReadInputFile.exists());
		workbook = Workbook.getWorkbook(testReadInputFile);
        if (workbook!=null) {
            mainSheet = workbook.getSheet("readRecordsIn");
        }
        Assert.assertNotNull(mainSheet);
        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
	}
	
	@AfterClass
	public void tearDown() {
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for Record *****************");
	}

	@Test(groups={"ColumnOrder"})
	public void getColumnOrder_AllArrestRecordColumnsInOrder() {
		Sheet sheet = workbook.getSheet("readRecordsIn");
		List<Object> columnOrder = Record.getColumnOrder(ArrestRecord.class, sheet, new ArrestRecord());
		RecordColumnEnum[] arrestRecordColumnEnums = ArrestRecord.RecordColumnEnum.values();
		
		for (int c=0;c<columnOrder.size();c++) {
			Assert.assertEquals(columnOrder.get(c), arrestRecordColumnEnums[c]);
		}
	}

	@Test(groups={"ColumnOrder"})
	public void getColumnOrder_MissingArrestRecordColumns() {
		Sheet sheet = workbook.getSheet("missingColumns");
		List<Object> columnOrder = Record.getColumnOrder(ArrestRecord.class, sheet, new ArrestRecord());
		
		Assert.assertEquals(columnOrder.size(), 5);
		Assert.assertEquals(columnOrder.get(0), ArrestRecord.RecordColumnEnum.ID_COLUMN);
		Assert.assertEquals(columnOrder.get(3), ArrestRecord.RecordColumnEnum.COUNTY_COLUMN);
		for (Object column : columnOrder) {
			Assert.assertNotEquals(column, ArrestRecord.RecordColumnEnum.FIRSTNAME_COLUMN);
		}
	}

	@Test(groups={"ColumnOrder"})
	public void getColumnOrder_ExtraArrestRecordColumns() {
		Sheet sheet = workbook.getSheet("extraColumns");
		List<Object> columnOrder = Record.getColumnOrder(ArrestRecord.class, sheet, new ArrestRecord());

		Assert.assertEquals(columnOrder.size(), 23);
		Assert.assertEquals(columnOrder.get(0), "EXTRA_COLUMN");
		Assert.assertEquals(columnOrder.get(1), ArrestRecord.RecordColumnEnum.ID_COLUMN);
		Assert.assertEquals(columnOrder.get(22), ArrestRecord.RecordColumnEnum.RACE_COLUMN);
	}

	@Test(groups={"ColumnOrder"})
	public void getColumnOrder_AllArrestRecordColumnsRearranged() {
		Sheet sheet = workbook.getSheet("columnsShuffled");
		List<Object> columnOrder = Record.getColumnOrder(ArrestRecord.class, sheet, new ArrestRecord());
		
		Assert.assertEquals(columnOrder.size(), 20);
		Assert.assertEquals(columnOrder.get(0), ArrestRecord.RecordColumnEnum.FULLNAME_COLUMN);
		Assert.assertEquals(columnOrder.get(19), ArrestRecord.RecordColumnEnum.RACE_COLUMN);
		
	}

	@Test(groups={"ReadRowsIn"}, dependsOnGroups={"ColumnOrder"})
	public void readRowIntoRecord_ArrestRecordComplete() {
		ArrestRecord record1 = new ArrestRecord();
		Record.readRowIntoRecord(ArrestRecord.class, mainSheet, record1, 1, null);

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

	@Test(groups={"ReadRowsIn"}, dependsOnGroups={"ColumnOrder"})
	public void readRowIntoRecord_ArrestRecordDifferentNamedColumns() {
		ArrestRecord record1 = new ArrestRecord();
        ArrestRecord record2 = new ArrestRecord();
		Sheet diffColumnsSheet = workbook.getSheet("readRecordsInDiffColumns");
		List<Object> columnOrder = Record.getColumnOrder(ArrestRecord.class, diffColumnsSheet, record1);
		Record.readRowIntoRecord(ArrestRecord.class, diffColumnsSheet, record1, 1, columnOrder);
        Record.readRowIntoRecord(ArrestRecord.class, diffColumnsSheet, record2, 2, columnOrder);

		Calendar testCalendar1 = convertStringToCalendar("Aug-20-2017 04:09 AM");
		Calendar testCalendar2 = convertStringToCalendar("Aug-20-2017 12:00 AM");
		
		Assert.assertEquals(record1.getId(), "Arlena_Ramirez_34029315");
		Assert.assertEquals(record1.getMiddleName(), null);
		Assert.assertEquals(record1.getFullName(), "Arlena  Ramirez");
		Assert.assertEquals(record1.getLastName(), "Ramirez");
		Assert.assertEquals(record1.getArrestDate().get(Calendar.MONTH), testCalendar1.get(Calendar.MONTH));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.DAY_OF_MONTH), testCalendar1.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.YEAR), testCalendar1.get(Calendar.YEAR));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.HOUR), testCalendar1.get(Calendar.HOUR));
		Assert.assertEquals(record1.getArrestDate().get(Calendar.MINUTE), testCalendar1.get(Calendar.MINUTE));
		Assert.assertEquals(record1.getTotalBond(), new Long(800));
		Assert.assertEquals(record1.getArrestAge(), new Integer(28));
		Assert.assertEquals(record1.getGender(), "Female");
		Assert.assertEquals(record1.getCity(), "Urbandale");
		Assert.assertEquals(record1.getHeight(), "5'05\"");
		Assert.assertEquals(record1.getWeight(), "200 lbs");
		Assert.assertEquals(record1.getCounty(), "Polk");
		Assert.assertEquals(record1.getHairColor(), "Black");
		Assert.assertEquals(record1.getEyeColor(), "Brown");
		Assert.assertEquals(record1.getBirthPlace(), "Mars");
		Assert.assertEquals(record1.getCharges()[0], "#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000");

        Assert.assertEquals(record2.getId(), "Christopher_Haney_34027045");
        Assert.assertEquals(record2.getFirstName(), "Christopher");
        Assert.assertEquals(record2.getMiddleName(), "Jason");
        Assert.assertEquals(record2.getFullName(), "Christopher Jason Haney");
        Assert.assertEquals(record2.getLastName(), "Haney");
        Assert.assertEquals(record2.getArrestDate().get(Calendar.MONTH), testCalendar2.get(Calendar.MONTH));
        Assert.assertEquals(record2.getArrestDate().get(Calendar.DAY_OF_MONTH), testCalendar2.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(record2.getArrestDate().get(Calendar.YEAR), testCalendar2.get(Calendar.YEAR));
        Assert.assertEquals(record2.getArrestDate().get(Calendar.HOUR), testCalendar2.get(Calendar.HOUR));
        Assert.assertEquals(record2.getArrestDate().get(Calendar.MINUTE), testCalendar2.get(Calendar.MINUTE));
        Assert.assertNotEquals(record2.getTotalBond(), "do not read in");
        Assert.assertNull(record2.getTotalBond());
        Assert.assertEquals(record2.getArrestAge(), new Integer(22));
        Assert.assertEquals(record2.getGender(), "Male");
        Assert.assertEquals(record2.getCity(), "Des Moines");
        Assert.assertEquals(record2.getHeight(), "6'05\"");
        Assert.assertEquals(record2.getWeight(), "230 lbs");
        Assert.assertEquals(record2.getCounty(), "Polk");
        Assert.assertEquals(record2.getHairColor(), "Black");
        Assert.assertEquals(record2.getEyeColor(), "Brown");
        Assert.assertNull(record2.getBirthPlace());
        Assert.assertEquals(record2.getCharges()[0], "#1 TRESPASS BOND: $300");
    }
	
	@Test(groups={"ReadRowsIn"})
	public void readRowIntoRecord_ArrestRecordMissingAndBadData() {
		ArrestRecord record4 = new ArrestRecord();
		Record.readRowIntoRecord(ArrestRecord.class, mainSheet, record4, 4, null);

		Assert.assertEquals(record4.getId(), "BadMissing_Data_34021731");
		Assert.assertEquals(record4.getFirstName(), "BadMissing");
		Assert.assertEquals(record4.getFullName(), "BadMissing Record Data");
		Assert.assertEquals(record4.getLastName(), "Data");
		Assert.assertEquals(record4.getGender(), "Female");
		Assert.assertNull(record4.getArrestDate());
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
    public void testGetAsSortedList_ArrestCounty() {
    	RecordSheet recordSet = new RecordSheet(); 
		ArrestRecord record1 = new ArrestRecord();
        ArrestRecord record2 = new ArrestRecord();
        ArrestRecord record4 = new ArrestRecord();
        Sheet sortingSheet = workbook.getSheet("sorting");
		Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record1, 1, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record2, 2, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record4, 4, null);
    	recordSet.addRecord(record1);
    	recordSet.addRecord(record2);
    	recordSet.addRecord(record4);

    	for (int t=0;t<=40;t++) {
	        List<Record> sortedList = Record.getAsSortedList(recordSet.getRecords(), ArrestRecord.CountyComparator);
	        
	        assertThat(sortedList, contains(record4, record1, record2));
    	}
    }

    @Test
    public void testGetAsSortedList_ArrestDate() {
    	RecordSheet recordSet = new RecordSheet(); 
		ArrestRecord record1 = new ArrestRecord();
        ArrestRecord record2 = new ArrestRecord();
        ArrestRecord record4 = new ArrestRecord();
        Sheet sortingSheet = workbook.getSheet("sorting");
		Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record1, 1, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record2, 2, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record4, 4, null);
    	recordSet.addRecord(record1);
    	recordSet.addRecord(record2);
    	recordSet.addRecord(record4);

    	for (int t=0;t<=40;t++) {
	        List<Record> sortedList = Record.getAsSortedList(recordSet.getRecords(), ArrestRecord.ArrestDateComparator);
	        
	        assertThat(sortedList, contains(record2, record1, record4));
    	}
    }

    @Test
    public void testGetAsSortedList_NoComparator() {
    	RecordSheet recordSet = new RecordSheet(); 
		ArrestRecord record1 = new ArrestRecord();
        ArrestRecord record2 = new ArrestRecord();
        ArrestRecord record4 = new ArrestRecord();
        Sheet sortingSheet = workbook.getSheet("sorting");
		Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record1, 1, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record2, 2, null);
        Record.readRowIntoRecord(ArrestRecord.class, sortingSheet, record4, 4, null);
    	recordSet.addRecord(record4);
    	recordSet.addRecord(record2);
    	recordSet.addRecord(record1);

    	for (int t=0;t<=40;t++) {
	        List<Record> sortedList = Record.getAsSortedList(recordSet.getRecords(), null);
	        
	        assertThat(sortedList, containsInAnyOrder(record4, record2, record1));
	    	Assert.assertEquals(sortedList,  sortedList);
    	}
    }

    @Test
	public void splitByField_ArrestRecordsByCounty() throws InterruptedException {
		List<Record> records = new ArrayList<>(ioUtil.getInputter().readRecordsFromSheet(testReadInputFile, "readRecordsIn").getRecords());
        Collections.sort(records, ArrestRecord.CountyComparator);
		RecordWorkbook splitRecordsBook = Record.splitByField(records, RecordColumnEnum.COUNTY_COLUMN.getColumnTitle(), ArrestRecord.class);
		
		Assert.assertEquals(splitRecordsBook.sheetCount(), 3);
		Assert.assertEquals(splitRecordsBook.getSheet("Polk").recordCount(), 2);
		Assert.assertEquals(splitRecordsBook.getSheet("Johnson").recordCount(), 1);
		Assert.assertEquals(((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Polk")).getCounty(), ((ArrestRecord)splitRecordsBook.getRecordsFromSheet("Polk").toArray()[1]).getCounty());
		Assert.assertNotEquals(((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Polk")).getCounty(), ((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Johnson")).getCounty());
	}

	@Test
	public void splitByField_ArrestRecordsByCity_NullDelimiter() {
		List<Record> records = new ArrayList<>(ioUtil.getInputter().readRecordsFromSheet(testReadInputFile, "readRecordsIn").getRecords());
        Collections.sort(records, ArrestRecord.CityComparator);
		RecordWorkbook splitRecordsBook = Record.splitByField(records, RecordColumnEnum.CITY_COLUMN.getColumnTitle(), ArrestRecord.class);
		
		Assert.assertEquals(splitRecordsBook.sheetCount(), 4);
		Assert.assertEquals(splitRecordsBook.getSheet("Des Moines").recordCount(), 1);
		Assert.assertEquals(splitRecordsBook.getSheet("Urbandale").recordCount(), 1);
		Assert.assertEquals(splitRecordsBook.getSheet("UnknownSheet").recordCount(), 1);
		Assert.assertNotEquals(((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Des Moines")).getCity(), ((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Urbandale")).getCity());
		Assert.assertNotEquals(((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("Urbandale")).getCity(), ((ArrestRecord)splitRecordsBook.getFirstRecordFromSheet("UnknownSheet")).getCity());
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
