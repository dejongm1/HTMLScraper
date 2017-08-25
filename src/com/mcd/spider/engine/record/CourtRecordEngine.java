package com.mcd.spider.engine.record;

import com.mcd.spider.entities.record.CourtRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.exception.SpiderException;

import java.util.List;

public interface CourtRecordEngine {

	Site getSite(String[] args);
	void getCourtRecords(State state, long maxNumberOfResults, RecordFilter.RecordFilterEnum filter) throws SpiderException;

    List<Record> filterRecords(List<CourtRecord> fullCourtRecords);
}
