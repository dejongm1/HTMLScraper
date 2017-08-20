package com.mcd.spider.main.entities.record;

import com.mcd.spider.main.engine.record.ArrestRecordEngine;
import com.mcd.spider.main.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.main.engine.record.various.ArrestsDotOrgEngine;

import java.util.*;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap<>();
	private static Map<String, State> nameToState = new HashMap<>();
	private static List<State> allStates = new ArrayList<>();

	private String name;
	private String abbreviation;
	private List<ArrestRecordEngine> engines;

	public static final State AL = new State("Alabama", "AL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State AK = new State("Alaska", "AK", new ArrayList<>());
	public static final State AZ = new State("Arizona", "AZ", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State AR = new State("Arkansas", "AR", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State CA = new State("California", "CA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State CO = new State("Colorado", "CO", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State CT = new State("Connecticut", "CT", new ArrayList<>());
	public static final State DE = new State("Delaware", "DE", new ArrayList<>());
	public static final State FL = new State("Florida", "FL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State GA = new State("Georgia", "GA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State HI = new State("Hawaii", "HI", new ArrayList<>());
	public static final State ID = new State("Idaho", "ID", new ArrayList<>());
	public static final State IL = new State("Illinois", "IL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State IN = new State("Indiana", "IN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
    public static final State IA = new State("Iowa", "IA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine(), new DesMoinesRegisterComEngine())));
	public static final State KS = new State("Kansas", "KS", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State KY = new State("Kentucky", "KY", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State LA = new State("Louisiana", "LA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State ME = new State("Maine", "ME", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MD = new State("Maryland", "MD", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MA = new State("Massachusetts", "MA", new ArrayList<>());
	public static final State MI = new State("Michigan", "MI", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MN = new State("Minnesota", "MN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MS = new State("Mississippi", "MS", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MO = new State("Missouri", "MO", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State MT = new State("Montana", "MT", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NE = new State("Nebraska", "NE", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NV = new State("Nevada", "NV", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NH = new State("New Hampshire", "NH", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NJ = new State("New Jersey", "NJ", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NM = new State("New Mexico", "NM", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State NY = new State("New York", "NY", new ArrayList<>());
	public static final State NC = new State("North Carolina", "NC", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State ND = new State("North Dakota", "ND", new ArrayList<>());
	public static final State OH = new State("Ohio", "OH", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State OK = new State("Oklahoma", "OK", new ArrayList<>());
	public static final State OR = new State("Oregon", "OR", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State PA = new State("Pennsylvania", "PA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State RI = new State("Rhode Island", "RI", new ArrayList<>());
	public static final State SC = new State("South Carolina", "SC", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State SD = new State("South Dakota", "SD", new ArrayList<>());
	public static final State TN = new State("Tennessee", "TN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State TX = new State("Texas", "TX", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State UT = new State("Utah", "UT", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State VT = new State("Vermont", "VT", new ArrayList<>());
	public static final State VA = new State("Virginia", "VA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State WA = new State("Washington", "WA", new ArrayList<>());
	public static final State WV = new State("West Virginia", "WV", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State WI = new State("Wisconsin", "WI", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State WY = new State("Wyoming", "WY", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())));
	public static final State AS = new State("American Samoa", "AS", new ArrayList<>());
	public static final State DC = new State("District of Columbia", "DC", new ArrayList<>());
	public static final State FM = new State("Federated States of Micronesia", "FM", new ArrayList<>());
	public static final State GU = new State("Guam", "GU", new ArrayList<>());
	public static final State MH = new State("Marshall Islands", "MH", new ArrayList<>());
	public static final State MP = new State("Northern Mariana Islands", "MP", new ArrayList<>());
	public static final State PW = new State("Palau", "PW", new ArrayList<>());
	public static final State PR = new State("Puerto Rico", "PR", new ArrayList<>());
	public static final State VI = new State("Virgin Islands", "VI", new ArrayList<>());


	private State(String name, String abbreviation, List<ArrestRecordEngine> engines) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.engines = engines;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name, this);
		if (!this.getEngines().isEmpty()) {
			allStates.add(this); 
		}
	}
	public void addEngine(ArrestRecordEngine engine) {
		this.getEngines().add(engine);
	}
	public String getName() {
		return this.name;
	}
	public String getAbbreviation() {
		return this.abbreviation;
	}
	public List<ArrestRecordEngine> getEngines() {
		return this.engines;
	}
	public static List<State> values() {
		return (List<State>) abbreviationToState.values();
	}
	public static List<State> confirmState(String value) {
		if (value.equalsIgnoreCase("all")) {
			return allStates;
		} else {
			List<State> states = new ArrayList<>();
			State confirmedState = getState(value);
			if (confirmedState!=null) {
				states.add(confirmedState);
			}
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
