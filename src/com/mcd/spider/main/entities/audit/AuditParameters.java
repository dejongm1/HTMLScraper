package com.mcd.spider.main.entities.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mcd.spider.main.util.InputUtil;
import com.mcd.spider.main.util.SpiderConstants;

/**
 * 
 * @author u569220
 *
 */

public class AuditParameters {

	private String urlToAudit;
	private List<Term> terms = new ArrayList<>();
	private Integer depth;
	private int sleepTime;
	private boolean performanceTest;
	private boolean fullReportFlag;
	private boolean includeCommonWordsFlag;
	private InputUtil inputUtil = new InputUtil();
	
	public AuditParameters(String argString) throws IOException {
		parseOutParameters(argString);
	}
	
	private void parseOutParameters(String argString) throws IOException {
		String[] newArgs;
		if (!argString.equals("")) {
			newArgs = argString.trim().toLowerCase().split("(?=-)");
			for (String arg : newArgs){
				String parameter = arg.trim().substring(arg.indexOf(' ')+1);
				if (arg.startsWith("-url")) {
					String url = inputUtil.convertToUrl(parameter);
					if (url==null) {
						url = (String )inputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
					} else if (url.endsWith("/")) {
				        url = url.substring(0, url.lastIndexOf("/"));
			        } 
					setUrlToAudit(url);
				} else if (arg.startsWith("-depth")) {
					setDepth(inputUtil.convertToNumber(parameter));
				} else if (arg.startsWith("-search") || arg.startsWith("-term")) {
					String termString = parameter;
					if (termString!=null) {
						for (String term : termString.split(",")) {
							addTerm(new Term(term.trim(), 0));
						}
					}
				} else if (arg.startsWith("-perform")) {
					setPerformanceTest(true);
				} else if (arg.startsWith("-sleep")) {
					setSleepTime(inputUtil.convertToNumber(parameter));
				} else if (arg.startsWith("-full")) {
					setFullReportFlag(true);
				} else if (arg.startsWith("-common")) {
					setIncludeCommonWordsFlag(true);
				} else {
					System.out.println("I didn't recognize argument \"" + arg + "\". Ignoring and proceeding...");
				}
			}
		}
	}

	public String getUrlToAudit() {
		return urlToAudit;
	}

	public void setUrlToAudit(String urlToAudit) {
		this.urlToAudit = urlToAudit;
	}

	public List<Term> getTerms() {
		return terms;
	}

	public void addTerm(Term term) {
		this.terms.add(term);
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public boolean isPerformanceTest() {
		return performanceTest;
	}

	public void setPerformanceTest(boolean performanceTest) {
		this.performanceTest = performanceTest;
	}

	public boolean isFullReportFlag() {
		return fullReportFlag;
	}

	public void setFullReportFlag(boolean fullReportFlag) {
		this.fullReportFlag = fullReportFlag;
	}

	public boolean isIncludeCommonWordsFlag() {
		return includeCommonWordsFlag;
	}

	public void setIncludeCommonWordsFlag(boolean includeCommonWordsFlag) {
		this.includeCommonWordsFlag = includeCommonWordsFlag;
	}
	
}
