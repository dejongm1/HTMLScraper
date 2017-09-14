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
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @author Michael De Jong
 *
 */

public class RecordInputUtilTest {

	private static Logger logger = Logger.getLogger(RecordInputUtilTest.class);
	private File testReadInputFile = new File("output/testing/ArrestRecordInputTest.xls");
	private RecordIOUtil ioUtil;
	RecordInputUtil inputter;
	Sheet sheet;
	Workbook workbook;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		logger.info("********** Starting Test cases for RecordInputUtil *****************");
		System.setProperty("TestingSpider", "true");
		System.setProperty("offline", "true");
		ioUtil = new RecordIOUtil("IOWA", new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
		inputter = ioUtil.getInputter();
		Assert.assertTrue(testReadInputFile.exists());
		workbook = Workbook.getWorkbook(testReadInputFile);
        if (workbook!=null) {
            sheet = workbook.getSheet("readRecordsIn");
        }
        Assert.assertNotNull(sheet);
	}
	
	@AfterClass
	public void tearDown() throws Exception {
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for RecordInputUtil *****************");
	}
	
	@Test(groups={"Inputter"})
	public void testReadRecordsFromSheet() {
		RecordSheet readRecordSheet = inputter.readRecordsFromSheet(testReadInputFile, "readRecordsIn");
		Record[] readRecordArray = new Record[readRecordSheet.recordCount()];
		readRecordSheet.getRecords().toArray(readRecordArray);
		
		Assert.assertNotNull(readRecordSheet);
		Assert.assertEquals(readRecordSheet.recordCount(), 3);
	}
	
	@Test(groups={"Inputter"})
	public void testReadRecordsFromWorkbook() {
		RecordWorkbook recordsBook = inputter.readRecordsFromWorkbook(testReadInputFile);
		Sheet readSheet1 = workbook.getSheet("readRecordsIn");
		Sheet readSheet2 = workbook.getSheet("readRecordsInDiffColumns");
		Sheet readSheet3 = workbook.getSheet("emptyRows");
		
		Assert.assertEquals(recordsBook.sheetCount(), 11);
		Assert.assertEquals(recordsBook.getSheet(inputter.getSheetIndex(testReadInputFile, "readRecordsIn")).recordCount(), inputter.getNonEmptyRowCount(readSheet1)-1); //compare sheet (minus header) to respective set
		Assert.assertEquals(recordsBook.getSheet(inputter.getSheetIndex(testReadInputFile, "readRecordsInDiffColumns")).recordCount(), inputter.getNonEmptyRowCount(readSheet2)-1); //compare sheet (minus header) to respective set
		Assert.assertEquals(recordsBook.getSheet(inputter.getSheetIndex(testReadInputFile, "emptyRows")).recordCount(), inputter.getNonEmptyRowCount(readSheet3)-1); //compare sheet (minus header) to respective set
		//first 4 sheet are empty
		for (int s=0;s<4;s++) {
			Assert.assertEquals(recordsBook.getSheet(s).recordCount(), inputter.getNonEmptyRowCount(workbook.getSheet(s)));
		}
		Assert.assertEquals(recordsBook.sheetCount(), workbook.getNumberOfSheets());
	}
//
//	@Test(groups={"Inputter"})
//	public void testReadRecordsFromDefaultWorkbook() {
//		//readRecordsFromDefaultWorkbook();
//		
//		Assert.fail();
//	}
//	
	@Test(groups={"Inputter"})
	public void testGetSheetIndex() {
		int sheetNumber0 = inputter.getSheetIndex(testReadInputFile, "Sheet0");
		int sheetNumber2 = inputter.getSheetIndex(testReadInputFile, "Sheet2");
		int sheetNumber3 = inputter.getSheetIndex(testReadInputFile, "Sheet3");
		
		Assert.assertEquals(sheetNumber0, 0);
		Assert.assertEquals(sheetNumber2, 2);
		Assert.assertEquals(sheetNumber3, 3);
	}
	
	@Test
	public void testGetNonEmptyRowCount() {
		Sheet testSheet = null;
		if (workbook!=null) {
			testSheet = workbook.getSheet("emptyRows");
        }
		Assert.assertEquals(inputter.getNonEmptyRowCount(testSheet), 4);
	}

}
