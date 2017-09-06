package com.mcd.spider.entities.io;

import java.util.Set;

import com.mcd.spider.entities.record.Record;

import jxl.Cell;

/**
 * 
 * @author u569220
 *
 */

public class RecordSheet {

	private Cell[] headerRow;
	private String sheetName;
	private Set<Record> sheetRecords;
	
	public RecordSheet(Cell[] headerRow, String sheetName, Set<Record> sheetRecords) {
		this.headerRow = headerRow;
		this.sheetName = sheetName;
		this.sheetRecords = sheetRecords;
	}
	
	public Cell[] getHeaderRow() {
		return headerRow;
	}

	public void setHeaderRow(Cell[] headerRow) {
		this.headerRow = headerRow;
	}

	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public Set<Record> getSheetRecords() {
		return sheetRecords;
	}
	public void setSheetRecords(Set<Record> sheetRecords) {
		this.sheetRecords = sheetRecords;
	}

	//method to get specific row
	//methods to call output/input methods?
	
}
