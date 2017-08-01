package com.mcd.spider.main.entities.record.filter;

import java.util.Arrays;
import java.util.List;

import com.mcd.spider.main.entities.record.ArrestRecord;

public class ArrestRecordFilter {
	public enum ArrestRecordFilterEnum {
		ALCOHOL("Alcohol-related", Arrays.asList("alcohol", "oui", "dui", "owi", "dwi", "bui", "open container", "intox")),
		TRAFFIC("Traffic-related", Arrays.asList("speed", "traffic"));
	
		private String filterName;
		private List<String> keywords;
		
		ArrestRecordFilterEnum(String filterName, List<String> keywords) {
			this.filterName = filterName;
			this.keywords = keywords;
		}
		public String filterName() {
			return filterName;
		}
		public List<String> keywords() {
			return keywords;
		}
		
		public static ArrestRecordFilterEnum findFilter(String filterName) {
			for (ArrestRecordFilterEnum filterEnum : ArrestRecordFilterEnum.values()) {
				if (filterName.equalsIgnoreCase(filterEnum.name())) {
					return filterEnum;
				}
			}
			return null;
		}
	}
	
	public static boolean filter(String charge, ArrestRecordFilterEnum filter) {
		boolean matches = false;
		for (String keyword : filter.keywords) {
			if (!matches) {
				matches = charge.toLowerCase().contains(keyword);
			}
		}
		return matches;
	}
}
