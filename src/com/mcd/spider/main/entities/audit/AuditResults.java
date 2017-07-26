package com.mcd.spider.main.entities.audit;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author u569220
 *
 */

public class AuditResults {
	
	private SortedSet<PageAuditResult> oneHundredResponses;
	private SortedSet<PageAuditResult> twoHundredResponses;
	private SortedSet<PageAuditResult> threeHundredResponses;
	private SortedSet<PageAuditResult> fourHundredResponses;
	private SortedSet<PageAuditResult> fiveHundredResponses;
    private SortedSet<PageAuditResult> offlineResponses;
	private SortedSet<PageAuditResult> otherResponses;
	private SortedSet<PageAuditResult> allResponses;
	
	public AuditResults() {
		oneHundredResponses = new TreeSet<>();
		twoHundredResponses = new TreeSet<>();
		threeHundredResponses = new TreeSet<>();
		fourHundredResponses = new TreeSet<>();
        fiveHundredResponses = new TreeSet<>();
        offlineResponses = new TreeSet<>();
		otherResponses = new TreeSet<>();
		allResponses = new TreeSet<>();
	}
	
	public void addResponse(PageAuditResult response) {
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
        }  else if (response.getCode()==0) {
            offlineResponses.add(response);
        } else {
			otherResponses.add(response);
		}
		allResponses.add(response);
	}
	
	public SortedSet<PageAuditResult> getOneHundredResponses() {
		return oneHundredResponses;
	}

	public SortedSet<PageAuditResult> getTwoHundredResponses() {
		return twoHundredResponses;
	}

	public SortedSet<PageAuditResult> getThreeHundredResponses() {
		return threeHundredResponses;
	}

	public SortedSet<PageAuditResult> getFourHundredResponses() {
		return fourHundredResponses;
	}

	public SortedSet<PageAuditResult> getFiveHundredResponses() {
		return fiveHundredResponses;
	}

    public SortedSet<PageAuditResult> getOfflineResponses() {
        return offlineResponses;
    }
	
	public SortedSet<PageAuditResult> getAllResponses() {
		return allResponses;
	}
	
	public long count() {
		return (long) this.allResponses.size();
	}

}
