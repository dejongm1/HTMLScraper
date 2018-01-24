package com.mcd.spider.engine;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mcd.spider.engine.record.various.ArrestsDotOrgEngine;
import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.entities.site.service.DesMoinesRegisterComSite;
import com.mcd.spider.util.io.RecordIOUtil;

/**
 * 
 * @author Michael De Jong
 *
 */

public class SpiderEngineTest {

	private static Logger logger = Logger.getLogger(SpiderEngineTest.class);

	private SpiderEngine engine;
	private State state;
	private File testOutputFileOne = new File("output/testing/IAArrestsOrgOutput.xls");
	private File testOutputFileTwo = new File("output/testing/DSMRegComOutput.xls");
	private File testOutputFileLN = new File("output/testing/OKArrestsOrgOutput.xls");
	private File testOutputFileLNIneligible = new File("output/testing/OKArrestsOrgOutput_Ineligible.xls");
	private File testOutputFileOneFiltered = new File("output/testing/IAArrestsOrgOutput_Alcohol-related.xls");
	private File testOutputFileTwoFiltered = new File("output/testing/DSMRegComOutput_Alcohol-related.xls");
	private File mockOutputFileOne;
	private File mockOutputFileTwo;
	private File mockOutputFileOneFiltered;
	private File mockOutputFileTwoFiltered;
	private RecordIOUtil mainIOUtil;
	private RecordIOUtil secondaryIOUtil;
	private RecordIOUtil tertiaryIOUtil;


	@BeforeClass
	public void setUpClass() {
		logger.info("********** Starting Test cases for SpiderEngine *****************");
		System.setProperty("TestingSpider", "true");
		engine = new SpiderEngine();
		state = State.IA;
		mainIOUtil = new RecordIOUtil(state.getName(), new ArrestRecord(), state.getEngines().get(0).getSite(), true);
		secondaryIOUtil = new RecordIOUtil(state.getName(), new ArrestRecord(), new DesMoinesRegisterComSite(new String[]{state.getName()}), true);
		tertiaryIOUtil = new RecordIOUtil(State.OK.getName(), new ArrestRecord(), State.OK.getEngines().get(0).getSite(), true);
	}

