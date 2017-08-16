package com.mcd.spider.main.util.io;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import org.apache.log4j.Logger;

import java.io.File;

public class RecordIOUtil {
	
	public static final Logger logger = Logger.getLogger(RecordIOUtil.class);
	private static final String EXT = ".xls";
    private static final String OUTPUT_DIR = "output/";
    private static final String TRACKING_DIR = "tracking/";
	
	private String docName;
	private RecordInputUtil inputter;
	private RecordOutputUtil outputter;
	private File idFile;
//	private Site site;
	
	public RecordIOUtil(State state, Record record, Site site) {
        this.idFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Archive.txt");
		this.docName = OUTPUT_DIR + state.getName() + "_" + record.getClass().getSimpleName() + "_" + site.getName() + EXT;
		this.outputter = new RecordOutputUtil(docName, idFile, state, record, site);
		this.inputter = new RecordInputUtil(docName, idFile, site, record);
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
