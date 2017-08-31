package com.mcd.spider.util.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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

	private File testReadInputFile = new File("output/testing/ArrestRecordInputTest.xls");
	private RecordIOUtil ioUtil;
	RecordInputUtil inputter;
	Sheet sheet;
	Workbook workbook;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
		inputter = ioUtil.getInputter();
		Assert.assertTrue(testReadInputFile.exists());
		workbook = Workbook.getWorkbook(testReadInputFile);
        if (workbook!=null) {
            sheet = workbook.getSheet("readRecordsIn");
        }
        Assert.assertNotNull(sheet);
	}
	
	@BeforeTest
	public void setUp() throws Exception {
		
	}

	@AfterTest
	public void tearDown() throws Exception {
		
	}
	
	@Test(groups={"Inputter"})
	public void testReadRecordsFromSheet() {
		Set<Record> readRecords = inputter.readRecordsFromSheet(testReadInputFile, "readRecordsIn");
		Record[] readRecordArray = new Record[readRecords.size()];
		readRecords.toArray(readRecordArray);
		
		Assert.assertNotNull(readRecords);
		Assert.assertEquals(readRecords.size(), 3);
	}
	
	@Test(groups={"Inputter"})
	public void testReadRecordsFromWorkbook() {
		List<Set<Record>> recordsSetList = inputter.readRecordsFromWorkbook(testReadInputFile);
		Sheet readSheet1 = workbook.getSheet("readRecordsIn");
		Sheet readSheet2 = workbook.getSheet("readRecordsInDiffColumns");
		Sheet readSheet3 = workbook.getSheet("emptyRows");
		
		Assert.assertEquals(recordsSetList.size(), 10);
		Assert.assertEquals(recordsSetList.get(inputter.getSheetIndex(testReadInputFile, "readRecordsIn")).size(), inputter.getNonEmptyRowCount(readSheet1)-1); //compare sheet (minus header) to respective set
		Assert.assertEquals(recordsSetList.get(inputter.getSheetIndex(testReadInputFile, "readRecordsInDiffColumns")).size(), inputter.getNonEmptyRowCount(readSheet2)-1); //compare sheet (minus header) to respective set
		Assert.assertEquals(recordsSetList.get(inputter.getSheetIndex(testReadInputFile, "emptyRows")).size(), inputter.getNonEmptyRowCount(readSheet3)-1); //compare sheet (minus header) to respective set
		//first 4 sheet are empty
		for (int s=0;s<4;s++) {
			Assert.assertEquals(recordsSetList.get(s).size(), workbook.getSheet(s).getRows());
		}
		Assert.assertEquals(recordsSetList.size(), workbook.getNumberOfSheets());
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
			testSheet = workbook.getSheet("readRecordsIn");
        }
		Assert.assertEquals(inputter.getNonEmptyRowCount(testSheet), 4);
		Assert.assertEquals(testSheet.getRows(), 16);
	}

}
