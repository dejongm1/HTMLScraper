package com.mcd.scraper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap();
	private static Map<String, State> nameToState = new HashMap();

	private String abbreviation;
	private String name;
	private String url;
	private String selector;
	private static final String SELECTOR_SEPARATOR = ">>>";

	public static final State IA = new State("IA", "Iowa", "http://iowa.arrests.org",
			".profile-card .title a[href]" + SELECTOR_SEPARATOR
			+ ".info .section-content div, .section-content.charges .charge-title, .section-content.charges .charge-description");

	//future 
	//private State(String abbreviation, String name, String urls[], String[]s selectors) {
	private State(String abbreviation, String name, String url, String selector) {
		this.abbreviation = abbreviation;
		this.name = name;
		this.url = url;
		this.selector = selector;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name, this);
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	public String getName() {
		return name;
	}
	public String getUrl() {
		return url;
	}
	public String getSelector() {
		return selector;
	}
	public static List<State> values() {
		return (List<State>) abbreviationToState.values();
	}
	public static State getState(String value) {
		//way to ignore case here?
		if (value.length()==2) {
			return abbreviationToState.get(value);
		} else {
			return nameToState.get(value);
		}
	}
}
