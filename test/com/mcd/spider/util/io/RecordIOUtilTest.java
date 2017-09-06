package com.mcd.spider.util.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
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
		Set<Record> mergedRecords = ioUtil.mergeRecordsFromSheets(testMergeFileOne, testMergeFileTwo, 0, 0);
		Assert.assertEquals(mergedRecords.size(), 12);
	}
	
	@Test
	public void testMergeRecordsFromSheets_NullFilePassed() {
		Set<Record> mergedRecords = ioUtil.mergeRecordsFromSheets(testMergeFileOne, null, 0, -1);
		Set<Record> recordsFromSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileOne, 0);
		
		Assert.assertEquals(mergedRecords.size(), 8);
		Assert.assertEquals(recordsFromSheet.size(), mergedRecords.size());
	}
	
	@Test
	public void testMergeRecordsFromWorkbooks() {
		List<Set<Record>> mergedRecords = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOne, testMergeFileTwo);		
		
		Assert.assertEquals(mergedRecords.size(), 1);
		Assert.assertEquals(mergedRecords.get(0).size(), 12);
	}
	
	@Test
	public void testMergeRecordsFromWorkbooks_MoreSheetsBookOne() {
		List<Set<Record>> mergedRecords = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOneExtraSheets, testMergeFileTwo);
		Set<Record> recordsFromExtraSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileOneExtraSheets, 1);
		
		Assert.assertEquals(mergedRecords.size(), 2);
		Assert.assertEquals(mergedRecords.get(0).size(), 12);
		Assert.assertEquals(mergedRecords.get(1).size(), recordsFromExtraSheet.size());
	}

	@Test
	public void testMergeRecordsFromWorkbooks_MoreSheetsBookTwo() {
		List<Set<Record>> mergedRecords = ioUtil.mergeRecordsFromWorkbooks(testMergeFileOne, testMergeFileTwoExtraSheets);
		Set<Record> recordsFromExtraSheet = ioUtil.getInputter().readRecordsFromSheet(testMergeFileTwoExtraSheets, 1);

		Assert.assertEquals(mergedRecords.size(), 2);
		Assert.assertEquals(mergedRecords.get(0).size(), 12);
		Assert.assertEquals(mergedRecords.get(1).size(), recordsFromExtraSheet.size());
	}

}
