package com.mcd.spider.util.io;

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

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

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
		logger.info("********** Finishing Test cases for RecordOutputUtil *****************");
    }

    @BeforeMethod
    public void setUpMethod() throws IOException, WriteException {
    	System.setProperty("offline", "false");
        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
    	System.setProperty("offline", "true");
        outputter = ioUtil.getOutputter();
    	backUpDoc = new File(ioUtil.getMainDocPath().substring(0, ioUtil.getMainDocPath().indexOf(RecordIOUtil.getEXT())) + RecordOutputUtil.getBackupSuffix() + RecordIOUtil.getEXT());
        mainDoc = new File(ioUtil.getMainDocPath());
        mainDocRenamed = new File(mainDoc.getPath() + "tempForTesting");
        mergedDoc = new File(outputter.getMergedDocPath());
        filteredDoc = new File(outputter.getFilteredDocPath(RecordFilterEnum.findFilter("alcohol")));
        outputter.createWorkbook(mainDoc.getPath(), null, true);
        testWorkbook = Workbook.createWorkbook(mainDoc);
        WritableSheet sheet = testWorkbook.createSheet(outputter.getState().getName(), 0);
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
    public void testCreateWorkbook_mainDocExists() throws Exception {
        outputter.createWorkbook(mainDoc.getPath(), null, true);
        Assert.assertTrue(mainDoc.exists());
        Assert.assertTrue(backUpDoc.exists());

        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        Workbook backupWorkbook = Workbook.getWorkbook(backUpDoc);

        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), backupWorkbook.getNumberOfSheets());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), backupWorkbook.getSheet(0).getRows());
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), backupWorkbook.getSheet(0).getName());
    }

    @Test
    public void testCreateWorkbook_mainDocDoesntExist() throws Exception {
        renameMainDoc();

        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        outputter = ioUtil.getOutputter();

        outputter.createWorkbook(mainDoc.getPath(), null, true);
        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);

        Assert.assertFalse(backUpDoc.exists());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), 1);
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), 1);
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), outputter.getState().getName());
    }

    @Test
    public void testSaveRecordsToWorkbook() throws Exception {
        //create list of records with basic data
    	Set<Record> mockedRecords = new HashSet<>();
    	for (int r=0;r<15;r++) {
            ArrestRecord record = new ArrestRecord();
            record.setId(String.valueOf(r));
            record.setFullName("name" + r);
            mockedRecords.add(record);
        }
        outputter.saveRecordsToWorkbook(mockedRecords, testWorkbook);
    	//check sizes(rows (minus header) vs list size) match
        Assert.assertEquals(mockedRecords.size(), testWorkbook.getSheet(0).getRows()-1);
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
        Assert.assertEquals(rowInserted[0].getContents(), "1233afsasf");
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
    	Set<Record> records = new HashSet<>();
    	records.add(mockRecordOne);
    	records.add(mockRecordTwo);
    	outputter.createWorkbook(filteredDoc.getPath(), records, false);
    	Workbook filteredWorkbook = Workbook.getWorkbook(filteredDoc);
    	
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	Assert.assertTrue(filteredDoc.exists());
    	Assert.assertEquals(filteredWorkbook.getSheet(0).getRows(), records.size()+1);
    	Assert.assertEquals(filteredDoc.getPath(), outputter.getFilteredDocPath(RecordFilterEnum.ALCOHOL));
    }

    @Test
    public void testCreateMergedSpreadsheet() throws Exception {
    	//create a list of merged records
    	Set<Record> records = new HashSet<>();
    	records.add(mockRecordOne);
    	records.add(mockRecordTwo);
    	outputter.createWorkbook(mergedDoc.getPath(), records, false);
    	Workbook mergedWorkbook = Workbook.getWorkbook(mergedDoc);
    	
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	Assert.assertTrue(mergedDoc.exists());
    	Assert.assertEquals(mergedWorkbook.getSheet(0).getRows(), records.size()+1);
    	Assert.assertEquals(mergedDoc.getPath(), outputter.getMergedDocPath());
    }

    @Test
    public void testSplitIntoSheets_ArrestRecord() throws Exception {
        List<Set<Record>> recordsSetList = new ArrayList<>();
        Set<Record> recordsListOne = new HashSet<>();
        Set<Record> recordsListTwo = new HashSet<>();
        Record mockRecordThree = new ArrestRecord();
        mockRecordThree.setId("123sdf");
        ((ArrestRecord)mockRecordThree).setFullName("Third record");
        ((ArrestRecord)mockRecordThree).setCounty("Polk");
        recordsListOne.add(mockRecordOne);
        recordsListTwo.add(mockRecordTwo);
        recordsListOne.add(mockRecordThree);
        recordsSetList.add(recordsListOne);
        recordsSetList.add(recordsListTwo);
        
        Assert.assertTrue(mainDoc.exists());

        outputter.splitIntoSheets(mainDoc.getPath(), ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnTitle(), recordsSetList, ArrestRecord.class);

        Workbook splitworkWorkbook = Workbook.getWorkbook(mainDoc);
        Sheet sheetOne = splitworkWorkbook.getSheet("Polk");
        Sheet sheetTwo = splitworkWorkbook.getSheet("Johnson");
        Sheet mainSheet = splitworkWorkbook.getSheet(outputter.getState().getName());

        Assert.assertNotNull(sheetOne);
        Assert.assertNotNull(sheetTwo);
        Assert.assertEquals(sheetOne.getRows(), recordsListOne.size()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetTwo.getRows(), recordsListTwo.size()+1);//+1 for columnHeaders
        Assert.assertEquals(splitworkWorkbook.getNumberOfSheets(), recordsSetList.size()+1);//+1 for mainsheet
        for (int r=1;r<sheetOne.getRows();r++) {
        	Assert.assertTrue(sheetOne.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Polk"));
        }        
        for (int r=1;r<sheetTwo.getRows();r++) {
        	Assert.assertTrue(sheetTwo.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Johnson"));
        }
    }

    @Test
    public void testSplitIntoSheets_NullDelimiter() throws Exception {
        List<Set<Record>> recordsSetList = new ArrayList<>();
        Set<Record> recordsListOne = new HashSet<>();
        Set<Record> recordsListTwo = new HashSet<>();
        Set<Record> recordsListThree = new HashSet<>();
        Record mockRecordThree = new ArrestRecord();
        mockRecordThree.setId("123sdf");
        ((ArrestRecord)mockRecordThree).setFullName("Third record");
        ((ArrestRecord)mockRecordThree).setCounty(null);
        recordsListOne.add(mockRecordOne);
        recordsListTwo.add(mockRecordTwo);
        recordsListThree.add(mockRecordThree);
        recordsSetList.add(recordsListOne);
        recordsSetList.add(recordsListTwo);
        recordsSetList.add(recordsListThree);

        outputter.splitIntoSheets(mainDoc.getPath(), ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnTitle(), recordsSetList, ArrestRecord.class);

        Workbook splitworkWorkbook = Workbook.getWorkbook(mainDoc);
        Sheet sheetOne = splitworkWorkbook.getSheet("Polk");
        Sheet sheetTwo = splitworkWorkbook.getSheet("Johnson");
        Sheet sheetThree = splitworkWorkbook.getSheet("empty");
        Sheet mainSheet = splitworkWorkbook.getSheet(outputter.getState().getName());

        Assert.assertNotNull(sheetOne);
        Assert.assertNotNull(sheetTwo);
        Assert.assertNotNull(sheetThree);
        Assert.assertEquals(sheetOne.getRows(), recordsListOne.size()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetTwo.getRows(), recordsListTwo.size()+1);//+1 for columnHeaders
        Assert.assertEquals(sheetThree.getRows(), recordsListThree.size()+1);//+1 for columnHeaders
        Assert.assertEquals(splitworkWorkbook.getNumberOfSheets(), recordsSetList.size()+1);//+1 for mainsheet
        for (int r=1;r<sheetOne.getRows();r++) {
        	Assert.assertTrue(sheetOne.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Polk"));
        }        
        for (int r=1;r<sheetTwo.getRows();r++) {
        	Assert.assertTrue(sheetTwo.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase("Johnson"));
        }
        for (int r=1;r<sheetThree.getRows();r++) {
        	Assert.assertTrue(sheetThree.getRow(r)[ArrestRecord.RecordColumnEnum.COUNTY_COLUMN.getColumnIndex()].getContents().equalsIgnoreCase(""));
        }
    }

    @Test
    public void testBackupUnCrawledRecords() throws Exception {
    	//mock a list of ids
    	Map<Object, String> urlsMap = new HashMap<>();
    	urlsMap.put("123", "www.google.com/123");
    	urlsMap.put("234", "www.google.com/234");
    	urlsMap.put("345", "www.google.com/345");
    	urlsMap.put("456", "www.google.com/456");
    	outputter.backupUnCrawledRecords(urlsMap);
    	String[] ids = ioUtil.getInputter().getUncrawledIds().toArray(new String[urlsMap.size()]);
    	//size of mocked list should match rows in file
    	Assert.assertEquals(urlsMap.size(), ids.length);
    	int e = 0;
    	for (Map.Entry<Object, String> entry : urlsMap.entrySet()) {
    		Assert.assertEquals(ids[e], (String) entry.getKey());
    		e++;
    	}
    }
    
    private void renameMainDoc() throws Exception {
        mainDoc.renameTo(mainDocRenamed);
        Assert.assertTrue(mainDocRenamed.exists());
        testWorkbook = Workbook.createWorkbook(mainDocRenamed);
        WritableSheet sheet = testWorkbook.createSheet(outputter.getState().getName(), 0);
        outputter.createColumnHeaders(sheet);
        testWorkbook.write();
        testWorkbook.close();
    }

}
