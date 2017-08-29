package com.mcd.spider.util.io;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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
	
	@BeforeClass
	public void setUpClass() throws BiffException, IOException {
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
	
	@Test(dependsOnGroups={"ReadRowsIn"}, groups={"Inputter"})
	public void testReadRecordsFromSheet() {
		Set<Record> readRecords = inputter.readRecordsFromSheet(testReadInputFile, "readRecordsIn");
		Record[] readRecordArray = new Record[readRecords.size()];
		readRecords.toArray(readRecordArray);
		
		Assert.assertNotNull(readRecords);
		Assert.assertEquals(readRecords.size(), 3);
	}
	
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
	public void testGetEmptyRowCount() {
		Assert.fail();
	}

}
