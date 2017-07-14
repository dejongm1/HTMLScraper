package com.mcd.scraper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap();
	private static Map<String, State> nameToState = new HashMap();

	private String abbreviation;
	private String name;

	public static final State IA = new State("IA", "Iowa");

	private State(String abbreviation, String name) {
		this.abbreviation = abbreviation;
		this.name = name;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name, this);
	}
	public String getAbbreviation()
	{
		return abbreviation;
	}
	public String getName()
	{
		return name;
	}
	public static List<State> values() {
		return (List<State>) abbreviationToState.values();
	}
	public static State getState(String value)
	{
		if (value.length()==2) {
			return abbreviationToState.get(value);
		} else {
			return nameToState.get(value);
		}
	}
}
