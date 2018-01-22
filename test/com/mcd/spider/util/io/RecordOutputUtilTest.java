package com.mcd.spider.util.io;

import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.util.SpiderConstants;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.mcd.spider.entities.record.ArrestRecord.ArrestDateComparator;

public class RecordOutputUtilTest {

	private static Logger logger = Logger.getLogger(RecordOutputUtilTest.class);
    private RecordIOUtil ioUtil;
    private RecordOutputUtil outputter;
    private File backUpDoc;
    private File mainDoc;
    private File mergedDoc;
    private File filteredDoc;
    private File mainDocRenamed;
    private WritableWorkbook testWorkbook;
    private ArrestRecord mockRecordOne = new ArrestRecord();
    private ArrestRecord mockRecordTwo = new ArrestRecord();
    
    @BeforeClass
    public void setUpClass() {
		logger.info("********** Starting Test cases for RecordOutputUtil *****************");
		System.setProperty("TestingSpider", "true");
    	mockRecordOne.setId("Ashley_Graves_34029315");
		mockRecordOne.setFullName("Ashley  Graves");
		mockRecordOne.setFirstName("Ashley");
		mockRecordOne.setLastName("Graves");
		Calendar arrestDate = Calendar.getInstance();
		arrestDate.setTime(new Date("Aug-21-2017"));
        mockRecordOne.setArrestDate(arrestDate);
        mockRecordOne.setArrestAge(28);
        mockRecordOne.setGender("Female");
        mockRecordOne.setCity("Urbandale");
        mockRecordOne.setState("Iowa");
        mockRecordOne.setCounty("Polk");
        mockRecordOne.setWeight("200 lbs");
        mockRecordOne.setCharges(new String[]{"#1 ASSAULT CAUSING BODILY INJURY OR MENTAL ILLNESS STATUTE: SR308623 BOND: $1000"});
		
		mockRecordTwo.setId("115922");
		mockRecordTwo.setFullName("Michael De Jong");
		arrestDate = Calendar.getInstance();
		arrestDate.setTime(new Date("Aug-20-2017"));
        mockRecordTwo.setArrestDate(arrestDate);
        mockRecordTwo.setArrestAge(28);
        mockRecordTwo.setGender("MALE");
        mockRecordTwo.setCounty("Johnson");
        mockRecordTwo.setHeight("5 foot, 6 inches");
        mockRecordTwo.setWeight("200 pounds");
        mockRecordTwo.setHairColor("black");
        mockRecordTwo.setEyeColor("brown");
        mockRecordTwo.setCharges(new String[]{"MURDER"});
    }
    
    @AfterClass
    public void tearDown() {
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for RecordOutputUtil *****************");
    }

