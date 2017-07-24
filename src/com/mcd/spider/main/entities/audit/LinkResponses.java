package com.mcd.spider.main.entities.audit;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author u569220
 *
 */

public class LinkResponses {
	
	private SortedSet<LinkResponse> oneHundredResponses;
	private SortedSet<LinkResponse> twoHundredResponses;
	private SortedSet<LinkResponse> threeHundredResponses;
	private SortedSet<LinkResponse> fourHundredResponses;
	private SortedSet<LinkResponse> fiveHundredResponses;
	private SortedSet<LinkResponse> otherResponses;
	private SortedSet<LinkResponse> allResponses;
	
	public LinkResponses() {
		oneHundredResponses = new TreeSet<>();
		twoHundredResponses = new TreeSet<>();
		threeHundredResponses = new TreeSet<>();
		fourHundredResponses = new TreeSet<>();
		fiveHundredResponses = new TreeSet<>();
		otherResponses = new TreeSet<>();
		allResponses = new TreeSet<>();
	}
	
	public void addResponse(LinkResponse response) {
		if (response.getCode()>=100 && response.getCode()<200) {
			oneHundredResponses.add(response);
		} else if (response.getCode()>=200 && response.getCode()<300) {
			twoHundredResponses.add(response);
		} else if (response.getCode()>=300 && response.getCode()<400) {
			threeHundredResponses.add(response);
		} else if (response.getCode()>=400 && response.getCode()<500) {
			fourHundredResponses.add(response);
		}  else if (response.getCode()>=500 && response.getCode()<600) {
			fiveHundredResponses.add(response);
		} else {
			otherResponses.add(response);
		}
		allResponses.add(response);
	}
	
	public SortedSet<LinkResponse> getOneHundredResponses() {
		return oneHundredResponses;
	}

	public SortedSet<LinkResponse> getTwoHundredResponses() {
		return twoHundredResponses;
	}

	public SortedSet<LinkResponse> getThreeHundredResponses() {
		return threeHundredResponses;
	}

	public SortedSet<LinkResponse> getFourHundredResponses() {
		return fourHundredResponses;
	}

	public SortedSet<LinkResponse> getFiveHundredResponses() {
		return fiveHundredResponses;
	}
	
	public SortedSet<LinkResponse> getAllResponses() {
		return allResponses;
	}
	
	public long count() {
		return (long) this.oneHundredResponses.size()
					+ this.twoHundredResponses.size()
					+ this.threeHundredResponses.size()
					+ this.fourHundredResponses.size()
					+ this.fiveHundredResponses.size()
					+ this.otherResponses.size();
	}

}
