package com.mcd.spider.util.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;

import jxl.Workbook;

public class RecordOutputUtilTest {

    private RecordIOUtil ioUtil;
    private RecordOutputUtil outputter;
    private File backUpDoc;
    private File mainDoc;
    private File mainDocRenamed;

    @BeforeClass
    public void setUp() throws IOException {
        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        outputter = ioUtil.getOutputter();
        backUpDoc = new File(ioUtil.getMainDocName().substring(0, ioUtil.getMainDocName().indexOf(RecordIOUtil.getEXT())) + RecordOutputUtil.getBackupSuffix() + RecordIOUtil.getEXT());
        mainDoc = new File(ioUtil.getMainDocName());
        mainDocRenamed = new File(mainDoc.getPath() + "tempForTesting");
    }

    @AfterClass
    public void tearDown() {
        mainDocRenamed.delete();
        backUpDoc.delete();
        //delete merged and/or filtered file
    }


    @Test
    public void testCreateWorkbook_mainDocExists() throws Exception {
        Assert.assertTrue(mainDoc.exists());
        
        outputter.createWorkbook();
        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        Workbook backupWorkbook = Workbook.getWorkbook(backUpDoc);
        
        Assert.assertTrue(backUpDoc.exists());
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), backupWorkbook.getNumberOfSheets());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), backupWorkbook.getSheet(0).getRows());
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), backupWorkbook.getSheet(0).getName());
    }

    @Test
    public void testCreateWorkbook_mainDocDoesntExist() throws Exception {
        mainDoc.renameTo(mainDocRenamed);
        Assert.assertFalse(mainDoc.exists());
        
        outputter.createWorkbook();
        Workbook mainWorkbook = Workbook.getWorkbook(mainDoc);
        
        Assert.assertFalse(backUpDoc.exists());
        Assert.assertEquals(mainWorkbook.getSheet(0).getRows(), 1);
        Assert.assertEquals(mainWorkbook.getNumberOfSheets(), 1);
        Assert.assertEquals(mainWorkbook.getSheet(0).getName(), outputter.getState().getName());
        
        mainDocRenamed.renameTo(mainDoc);
        Assert.assertTrue(mainDoc.exists());
    }

    @Test
    public void testSaveRecordsToWorkbook() throws Exception {
    	//create list of records with basic data
    	List<Record> mockedRecords = new ArrayList<>(); 
    	
    	//saveRecordsToWorkbook(records, tempworkbook)
    	//check sizes(rows vs list size) match
    }

    @Test
    public void testAddRecordToMainWorkbook() throws Exception {
    	//create a temp workbook or a new one for adding records to just build on 
    	//check current number of rows
    	//addRecordToWorkbook()
    	//check that row was inserted where it should've been
    	//check that file create, copy, delete, rename didn't leave extra files??
    }

    @Test
    public void testRemoveColumnsFromSpreadsheet() throws Exception {
    	//new temp workbook
    	//check column count before
    	//remove columns
    	//check after
    	//clean up workbook
    }

    @Test
    public void testCreateFilteredSpreadsheet() throws Exception {
    	//create a list of filtered records
    	//createspreadsheetwithRecords()
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	//delete workbook
    }

    @Test
    public void testCreateMergedSpreadsheet() throws Exception {
    	//create a list of merged records
    	//createspreadsheetwithRecords()
    	//TODO future - confirm it creates a backup, if one already exists
    	//confirm it exists, name is correct and row count matches list
    	//delete workbook
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
    }

    @Test
    public void testBackupUnCrawledRecords() throws Exception {
    	//mock a list of ids
    	//backupUncrawledRecords()
    	//size of mocked list should match rows in file
    	//delete file
    }

}