    @BeforeMethod
    public void setUpMethod() throws IOException, WriteException {
    	System.setProperty("offline", "false");
        ioUtil = new RecordIOUtil("IOWA", new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
    	System.setProperty("offline", "true");
        outputter = ioUtil.getOutputter();
    	backUpDoc = new File(ioUtil.getMainDocPath().substring(0, ioUtil.getMainDocPath().indexOf(RecordIOUtil.getEXT())) + RecordOutputUtil.getBackupSuffix() + RecordIOUtil.getEXT());
        mainDoc = new File(ioUtil.getMainDocPath());
        mainDocRenamed = new File(mainDoc.getPath() + "tempForTesting");
        mergedDoc = new File(outputter.getMergedDocPath(null));
        filteredDoc = new File(outputter.getFilteredDocPath(RecordFilterEnum.findFilter("alcohol")));
        outputter.createWorkbook(mainDoc.getPath(), null, true, null);
        testWorkbook = Workbook.createWorkbook(mainDoc);
        WritableSheet sheet = testWorkbook.createSheet(SpiderConstants.MAIN_SHEET_NAME, 0);
        outputter.createColumnHeaders(sheet);
        testWorkbook.write();
        testWorkbook.close();
    }

    @AfterMethod
    public void tearDownMethod() {
        mainDocRenamed.delete();
        mainDoc.delete();
        backUpDoc.delete();
        ioUtil.getCrawledIdFile().delete();
        ioUtil.getUncrawledIdFile().delete();
        mergedDoc.delete();
        filteredDoc.delete();
        //delete merged and/or filtered file
    }

    @Test
    public void testCreateWorkbook_MainDocExistsNullRecordsPassedIn() throws Exception {
        outputter.createWorkbook(mainDoc.getPath(), null, true, null);
        Assert.assertTrue(mainDoc.exists());
        Assert.assertTrue(backUpDoc.exists());

        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        Workbook backupWorkbook = Workbook.getWorkbook(backUpDoc);

        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), backupWorkbook.getNumberOfSheets());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), backupWorkbook.getSheet(0).getRows());
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), backupWorkbook.getSheet(0).getName());
    }

    @Test
    public void testCreateWorkbook_MultipleSheets() throws Exception {
    	RecordWorkbook recordBook = new RecordWorkbook();
    	RecordSheet recordSheetOne = new RecordSheet();
    	recordSheetOne.addRecord(mockRecordOne);
    	RecordSheet recordSheetTwo = new RecordSheet();
    	recordSheetOne.addRecord(mockRecordTwo);
    	recordBook.addSheet(recordSheetOne);
    	recordBook.addSheet(recordSheetTwo);
    	String[] sheetNames = recordBook.getSheetNames();
        outputter.createWorkbook(mainDoc.getPath(), recordBook, true, sheetNames, null);
        Assert.assertTrue(mainDoc.exists());

        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), recordBook.sheetCount());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), recordBook.getSheet(0).recordCount()+1); //+1 for header row
        Assert.assertEquals(mainWorkbook.getSheet(1).getRows(), recordBook.getSheet(1).recordCount()+1); //+1 for header row
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), sheetNames[0]);
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), SpiderConstants.MAIN_SHEET_NAME);
        Assert.assertEquals(mainWorkbook.getSheet(1).getName(), sheetNames[1]);
    }

    @Test
    public void testCreateWorkbook_MainDocDoesntExist() throws Exception {
        renameMainDoc();

        ioUtil = new RecordIOUtil("IOWA", new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        outputter = ioUtil.getOutputter();

        outputter.createWorkbook(mainDoc.getPath(), null, true, null);
        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);

        Assert.assertFalse(backUpDoc.exists());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), 1);
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), 1);
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), SpiderConstants.MAIN_SHEET_NAME);
    }

    @Test
    public void testSaveRecordsToWorkbook() throws Exception {
        //create list of records with basic data
    	RecordSheet mockedRecordSheet = new RecordSheet();
    	for (int r=0;r<15;r++) {
            ArrestRecord record = new ArrestRecord();
            record.setId(String.valueOf(r));
            record.setFullName("name" + r);
            mockedRecordSheet.addRecord(record);
        }
        outputter.saveRecordsToWorkbook(mockedRecordSheet, testWorkbook, ArrestDateComparator);
    	//check sizes(rows (minus header) vs list size) match
        Assert.assertEquals(mockedRecordSheet.recordCount(), testWorkbook.getSheet(0).getRows()-1);
    }

    @Test
    public void testAddRecordToMainWorkbook() throws Exception {
    	Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
    	int idFileLength = ioUtil.getInputter().getCrawledIds().size();
        int currentRowCount = mainWorkbook.getSheet(0).getRows();
        ArrestRecord arrestRecord = new ArrestRecord();
        arrestRecord.setId("1233afsasf");
        outputter.addRecordToMainWorkbook(arrestRecord);

        //read workbook back in to get inserted row
    	mainWorkbook = Workbook.getWorkbook(mainDoc);
        Cell[] rowInserted = mainWorkbook.getSheet(0).getRow(currentRowCount);
        
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), currentRowCount+1);
        Assert.assertEquals(rowInserted[0].getContents(), "1233AFSASF");
        Assert.assertEquals(idFileLength+1, ioUtil.getInputter().getCrawledIds().size());
    }

    @Test
    public void testRemoveColumnsFromSpreadsheet() throws Exception {
        Integer[] columnsToRemove = new Integer[]{ArrestRecord.RecordColumnEnum.ID_COLUMN.getColumnIndex(),ArrestRecord.RecordColumnEnum.OFFENDERID_COLUMN.getColumnIndex(),ArrestRecord.RecordColumnEnum.RACE_COLUMN.getColumnIndex()};
        outputter.removeColumnsFromSpreadsheet(columnsToRemove, mainDoc.getPath());
    	
        Workbook workbook = Workbook.getWorkbook(mainDoc);
        Sheet sheet = workbook.getSheet(0);
        Cell[] headerRow = sheet.getRow(0);
        
        Assert.assertEquals(sheet.getColumns(), ArrestRecord.RecordColumnEnum.values().length-columnsToRemove.length);
        for (int c=0;c<headerRow.length;c++) {
        	Assert.assertNotEquals(headerRow[c].getContents(), ArrestRecord.RecordColumnEnum.ID_COLUMN.getColumnTitle());
        	Assert.assertNotEquals(headerRow[c].getContents(), ArrestRecord.RecordColumnEnum.OFFENDERID_COLUMN.getColumnTitle());
        	Assert.assertNotEquals(headerRow[c].getContents(), ArrestRecord.RecordColumnEnum.RACE_COLUMN.getColumnTitle());
        }
    }

    @Test
    public void testCreateAlcoholFilteredSpreadsheet() throws Exception {
    	//create a list of merged records
    	RecordSheet recordSheet = new RecordSheet();
    	recordSheet.addRecord(mockRecordOne);
    	recordSheet.addRecord(mockRecordTwo);
    	outputter.createWorkbook(filteredDoc.getPath(), recordSheet, false, ArrestDateComparator);
    	Workbook filteredWorkbook = Workbook.getWorkbook(filteredDoc);
    	
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	Assert.assertTrue(filteredDoc.exists());
    	Assert.assertEquals(filteredWorkbook.getSheet(0).getRows(), recordSheet.recordCount()+1);
    	Assert.assertEquals(filteredDoc.getPath(), outputter.getFilteredDocPath(RecordFilterEnum.ALCOHOL));
    }

    @Test
    public void testCreateMergedSpreadsheet() throws Exception {
    	//create a list of merged records
    	RecordSheet recordSheet = new RecordSheet();
    	recordSheet.addRecord(mockRecordOne);
    	recordSheet.addRecord(mockRecordTwo);
    	outputter.createWorkbook(mergedDoc.getPath(), recordSheet, false, ArrestDateComparator);
    	Workbook mergedWorkbook = Workbook.getWorkbook(mergedDoc);
    	
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	Assert.assertTrue(mergedDoc.exists());
    	Assert.assertEquals(mergedWorkbook.getSheet(0).getRows(), recordSheet.recordCount()+1);
    	Assert.assertEquals(mergedDoc.getPath(), outputter.getMergedDocPath(null));
    }

    @Test
    public void testGetCustomNamedMergedSpreadsheet() throws Exception {
    	String result = outputter.getMergedDocPath(outputter.getFilteredDocPath(RecordFilterEnum.findFilter("alcohol")));
    	Assert.assertEquals(result, "output\\testing\\IOWA_ArrestRecord_Alcohol-related_MERGED.xls");
    }

    @Test
    public void testSplitIntoSheets_ArrestRecord() throws Exception {
        renameMainDoc();
        
        RecordWorkbook recordsBook = new RecordWorkbook();
        RecordSheet recordSheetMain = new RecordSheet();
        RecordSheet recordSheetTwo = new RecordSheet();
        RecordSheet recordSheetThree = new RecordSheet();
        Record mockRecordThree = new ArrestRecord();
        mockRecordThree.setId("123sdf");
        ((ArrestRecord)mockRecordThree).setFullName("Third record");
        ((ArrestRecord)mockRecordThree).setCounty("Polk");
        recordSheetMain.addRecord(mockRecordOne);
        recordSheetMain.addRecord(mockRecordTwo);
        recordSheetMain.addRecord(mockRecordThree);
        recordSheetTwo.addRecord(mockRecordOne);
        recordSheetThree.addRecord(mockRecordTwo);
        recordSheetTwo.addRecord(mockRecordThree);
        recordsBook.addSheet(recordSheetMain);
        recordsBook.addSheet(recordSheetTwo);
        recordsBook.addSheet(recordSheetThree);
        
        Assert.assertFalse(mainDoc.exists());

        outputter.splitIntoSheets(mainDoc.getPath(), ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnTitle(), recordsBook, ArrestRecord.class, ArrestRecord.CountyComparator);

        Workbook splitworkWorkbook = Workbook.getWorkbook(mainDoc);
        Sheet mainSheet = splitworkWorkbook.getSheet(SpiderConstants.MAIN_SHEET_NAME);
        Sheet sheetTwo = splitworkWorkbook.getSheet("Polk");
        Sheet sheetThree = splitworkWorkbook.getSheet("Johnson");

        Assert.assertNotNull(mainSheet);
        Assert.assertNotNull(sheetTwo);
        Assert.assertNotNull(sheetThree);
        Assert.assertEquals(mainSheet.getRows(), recordSheetMain.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetTwo.getRows(), recordSheetTwo.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetThree.getRows(), recordSheetThree.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(splitworkWorkbook.getNumberOfSheets(), recordsBook.sheetCount()+1);//+1 for mainsheet
        for (int r=1;r<sheetTwo.getRows();r++) {
        	Assert.assertTrue(sheetTwo.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Polk"));
        }        
        for (int r=1;r<sheetThree.getRows();r++) {
        	Assert.assertTrue(sheetThree.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Johnson"));
        }
    }

    @Test
    public void testSplitIntoSheets_NullDelimiter() throws Exception {
        renameMainDoc();
        
        RecordWorkbook recordsBook = new RecordWorkbook();
        RecordSheet recordSheetMain = new RecordSheet();
        RecordSheet recordSheetTwo = new RecordSheet();
        RecordSheet recordSheetThree = new RecordSheet();
        RecordSheet recordSheetFour = new RecordSheet();
        Record mockRecordThree = new ArrestRecord();
        mockRecordThree.setId("123sdf");
        ((ArrestRecord)mockRecordThree).setFullName("Third record");
        ((ArrestRecord)mockRecordThree).setCounty(null);
        recordSheetMain.addRecord(mockRecordOne);
        recordSheetMain.addRecord(mockRecordTwo);
        recordSheetMain.addRecord(mockRecordThree);
        recordSheetTwo.addRecord(mockRecordOne);
        recordSheetThree.addRecord(mockRecordTwo);
        recordSheetFour.addRecord(mockRecordThree);
        recordsBook.addSheet(recordSheetMain);
        recordsBook.addSheet(recordSheetTwo);
        recordsBook.addSheet(recordSheetThree);
        recordsBook.addSheet(recordSheetFour);

        Assert.assertFalse(mainDoc.exists());
        
        outputter.splitIntoSheets(mainDoc.getPath(), ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnTitle(), recordsBook, ArrestRecord.class, ArrestRecord.CountyComparator);

        Workbook splitworkWorkbook = Workbook.getWorkbook(mainDoc);
        Sheet mainSheet = splitworkWorkbook.getSheet(SpiderConstants.MAIN_SHEET_NAME);
        Sheet sheetTwo = splitworkWorkbook.getSheet("Polk");
        Sheet sheetThree = splitworkWorkbook.getSheet("Johnson");
        Sheet sheetFour = splitworkWorkbook.getSheet("empty");

        Assert.assertNotNull(mainSheet);
        Assert.assertNotNull(sheetTwo);
        Assert.assertNotNull(sheetThree);
        Assert.assertNotNull(sheetFour);
        Assert.assertEquals(mainSheet.getRows(), recordSheetMain.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetTwo.getRows(), recordSheetTwo.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetThree.getRows(), recordSheetThree.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetFour.getRows(), recordSheetFour.recordCount()+1);//+1 for columnHeaders
        Assert.assertEquals(splitworkWorkbook.getNumberOfSheets(), recordsBook.sheetCount()+1);//+1 for mainsheet
        for (int r=1;r<sheetTwo.getRows();r++) {
        	Assert.assertTrue(sheetTwo.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Polk"));
        }        
        for (int r=1;r<sheetThree.getRows();r++) {
        	Assert.assertTrue(sheetThree.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Johnson"));
        }
        for (int r=1;r<sheetFour.getRows();r++) {
        	Assert.assertTrue(sheetFour.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase(""));
        }
    }

    @Test
    public void testBackupUnCrawledRecords() throws Exception {
    	//mock a list of ids
    	Map<Object, String> urlsMap = new HashMap<>();
    	urlsMap.put("337480", "http://iowa.arrests.org/Arrests/Barry_Allen_337480/?d=1");
    	urlsMap.put("235490", "http://iowa.arrests.org/Arrests/Peter_Parker_235490/?d=1");
    	urlsMap.put("23434343", "http://iowa.arrests.org/Arrests/Steve_Rodgers_23434343/?d=1");
    	outputter.backupUnCrawledRecords(urlsMap);
    	String[] ids = ioUtil.getInputter().getUnCrawledIds().toArray(new String[3]);
    	//size of mocked list should match rows in file
    	Assert.assertEquals(ids.length, urlsMap.size());
    	int e = 0;
    	for (Map.Entry<Object, String> entry : urlsMap.entrySet()) {
    		Assert.assertEquals(ids[e], (String) entry.getKey());
    		e++;
    	}
    }

    @Test
    public void testBackupUnCrawledRecords_OneInvalidRecordUrl() throws Exception {
    	//mock a list of ids
    	Map<Object, String> urlsMap = new HashMap<>();
    	urlsMap.put("345", "www.google.com/345");
    	urlsMap.put("235490", "http://iowa.arrests.org/Arrests/Peter_Parker_235490/?d=1");
    	outputter.backupUnCrawledRecords(urlsMap);
    	String[] ids = ioUtil.getInputter().getUnCrawledIds().toArray(new String[1]);
    	//1 good, 1 bad that shouldn't be added
    	Assert.assertEquals(ids.length, 1);
		Assert.assertEquals(ids[0], "235490");
    }
    
    private void renameMainDoc() throws Exception {
        mainDoc.renameTo(mainDocRenamed);
        Assert.assertTrue(mainDocRenamed.exists());
        testWorkbook = Workbook.createWorkbook(mainDocRenamed);
        WritableSheet sheet = testWorkbook.createSheet(SpiderConstants.MAIN_SHEET_NAME, 0);
        outputter.createColumnHeaders(sheet);
        testWorkbook.write();
        testWorkbook.close();
    }

}
