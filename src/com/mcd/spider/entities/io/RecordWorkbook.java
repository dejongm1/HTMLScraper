package com.mcd.spider.entities.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mcd.spider.entities.record.Record;
import com.mcd.spider.util.SpiderConstants;

/**
 * 
 * @author u569220
 *
 */

public class RecordWorkbook {
	
	//initialize ioutil when this is instantiated? each workbook would get it's own ioUtil

	private List<RecordSheet> sheets;
	private int numOfSheets;

	public RecordWorkbook() {
		sheets = new ArrayList<>();
	}

	public RecordWorkbook(List<RecordSheet> recordSheet) {
		this.sheets = recordSheet;
		this.numOfSheets = sheets.size();
	}
	
	public List<RecordSheet> getSheets() {
		return sheets;
	}
	
	public RecordSheet getSheet(int sheetIndex) {
		return sheets.get(sheetIndex);
	}
	
	public RecordSheet getSheet(String sheetName) {
		for (RecordSheet sheet : sheets) {
			if (sheet.getSheetName().equalsIgnoreCase(sheetName)) {
				return sheet;
			}
		}
		return null;
	}

	public Record getFirstRecordFromSheet(int sheetIndex) {
		return this.getSheet(sheetIndex).getFirstRecordFromSheet();
	}

	public Record getFirstRecordFromSheet(String sheetName) {
		return this.getSheet(sheetName).getFirstRecordFromSheet();
	}
	
	public void setSheets(List<RecordSheet> recordSheet) {
		this.sheets = recordSheet;
	}
	
	public int sheetCount() {
		if (numOfSheets==0) {
			return sheets.size();
		} else {
			return numOfSheets;
		}
	}
	
	public void addSheet(RecordSheet sheet) {
		if (this.isEmpty()) { //first sheet gets common name
			sheet.setSheetName(SpiderConstants.MAIN_SHEET_NAME);
		} else if (sheet.getSheetName()==null) {
			sheet.setSheetName(sheet.extractSheetName());
		}
		this.sheets.add(sheet);
		numOfSheets++;
	}

	public void removeSheet(int sheetIndex) {
		this.sheets.remove(sheetIndex);
		numOfSheets--;
	}
	
	public Set<Record> getRecordsFromSheet(int sheetIndex) {
		return this.getSheet(sheetIndex).getRecords();
	}
	
	public Set<Record> getRecordsFromSheet(String sheetName) {
		return this.getSheet(sheetName).getRecords();
	}
	
	public String[] getSheetNames() {
		String[] sheetNames = new String[this.getSheets().size()];
		for (int s=0; s<this.getSheets().size();s++) {
			sheetNames[s] = this.getSheet(s).getSheetName();
		}
		return sheetNames;
	}
	
	public boolean isEmpty() {
		boolean empty = true;
		if (this!=null && !this.getSheets().isEmpty()) {
			for (RecordSheet recordSet : this.getSheets()) {
				if (recordSet.getRecords().size()>0) {
					empty = false;
				}
			}
		}
		return empty;
	}
}
