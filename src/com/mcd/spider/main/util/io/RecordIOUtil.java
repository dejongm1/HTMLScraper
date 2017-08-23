package com.mcd.spider.main.util.io;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author u569220
 *
 */
public class RecordIOUtil {
	
	public static final Logger logger = Logger.getLogger(RecordIOUtil.class);
	private static final String EXT = ".xls";
    private static final String OUTPUT_DIR = "output/";
    private static final String TRACKING_DIR = "tracking/";
	
	private String mainDocName;
	private RecordInputUtil inputter;
	private RecordOutputUtil outputter;
	private File crawledIdFile;
    private File uncrawledIdFile;
	private Record record;
	
	public RecordIOUtil(State state, Record record, Site site) {
        this.crawledIdFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Archive.txt");
        this.uncrawledIdFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Uncrawled.txt");
		this.mainDocName = OUTPUT_DIR + state.getName() + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
		this.record = record;
		this.outputter = new RecordOutputUtil(this, state, site);
		this.inputter = new RecordInputUtil(this);
	}

	public String getMainDocName() {
		return mainDocName;
	}

    public File getCrawledIdFile() {
        return crawledIdFile;
    }

    public File getUncrawledIdFile() {
        return uncrawledIdFile;
    }

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public RecordInputUtil getInputter() {
		return inputter;
	}

	public RecordOutputUtil getOutputter() {
		return outputter;
	}

	public Set<Record> mergeRecordsFromSheet(File fileOne, File fileTwo, int sheetNumber) {
		Set<Record> storedRecordsOne = inputter.readRecordsFromSheet(fileOne, sheetNumber);
		Set<Record> storedRecordsTwo = inputter.readRecordsFromSheet(fileTwo, sheetNumber);
		Set<Record> compiledRecords = new HashSet<>();
		Set<Record> outerSet = new HashSet<>(storedRecordsOne);
		Set<Record> innerSet = new HashSet<>(storedRecordsTwo);

		for (Record recordOne : outerSet) {
			for (Record recordTwo : innerSet) {
				if (recordOne.matches(recordTwo)) {
					recordOne.merge(recordTwo);
					compiledRecords.add(recordOne);
					storedRecordsTwo.remove(recordTwo);
				} else {
					compiledRecords.add(recordOne);
					storedRecordsOne.remove(recordOne);
				}
			}
		}
		compiledRecords.addAll(storedRecordsOne);
		compiledRecords.addAll(storedRecordsTwo);
		return compiledRecords;
	}

}
