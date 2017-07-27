package com.mcd.spider.main.entities.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchResults {
	//not sure what this will look like yet
	//is it a simple map? list of words and variations with count?

	private PageAuditResult sourcePage;
	private List<Term> results;
	
	public SearchResults(PageAuditResult page) {
		this.results = new ArrayList<>();
		this.sourcePage = page;
	}
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		if (results!=null) {
            sb.append("\n\tSearch Results: ");
            for (Term term : this.results)  {
                sb.append("\n\t\t" + term.getWord() + " was found " + term.getCount() + " times");
            }
		}
		return sb.toString();
	}
	
	public PageAuditResult getSourcePage() {
		return sourcePage;
	}

	public void setSourcePage(PageAuditResult sourcePage) {
		this.sourcePage = sourcePage;
	}

	public List<Term> getResults() {
		return results;
	}

	public void setResults(List<Term> results) {
		this.results = results;
	}
	
	public void add(Term term) {
		getResults().add(term);
	}
}
