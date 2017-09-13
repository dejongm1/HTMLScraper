package com.mcd.spider.entities.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;

/**
 * 
 * @author u569220
 *
 */

public class RecordSheet {

	private String sheetName;
	private Set<Record> records;


	public RecordSheet() {
		this.records = new HashSet<>();
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
	public void add(Record sheetRecord) {
		if (this.sheetName==null) {
			this.sheetName = extractSheetName(sheetRecord);
		}
		this.records.add(sheetRecord);
	}
	public void remove(Record record) {
		this.records.remove(record);
	}
	public boolean isEmpty() {
		return this.records.isEmpty();
	}
	public void addAll(RecordSheet recordSheetOne) {
		for (Record record : recordSheetOne.getRecords()) {
			this.add(record);
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
			return ((ArrestRecord)record).getCounty();
		} else {
			return "UnknownSheet";
		}
	}
	public String extractSheetName() {
		String sheetName = "UnknownSheet";
		if (!this.getRecords().isEmpty()) {
			Record record = this.getRecords().toArray(new Record[this.getRecords().size()])[0];
			if (record instanceof ArrestRecord) {
				sheetName = ((ArrestRecord)record).getCounty();
			}
		}
		return sheetName;
	}
}
