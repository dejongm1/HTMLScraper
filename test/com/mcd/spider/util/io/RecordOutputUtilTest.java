package com.mcd.spider.util.io;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordOutputUtilTest {

    private RecordIOUtil ioUtil;
    private RecordOutputUtil outputter;
    private File backUpDoc;
    private File mainDoc;
    private File mainDocRenamed;
    private WritableWorkbook testWorkbook;

    @BeforeClass
    public void setUp() throws IOException {
        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        backUpDoc = new File(ioUtil.getMainDocName().substring(0, ioUtil.getMainDocName().indexOf(RecordIOUtil.getEXT())) + RecordOutputUtil.getBackupSuffix() + RecordIOUtil.getEXT());
        mainDoc = new File(ioUtil.getMainDocName());
        outputter = ioUtil.getOutputter();
        outputter.createWorkbook();
        mainDocRenamed = new File(mainDoc.getPath() + "tempForTesting");
    }

    @AfterClass
    public void tearDown() {
        mainDocRenamed.delete();
        mainDoc.delete();
        backUpDoc.delete();
        //delete merged and/or filtered file
    }

    @AfterMethod(groups = {"tempOutputFile"})
    public void tearDownTemp() {
        mainDocRenamed.renameTo(mainDoc);
        Assert.assertTrue(mainDoc.exists());
    }

    @Test
    public void testCreateWorkbook_mainDocExists() throws Exception {
        outputter.createWorkbook();
        Assert.assertTrue(mainDoc.exists());
        Assert.assertTrue(backUpDoc.exists());

        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        Workbook backupWorkbook = Workbook.getWorkbook(backUpDoc);

        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), backupWorkbook.getNumberOfSheets());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), backupWorkbook.getSheet(0).getRows());
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), backupWorkbook.getSheet(0).getName());
    }

    @Test(groups = {"tempOutputFile"})
    public void testCreateWorkbook_mainDocDoesntExist() throws Exception {
        renameMainDoc();

        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        outputter = ioUtil.getOutputter();

        outputter.createWorkbook();
        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);

        Assert.assertFalse(backUpDoc.exists());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), 1);
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), 1);
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), outputter.getState().getName());
    }

    private void renameMainDoc() throws Exception {
        mainDoc.renameTo(mainDocRenamed);
        Assert.assertTrue(mainDocRenamed.exists());
        testWorkbook = Workbook.createWorkbook(mainDocRenamed);
        testWorkbook.createSheet(outputter.getState().getName(), 0);
        testWorkbook.write();
        testWorkbook.close();
    }

    @Test(groups = {"tempOutputFile"})
    public void testSaveRecordsToWorkbook() throws Exception {
        renameMainDoc();

        //create list of records with basic data
    	List<Record> mockedRecords = new ArrayList<>();
    	for (int r=0;r<15;r++) {
            ArrestRecord record = new ArrestRecord();
            record.setId(String.valueOf(r));
            record.setFullName("name" + r);
            mockedRecords.add(record);
        }
    	//saveRecordsToWorkbook(records, tempworkbook)
        outputter.saveRecordsToWorkbook(mockedRecords, testWorkbook);
    	//check sizes(rows (minus header) vs list size) match
        Assert.assertEquals(mockedRecords.size(), testWorkbook.getSheet(0).getRows()-1);
    }

    @Test(groups = {"tempOutputFile"})
    public void testAddRecordToMainWorkbook() throws Exception {
        renameMainDoc();

    	//check current number of rows
        int currentRowCount = testWorkbook.getSheet(0).getRows();
        ArrestRecord arrestRecord = new ArrestRecord();
        arrestRecord.setId("1233afsasf");
        outputter.addRecordToMainWorkbook(arrestRecord);
    	//addRecordToWorkbook()
    	//check that row was inserted where it should've been
    	//check that file create, copy, delete, rename didn't leave extra files??
    }

   /* @Test(groups = {"tempOutputFile"})
    public void testRemoveColumnsFromSpreadsheet() throws Exception {
        renameMainDoc();

    	//new temp workbook
    	//check column count before
    	//remove columns
    	//check after
    	//clean up workbook
        Assert.fail();
    }

    @Test
    public void testCreateFilteredSpreadsheet() throws Exception {
    	//create a list of filtered records
    	//createspreadsheetwithRecords()
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	//delete workbook
        Assert.fail();
    }

    @Test
    public void testCreateMergedSpreadsheet() throws Exception {
    	//create a list of merged records
    	//createspreadsheetwithRecords()
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	//delete workbook
        Assert.fail();
    }

    @Test
    public void testSplitIntoSheets() throws Exception {
    	//either create list of List<> or use methods to read it in
    	//create baseDoc to use?
    	//count of List<Record> should match sheet count
    	//check sheet names
    	//row count in each sheet should match list<record>.size()
    	//sum of row counts should match sum of records
    	//delete new sheets?
        Assert.fail();
    }

    @Test
    public void testBackupUnCrawledRecords() throws Exception {
    	//mock a list of ids
    	//backupUncrawledRecords()
    	//size of mocked list should match rows in file
    	//delete file
        Assert.fail();
    }*/
}
