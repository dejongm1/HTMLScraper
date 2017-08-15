package com.mcd.spider.main.util.io;

import org.apache.log4j.Logger;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;

public class RecordIOUtil {
	
	public static final Logger logger = Logger.getLogger(RecordIOUtil.class);
	private static final String EXT = ".xls";
	
	private String docName;
	private RecordInputUtil inputter;
	private RecordOutputUtil outputter;
	
	public RecordIOUtil(State state, Record record, Site site) {
		this.docName = state.getName() + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
		this.outputter = new RecordOutputUtil(docName, state, record, site);
		this.inputter = new RecordInputUtil(docName);
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
	
	

}
