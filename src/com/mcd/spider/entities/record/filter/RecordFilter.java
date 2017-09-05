package com.mcd.spider.entities.record.filter;

import java.util.Arrays;
import java.util.List;

public class RecordFilter {
	public enum RecordFilterEnum {
		ALCOHOL("Alcohol-related", Arrays.asList("alcohol", "oui", "dui", "owi", "dwi", "bui", "open container", "intox")),
		TRAFFIC("Traffic-related", Arrays.asList("speed", "traffic")),
        NONE("No-filter", Arrays.asList(""));

		private String filterName;
		private List<String> keywords;
		
		RecordFilterEnum(String filterName, List<String> keywords) {
			this.filterName = filterName;
			this.keywords = keywords;
		}
		public String filterName() {
			return filterName;
		}
		public List<String> keywords() {
			return keywords;
		}
		
		public static RecordFilterEnum findFilter(String filterName) {
            if (filterName.equals("")) {
                return RecordFilterEnum.NONE;
            }
			for (RecordFilterEnum filterEnum : RecordFilterEnum.values()) {
				if (filterName.equalsIgnoreCase(filterEnum.name())) {
					return filterEnum;
				}
			}
			return null;
		}
	}
	
	public static boolean filter(String charge, RecordFilterEnum filter) {
		boolean matches = false;
		for (String keyword : filter.keywords) {
			if (!matches) {
				matches = charge.toLowerCase().contains(keyword);
			}
		}
		return matches;
	}
}
