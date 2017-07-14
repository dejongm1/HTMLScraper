package com.mcd.scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap();
	private static Map<String, State> nameToState = new HashMap();
	private static List<State> allStates = new ArrayList<>();;

	private String abbreviation;
	private String name;
	private Site[] sites;

	public static final State IA = new State("IA", "Iowa", new Site[]{Site.ArrestsDotOrg});
	public static final State IL = new State("IL", "Illinois", new Site[]{Site.ArrestsDotOrg});

	private State(String abbreviation, String name, Site[] sites) {
		this.abbreviation = abbreviation;
		this.name = name;
		this.sites = sites;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name, this);
		allStates.add(this);
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	public String getName() {
		return name;
	}
	public Site[] getSites() {
		return sites;
	}
	public static List<State> values() {
		return (List<State>) abbreviationToState.values();
	}
	public static List<State> confirmState(String value) {
		if (value.equalsIgnoreCase("all")) {
			return allStates;
		} else {
			List<State> states = new ArrayList<>();
			states.add(getState(value));
			return states;
		}
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