	@BeforeMethod
	public void setUpMethod() {
		Assert.assertTrue(testOutputFileOne.exists());
		Assert.assertTrue(testOutputFileTwo.exists());
		Assert.assertTrue(testOutputFileLN.exists());
		Assert.assertTrue(testOutputFileLNIneligible.exists());
		Assert.assertTrue(testOutputFileOneFiltered.exists());
		Assert.assertTrue(testOutputFileTwoFiltered.exists());
		//rename these to RecordIOUtil expected names
		mockOutputFileOne = new File(mainIOUtil.getMainDocPath());
		mockOutputFileTwo = new File(secondaryIOUtil.getMainDocPath());
		mockOutputFileOneFiltered = new File(mainIOUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL));
		mockOutputFileTwoFiltered = new File(secondaryIOUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL));
		testOutputFileOne.renameTo(mockOutputFileOne);
		testOutputFileTwo.renameTo(mockOutputFileTwo);
		testOutputFileOneFiltered.renameTo(mockOutputFileOneFiltered);
		testOutputFileTwoFiltered.renameTo(mockOutputFileTwoFiltered);

	}

	@AfterMethod
	public void tearDownMethod() {
		//delete output books

		//rename testOutputBooks back
		mockOutputFileOne.renameTo(testOutputFileOne);
		mockOutputFileTwo.renameTo(testOutputFileTwo);
		mockOutputFileOneFiltered.renameTo(testOutputFileOneFiltered);
		mockOutputFileTwoFiltered.renameTo(testOutputFileTwoFiltered);
		Assert.assertTrue(testOutputFileOne.exists());
		Assert.assertTrue(testOutputFileTwo.exists());
		Assert.assertTrue(testOutputFileOneFiltered.exists());
		Assert.assertTrue(testOutputFileTwoFiltered.exists());
	}
	@AfterClass
	public void tearDownClass() {
		Assert.assertTrue(new File(mainIOUtil.getOutputter().getMergedDocPath(mainIOUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL))).delete());
		Assert.assertTrue(new File(mainIOUtil.getOutputter().getMergedDocPath(mainIOUtil.getMainDocPath())).delete());
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for SpiderEngine *****************");
	}

	@Test
	public void getArrestRecordsByState() {
		throw new RuntimeException("Test not implemented");
	}
	
	@Test
	public void customizeArrestOutputs_TwoEngines() {
		//merge book and filter book created
		//no lexis nexis book
		RecordFilterEnum filter = RecordFilterEnum.ALCOHOL;
		engine.customizeArrestOutputs(mainIOUtil, state, filter);
		
		//verify outputs
		Assert.assertFalse(new File(mainIOUtil.getOutputter().getLNPath()).exists());
		Assert.assertTrue(new File(mainIOUtil.getOutputter().getMergedDocPath(null)).exists());
		Assert.assertTrue(new File(mainIOUtil.getOutputter().getMergedDocPath(mainIOUtil.getOutputter().getFilteredDocPath(filter))).exists());
	}

	@Test
	public void customizeArrestOutputs_NoFilter() {
		//only all records merge book created
		//no lexis nexis book
		RecordFilterEnum filter = RecordFilterEnum.NONE;
		engine.customizeArrestOutputs(mainIOUtil, state, filter);

		//verify outputs
		Assert.assertFalse(new File(mainIOUtil.getOutputter().getLNPath()).exists());
		Assert.assertTrue(new File(mainIOUtil.getOutputter().getMergedDocPath(null)).exists());
		Assert.assertFalse(new File(mainIOUtil.getOutputter().getMergedDocPath(mainIOUtil.getOutputter().getFilteredDocPath(filter))).exists());
	}

	@Test
	public void customizeArrestOutputs_OneEngineLexisNexisEligible() {
		//no merging
		//lexis nexis book created
		State state = State.OK;
		state.setEngines(Arrays.asList(new ArrestsDotOrgEngine(state.getName())));
		RecordIOUtil iOUtil = new RecordIOUtil(state.getName(), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{state.getName()}), true);
		File mockFile = new File(iOUtil.getMainDocPath());
		testOutputFileLN.renameTo(mockFile);
		engine.customizeArrestOutputs(iOUtil, state, RecordFilterEnum.NONE);
		
		//verify outputs
		Assert.assertTrue(new File(iOUtil.getOutputter().getLNPath()).exists());
		Assert.assertFalse(new File(iOUtil.getOutputter().getMergedDocPath(iOUtil.getMainDocPath())).exists());
		
		//rename testOutputFileLN
		mockFile.renameTo(testOutputFileLN);
		Assert.assertTrue(new File(iOUtil.getOutputter().getLNPath()).delete());
	}

	@Test
	public void customizeArrestOutputs_OneEngineLexisNexisEligibleNoneFound() {
		//no merging
		//lexis nexis book not created because no eligible records were found
		State state = State.OK;
		state.setEngines(Arrays.asList(new ArrestsDotOrgEngine(state.getName())));
		RecordIOUtil iOUtil = new RecordIOUtil(state.getName(), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{state.getName()}), true);
		File mockFile = new File(iOUtil.getMainDocPath());
		testOutputFileLNIneligible.renameTo(mockFile);
		engine.customizeArrestOutputs(iOUtil, state, RecordFilterEnum.NONE);

		//verify outputs
		//verify outputs
		Assert.assertFalse(new File(iOUtil.getOutputter().getLNPath()).exists());
		Assert.assertFalse(new File(iOUtil.getOutputter().getMergedDocPath(iOUtil.getMainDocPath())).exists());
		
		//rename testOutputFileLN
		mockFile.renameTo(testOutputFileLNIneligible);
	}	

	@Test
	public void filterOutLexisNexisEligibleRecords() {
		ArrestRecord record1 = new ArrestRecord();
		record1.setArrestDate(Calendar.getInstance());
		record1.setFirstName("EligibleJohn");
		record1.setMiddleName("Q");
		record1.setLastName("Public");
		record1.setDob(new Date());
		ArrestRecord record2 = new ArrestRecord();
		record2.setArrestDate(Calendar.getInstance());
		record2.setLastName("Nelson");
		record2.setDob(new Date());
		ArrestRecord record3 = new ArrestRecord();
		record3.setArrestDate(Calendar.getInstance());
		record3.setFirstName("EligibleJoe");
		record3.setLastName("Gunny");
		record3.setDob(new Date());
		ArrestRecord record4 = new ArrestRecord();
		record4.setFirstName("Jack");
		record4.setLastName("Sprout");
		record4.setDob(new Date());

		RecordSheet recordSheet1 = new RecordSheet();
		recordSheet1.addRecord(record1);
		recordSheet1.addRecord(record2);
		RecordSheet recordSheet2 = new RecordSheet();
		recordSheet2.addRecord(record4);
		recordSheet2.addRecord(record3);

		RecordWorkbook recordBook = new RecordWorkbook();
		recordBook.addSheet(recordSheet2);
		recordBook.addSheet(recordSheet1);

		RecordWorkbook eligibleRecordBook = engine.filterOutLexisNexisEligibleRecords(recordBook);
		Assert.assertEquals(eligibleRecordBook.sheetCount(), recordBook.sheetCount());
		Assert.assertEquals(eligibleRecordBook.getSheet(0).recordCount(), 1);
		Assert.assertEquals(eligibleRecordBook.getSheet(1).recordCount(), 1);

	}
}
