package com.mcd.spider.entities.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mcd.spider.entities.record.Record;

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
	
	public void add(RecordSheet sheet) {
		this.sheets.add(sheet);
	}
	
	public String[] getSheetNames() {
		String[] sheetNames = new String[this.getSheets().size()];
		for (int s=0; s<this.getSheets().size();s++) {
			sheetNames[s] = this.getSheets().get(s).getSheetName();
		}
		return sheetNames;
	}
	
	//TODO remove when done refactoring
	public static boolean isEmpty(List<Set<Record>> book) {
		boolean empty = true;
		if (book!=null && !book.isEmpty()) {
			for (Set<Record> recordSet : book) {
				if (recordSet.size()>0) {
					empty = false;
				}
			}
		}
		return empty;
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
