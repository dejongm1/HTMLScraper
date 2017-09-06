package com.mcd.spider.entities.io;

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

	private List<RecordSheet> recordSheet;
	
	public RecordWorkbook(List<RecordSheet> recordSheet) {
		this.recordSheet = recordSheet;
	}

	public List<RecordSheet> getRecordSheet() {
		return recordSheet;
	}

	public void setRecordSheet(List<RecordSheet> recordSheet) {
		this.recordSheet = recordSheet;
	}
	
	//methods to access inputter and outputter
	
	//when this class is implememnted, change to a non-static method that returns whther this is empty or not
	public static boolean isEmpty(List<Set<Record>> recordSetList) {
		boolean empty = true;
		if (recordSetList!=null && !recordSetList.isEmpty()) {
			for (Set<Record> recordSet : recordSetList) {
				if (recordSet.size()>0) {
					empty = false;
				}
			}
		}
		return empty;
	}
}
