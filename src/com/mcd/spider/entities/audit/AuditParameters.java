package com.mcd.spider.entities.audit;

import com.mcd.spider.util.MainInputUtil;
import com.mcd.spider.util.SpiderConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Michael De Jong
 *
 */

public class AuditParameters {

	private String urlToAudit;
	private List<Term> terms = new ArrayList<>();
	private Integer depth;
	private int sleepTime;
	private boolean performanceTest;
	private boolean leanReportFlag;
	private boolean includeCommonWordsFlag;
	private MainInputUtil mainInputUtil = new MainInputUtil();
	
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
					String url = mainInputUtil.convertToUrl(parameter);
					if (url==null) {
						url = (String )mainInputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
					}
					if (url.endsWith("/")) {
				        url = url.substring(0, url.lastIndexOf("/"));
			        }
					setUrlToAudit(url);
				} else if (arg.startsWith("-depth")) {
					setDepth(mainInputUtil.convertToNumber(parameter));
				} else if (arg.startsWith("-search") || arg.startsWith("-term")) {
					String termString = parameter;
					if (termString!=null) {
						for (String term : termString.split(",")) {
							addTerm(new Term(term.trim(), 0));
						}
					}
				} else if (arg.startsWith("-perform") || arg.startsWith("-load")) {
					setPerformanceTest(true);
				} else if (arg.startsWith("-sleep")) {
					setSleepTime(mainInputUtil.convertToNumber(parameter));
				} else if (arg.startsWith("-full")) {
					setLeanReportFlag(true);
				} else if (arg.startsWith("-common") || arg.startsWith("-frequent")) {
					setIncludeCommonWordsFlag(true);
				} else if (arg.startsWith("-sitemap") || arg.startsWith("-map")) {
					setIncludeCommonWordsFlag(true);
				} else {
					System.out.println("I didn't recognize argument \"" + arg + "\". Ignoring and proceeding...");
				}
			}
		}
		//check for required args (url)
        if (urlToAudit==null) {
            String url = (String )mainInputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
            if (url.endsWith("/")) {
                url = url.substring(0, url.lastIndexOf("/"));
            }
            setUrlToAudit(url);
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

	public boolean isLeanReportFlag() {
		return leanReportFlag;
	}

	public void setLeanReportFlag(boolean leanReportFlag) {
		this.leanReportFlag = leanReportFlag;
	}

	public boolean isIncludeCommonWordsFlag() {
		return includeCommonWordsFlag;
	}

	public void setIncludeCommonWordsFlag(boolean includeCommonWordsFlag) {
		this.includeCommonWordsFlag = includeCommonWordsFlag;
	}
	
}
