package test.com.mcd.spider.main.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.main.util.SpiderUtil;
import com.mcd.spider.main.util.io.RecordIOUtil;

public class RecordIOUtilTest {

	private File testMergeFileOne = new File("resources/test/Iowa_ArrestRecord_ArrestsOrg.xls");
	private File testMergeFileTwo = new File("resources/test/Iowa_ArrestRecord_DesMoinesRegisterCom.xls");
	private RecordIOUtil ioUtil;
	
	@BeforeClass
	public void setUp() {
		System.setProperty("runInEclipse", "true");
//		SpiderUtil spiderUtil = new SpiderUtil();
		ioUtil = new RecordIOUtil(State.getState("IA"), new ArrestRecord(), new ArrestsDotOrgSite(new String[]{"iowa"}));
	}

	@AfterClass
	public void breakDown() {
		
	}

	@Test
	public void testMergeRecordsFromSheet() throws IOException {
		Assert.assertTrue(testMergeFileOne.exists());
		Assert.assertTrue(testMergeFileTwo.exists());
		
		Set<Record> mergedRecords = ioUtil.mergeRecordsFromSheet(testMergeFileOne, testMergeFileTwo, 0);
		
		Assert.assertEquals(mergedRecords.size(), 10); //11 if I can make this work for name suffixes
	}
	
	

}
