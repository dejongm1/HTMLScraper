package com.mcd.spider.main.engine.record;

import com.mcd.spider.main.entities.record.CourtRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.SpiderException;

import java.util.List;

public interface CourtRecordEngine {

	Site getSite(String[] args);
	void getCourtRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException;

    List<Record> filterRecords(List<CourtRecord> fullCourtRecords);
}
