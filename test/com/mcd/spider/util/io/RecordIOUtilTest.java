package com.mcd.spider.util.io;

import com.google.common.io.Files;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Michael De Jong
 */

public class RecordIOUtilTest {

	//get test-named files and rename them to site specific books for the test, delete after
	private File testMergeFileOne = new File("output/testing/testMergeFileOne.xls");
	private File testMergeFileTwo = new File("output/testing/testMergeFileTwo.xls");
	private File testArrestOrgOutput = new File("output/testing/Iowa_ArrestRecord_ArrestsOrg-test.xls");
	private File testDesMoinesRegisterComOutput = new File("output/testing/Iowa_ArrestRecord_DesMoinesRegisterCom-test.xls");
	
	private RecordIOUtil ioUtil;
	
	@BeforeClass
	public void setUp() throws IOException {
		Assert.assertTrue(testMergeFileOne.exists());
		Assert.assertTrue(testMergeFileTwo.exists());
		Files.copy(testMergeFileOne, testArrestOrgOutput);
		Files.copy(testMergeFileTwo, testDesMoinesRegisterComOutput);
		Assert.assertTrue(testArrestOrgOutput.exists());
		Assert.assertTrue(testDesMoinesRegisterComOutput.exists());
		ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
	}

	@AfterClass
	public void tearDown() {
		testArrestOrgOutput.delete();
		testDesMoinesRegisterComOutput.delete();
		Assert.assertTrue(!testArrestOrgOutput.exists());
		Assert.assertTrue(!testDesMoinesRegisterComOutput.exists());
	}

	@Test
	public void testMergeRecordsFromSheet() throws IOException {
		Set<Record> mergedRecords = ioUtil.mergeRecordsFromSheet(testArrestOrgOutput, testDesMoinesRegisterComOutput, 0);
		Assert.assertEquals(mergedRecords.size(), 12); //11 if I can make this work for name suffixes
	}


}
