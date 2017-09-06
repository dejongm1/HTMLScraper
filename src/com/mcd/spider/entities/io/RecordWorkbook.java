package com.mcd.spider.entities.io;

import java.util.List;

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
}
