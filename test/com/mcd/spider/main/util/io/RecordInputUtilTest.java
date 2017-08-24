package com.mcd.spider.main.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.html.ArrestsDotOrgSite;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class RecordInputUtilTest {

	private File testReadInputFile = new File("test/resources/RecordInputTest.xls");
	private RecordIOUtil ioUtil;
	RecordInputUtil inputter;
	Sheet sheet;
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
		System.setProperty("runInEclipse", "true");
		ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
		inputter = ioUtil.getInputter();
		Assert.assertTrue(testReadInputFile.exists());
		Workbook workbook = Workbook.getWorkbook(testReadInputFile);
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
	
	@Test
	public void testReadRecordsFromSheet() {
		Set<Record> readRecords = inputter.readRecordsFromSheet(testReadInputFile, "readRecordsIn");
		Record[] readRecordArray = new Record[readRecords.size()];
		readRecords.toArray(readRecordArray);
		
		Assert.assertNotNull(readRecords);
		Assert.assertEquals(readRecords.size(), 3);
	}
	
	@Test
	public void testGetSheetIndex() {
		int sheetNumber0 = inputter.getSheetIndex(testReadInputFile, "Sheet0");
		int sheetNumber2 = inputter.getSheetIndex(testReadInputFile, "Sheet2");
		int sheetNumber3 = inputter.getSheetIndex(testReadInputFile, "Sheet3");
		
		Assert.assertEquals(sheetNumber0, 0);
		Assert.assertEquals(sheetNumber2, 2);
		Assert.assertEquals(sheetNumber3, 3);
		
	}

}
