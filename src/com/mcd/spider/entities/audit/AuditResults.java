package com.mcd.spider.entities.audit;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author Michael De Jong
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
	private Document actualSiteMap;
	private File generatedSiteMap;
	
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
	
	public String prettyPrint(boolean leanReport) {
		StringBuilder sb = new StringBuilder();
		if (!leanReport) {
			for (PageAuditResult auditResult : allResponses) {
				//logger.info(result.getCode()==0?result.getUrl() + " didn't have an html file":result.prettyPrint()));
				sb.append(auditResult.prettyPrint());
			}
		}
		sb.append("\n\t\t200 series results: " + twoHundredResponses.size());
        sb.append("\n\t\t300 series results: " + threeHundredResponses.size());
        sb.append("\n\t\t400 series results: " + fourHundredResponses.size());
        sb.append("\n\t\t500 series results: " + fiveHundredResponses.size());
        //TODO Add total links counts and search results counts here
        if (getGeneratedSiteMap()!=null) {
            sb.append("\n\n\t\tSitemap generated and located at " + this.generatedSiteMap.getAbsolutePath());
        }
		return sb.toString();
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

	public Document getActualSiteMap() {
		return actualSiteMap;
	}

	public void setActualSiteMap(Document actualSiteMap) {
		this.actualSiteMap = actualSiteMap;
	}

	public File getGeneratedSiteMap() {
		return generatedSiteMap;
	}

	public void setGeneratedSiteMap(File siteMap) {
		this.generatedSiteMap = siteMap;
	}
	
}
