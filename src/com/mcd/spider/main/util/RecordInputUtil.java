package com.mcd.spider.main.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.mcd.spider.main.entities.record.Record;

import jxl.write.WritableWorkbook;

/**
 * 
 * @author u569220
 *
 */
public class RecordInputUtil {

	public static final Logger logger = Logger.getLogger(RecordInputUtil.class);

	private String docName;
	private WritableWorkbook workbook;
//	private Record record;
	private static final String OUTPUT_DIR = "output/";

	public RecordInputUtil(RecordOutputUtil outputUtil) {
		Calendar date = Calendar.getInstance();
		this.docName = outputUtil.getDocName();
//		this.state = outputUtil.getState();
//		this.record = outputUtil.getRecord();
//		this.site = outputUtil.getSite();
	}

	public String getDocName() {
		return docName;
	}
	public WritableWorkbook getWorkbook() {
		return workbook;
	}
	public void setWorkbook(WritableWorkbook workbook) {
		this.workbook = workbook;
	}

	public List<Record> readDefaultSpreadsheet() {
		//TODO
		List<Record> storedRecords = new ArrayList<>();
		Object sheet = readSpreadsheet(docName);


		return storedRecords;
	}
	public List<Record> readSpreadsheet(Object fileToRead) {
		//TODO
		List<Record> storedRecords = new ArrayList<>();


		return storedRecords;
	}
	
	public List<Record> mergeRecordsFromSpreadsheets(Object fileOne, Object fileTwo) {
		//TODO
		List<Record> storedRecordsOne = readSpreadsheet(fileOne);
		List<Record> storedRecordsTwo = readSpreadsheet(fileTwo);
		List<Record> compiledRecords = new ArrayList<>();
		
		
		
		
		return compiledRecords;
	}
}
