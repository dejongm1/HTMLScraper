package com.mcd.spider.main.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;

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
	
	private String docName;
	private RecordInputUtil inputter;
	private RecordOutputUtil outputter;
	private File idFile;
	private Record record;
	
	public RecordIOUtil(State state, Record record, Site site) {
        this.idFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Archive.txt");
		this.docName = OUTPUT_DIR + state.getName() + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
		this.record = record;
		this.outputter = new RecordOutputUtil(this, state, site);
		this.inputter = new RecordInputUtil(this);
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public File getIdFile() {
		return idFile;
	}

	public void setIdFile(File idFile) {
		this.idFile = idFile;
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

	public void setInputter(RecordInputUtil inputter) {
		this.inputter = inputter;
	}

	public RecordOutputUtil getOutputter() {
		return outputter;
	}

	public void setOutputter(RecordOutputUtil outputter) {
		this.outputter = outputter;
	}

	public void mergeRecordsFromSpreadsheets(File fileOne, File fileTwo) {
		Set<Record> storedRecordsOne = inputter.readSpreadsheet(fileOne);
		Set<Record> storedRecordsTwo = inputter.readSpreadsheet(fileTwo);
		Set<Record> compiledRecords = new HashSet<>();
		Set<Record> outerSet = new HashSet<>(storedRecordsOne);
		Set<Record> innerSet = new HashSet<>(storedRecordsTwo);
//		
		for (Record recordOne : outerSet) {
			for (Record recordTwo : innerSet) {
				if (recordOne.matches(recordTwo)) {
					recordOne.merge(recordTwo);
					compiledRecords.add(recordOne);
					storedRecordsTwo.remove(recordTwo);
				} else {
					compiledRecords.add(recordOne);
				}
			}
		}
		compiledRecords.addAll(storedRecordsTwo);		
		//create new spreadsheet with compiled records
		outputter.createMergedSpreadsheet(new ArrayList<>(compiledRecords));
		
	}
	

}
