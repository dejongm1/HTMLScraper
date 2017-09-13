package com.mcd.spider.entities.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;

/**
 * 
 * @author u569220
 *
 */

public class RecordSheet {

	public static final Logger logger = Logger.getLogger(RecordSheet.class);
	
	private String sheetName;
	private Set<Record> records;
	private Method sheetNameGetter;

	public RecordSheet() {
		this.records = new HashSet<>();
	}
	
	public RecordSheet(Method methodGetter) {
		this.records = new HashSet<>();
		sheetNameGetter = methodGetter;
	}
	
	public RecordSheet(/*Cell[] headerRow, */String sheetName, Set<Record> sheetRecords) {
		this.sheetName = sheetName;
		this.records = sheetRecords;
		//		this.headerRow = headerRow;
	}
	
	public RecordSheet(String sheetName, List<Record> sheetRecords) {
		this(sheetName, new HashSet<>(sheetRecords));
	}
	
	public static RecordSheet copy(RecordSheet recordSheet){
		RecordSheet rs = new RecordSheet();
		rs.setSheetName(recordSheet.getSheetName());
		rs.setRecords(new HashSet<>(recordSheet.getRecords()));
		return rs;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public Set<Record> getRecords() {
		return records;
	}
	public void setRecords(Set<Record> sheetRecords) {
		this.records = sheetRecords;
	}
	public void addRecord(Record sheetRecord) {
		if (this.sheetName==null) {
			this.sheetName = extractSheetName(sheetRecord);
		}
		this.records.add(sheetRecord);
	}
	public void removeRecord(Record record) {
		this.records.remove(record);
	}
	public boolean isEmpty() {
		return this.records.isEmpty();
	}
	public void addAllRecords(RecordSheet recordSheetOne) {
		for (Record record : recordSheetOne.getRecords()) {
			this.addRecord(record);
		}
	}
	public int recordCount(){
		return records.size();
	}
	public Record getFirstRecordFromSheet() {
		return (Record) this.getRecords().toArray()[0];
	}
	public String extractSheetName(Record record) {
		if (record instanceof ArrestRecord) {
			if (sheetNameGetter!=null) {
				try {
					if ((String)sheetNameGetter.invoke((ArrestRecord)record)!=null && !sheetNameGetter.invoke((ArrestRecord)record).equals("")) {
						return (String)sheetNameGetter.invoke((ArrestRecord)record);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.error("Trouble assigning a sheetname with " + sheetNameGetter.getName(), e);
				}
			} else {
				return ((ArrestRecord)record).getCounty();
			}
		}
		return "UnknownSheet";
	}
	
	public String extractSheetName() {
		if (!this.getRecords().isEmpty()) {
			Record record = this.getRecords().toArray(new Record[this.getRecords().size()])[0];
			if (record instanceof ArrestRecord) {
				return ((ArrestRecord)record).getCounty();
			}
		}
		return "UnknownSheet";
	}
}
