package com.mcd.spider.util.io;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import jxl.Workbook;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

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
    }

    @Test
    public void testSaveRecordsToMainWorkbook() throws Exception {
    }

    @Test
    public void testAddRecordToMainWorkbook() throws Exception {
    }

    @Test
    public void testRemoveColumnsFromSpreadsheet() throws Exception {
    }

    @Test
    public void testCreateFilteredSpreadsheet() throws Exception {
    }

    @Test
    public void testCreateMergedSpreadsheet() throws Exception {
    }

    @Test
    public void testSplitIntoSheets() throws Exception {
    }

    @Test
    public void testBackupUnCrawledRecords() throws Exception {
    }

}