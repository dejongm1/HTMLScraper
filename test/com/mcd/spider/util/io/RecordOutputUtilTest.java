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
    private boolean mainDocAlreadyExists;

    @BeforeClass
    public void setUp() throws IOException {
        ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}), true);
        outputter = ioUtil.getOutputter();
        mainDocAlreadyExists = new File(ioUtil.getMainDocName()).exists();
    }

    @AfterClass
    public void tearDown() {
        //delete backup file created
        //delete merged and/or filtered file
    }


    @Test
    public void testCreateWorkbook_mainDocExists() throws Exception {
        File backUp = new File(ioUtil.getMainDocName().substring(0, ioUtil.getMainDocName().indexOf(RecordIOUtil.getEXT())) + RecordOutputUtil.getBackupSuffix() + RecordIOUtil.getEXT());
        File mainDoc = new File(ioUtil.getMainDocName());
        outputter.createWorkbook();
        if (mainDocAlreadyExists) {
            Assert.assertTrue(backUp.exists());
        } else {
            Workbook workbook = Workbook.getWorkbook(mainDoc);
            Assert.assertEquals(workbook.getSheet(0).getRows(), 1);
        }

        //backup should have same size/rows as mainDoc
        //maindoc should only have header row if !mainDocAlreadyExists
        //sheetCount should be the same

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