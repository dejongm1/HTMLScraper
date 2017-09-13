package com.mcd.spider.util.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;

/**
 * Michael De Jong
 */

public class RecordIOUtilTest {

	private static Logger logger = Logger.getLogger(RecordIOUtilTest.class);
	//get test-named files and rename them to site specific books for the test, delete after
	private File testMergeFileOne = new File("output/testing/testMergeFileOne.xls");
	private File testMergeFileTwo = new File("output/testing/testMergeFileTwo.xls");
	private File testMergeFileOneExtraSheets = new File("output/testing/testMergeFileOneExtraSheets.xls");
	private File testMergeFileTwoExtraSheets = new File("output/testing/testMergeFileTwoExtraSheets.xls");
	
	private RecordIOUtil ioUtil;
	
	@BeforeClass
	public void setUp() throws IOException {
		logger.info("********** Starting Test cases for RecordIOUtil *****************");
		System.setProperty("TestingSpider", "true");
		Assert.assertTrue(testMergeFileOne.exists());
		Assert.assertTrue(testMergeFileTwo.exists());
		Assert.assertTrue(testMergeFileOneExtraSheets.exists());
		Assert.assertTrue(testMergeFileTwoExtraSheets.exists());
		ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
	}

	@AfterClass
	public void tearDown() {
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for RecordIOUtil *****************");
	}

	@Test
	public void testMergeRecordsFromSheets() {
		RecordSheet mergedRecordSheet = ioUtil.mergeRecordsFromSheets(testMergeFileOne, testMergeFileTwo, 0, 0);
		Assert.assertEquals(mergedRecordSheet.recordCount(), 12);
	}
	
	@Test
	public void testMergeRecordsFromSheets_NullFilePassed() {
		RecordSheet mergedRecordSheet = ioUtil.mergeRecordsFromSheets(testMergeFileOne, null, 0, -1);
		RecordSheet recordsFromSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileOne, 0);
		
		Assert.assertEquals(mergedRecordSheet.recordCount(), 8);
		Assert.assertEquals(recordsFromSheet.recordCount(), mergedRecordSheet.recordCount());
	}
	
	@Test
	public void testMergeRecordsFromWorkbooks() {
		RecordWorkbook mergedRecordBook = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOne, testMergeFileTwo);		
		
		Assert.assertEquals(mergedRecordBook.sheetCount(), 1);
		Assert.assertEquals(mergedRecordBook.getSheet(0).recordCount(), 12);
	}
	
	@Test
	public void testMergeRecordsFromWorkbooks_MoreSheetsBookOne() {
		RecordWorkbook mergedRecordBook = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOneExtraSheets, testMergeFileTwo);
		RecordSheet recordsFromExtraSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileOneExtraSheets, 1);
		
		Assert.assertEquals(mergedRecordBook.sheetCount(), 2);
		Assert.assertEquals(mergedRecordBook.getSheet(0).recordCount(), 12);
		Assert.assertEquals(mergedRecordBook.getSheet(1).recordCount(), recordsFromExtraSheet.recordCount());
	}

	@Test
	public void testMergeRecordsFromWorkbooks_MoreSheetsBookTwo() {
		RecordWorkbook mergedRecordBook = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOne, testMergeFileTwoExtraSheets);
		RecordSheet recordsFromExtraSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileTwoExtraSheets, 1);

		Assert.assertEquals(mergedRecordBook.sheetCount(), 2);
		Assert.assertEquals(mergedRecordBook.getSheet(0).recordCount(), 12);
		Assert.assertEquals(mergedRecordBook.getSheet(1).recordCount(), recordsFromExtraSheet.recordCount());
	}

}
