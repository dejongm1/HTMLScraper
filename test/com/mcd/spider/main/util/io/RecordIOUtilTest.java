package com.mcd.spider.main.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.html.ArrestsDotOrgSite;

public class RecordIOUtilTest {

	//get test-named files and rename them to site specific books for the test, delete after
	private File testMergeFileOne = new File("test/resources/testOutputFileOne.xls");
	private File testMergeFileTwo = new File("test/resources/testOutputFileTwo.xls");
	private File testArrestOrgOutput = new File("test/resources/Iowa_ArrestRecord_ArrestsOrg.xls");
	private File testDesMoinesRegisterComOutput = new File("test/resources/Iowa_ArrestRecord_DesMoinesRegisterCom.xls");
	
	private RecordIOUtil ioUtil;
	
	@BeforeClass
	public void setUp() throws IOException {
		System.setProperty("runInEclipse", "true");
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
		
		Assert.assertEquals(mergedRecords.size(), 10); //11 if I can make this work for name suffixes
	}
	
	

}
