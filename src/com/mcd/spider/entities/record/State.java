package com.mcd.spider.entities.record;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.engine.record.various.ArrestsDotOrgEngine;

import java.util.*;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap<>();
	private static Map<String, State> nameToState = new HashMap<>();
	private static List<State> allStates = new ArrayList<>();

	private String name;
	private String abbreviation;
	private List<ArrestRecordEngine> engines;
	public static final State AL = new State("ALABAMA", "AL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State AK = new State("ALASKA", "AK", new ArrayList<>(), false);
	public static final State AZ = new State("ARIZONA", "AZ", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State AR = new State("ARKANSAS", "AR", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State CA = new State("CALIFORNIA", "CA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State CO = new State("COLORADO", "CO", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State CT = new State("CONNECTICUT", "CT", new ArrayList<>(), false);
	public static final State DE = new State("DELAWARE", "DE", new ArrayList<>(), false);
	public static final State FL = new State("FLORIDA", "FL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State GA = new State("GEORGIA", "GA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State HI = new State("HAWAII", "HI", new ArrayList<>(), false);
	public static final State ID = new State("IDAHO", "ID", new ArrayList<>(), false);
	public static final State IL = new State("ILLINOIS", "IL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State IN = new State("INDIANA", "IN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
    public static final State IA = new State("IOWA", "IA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine(), new DesMoinesRegisterComEngine())), false);
	public static final State KS = new State("KANSAS", "KS", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State KY = new State("KENTUCKY", "KY", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State LA = new State("LOUISIANA", "LA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State ME = new State("MAINE", "ME", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MD = new State("MARYLAND", "MD", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MA = new State("MASSACHUSETTS", "MA", new ArrayList<>(), false);
	public static final State MI = new State("MICHIGAN", "MI", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MN = new State("MINNESOTA", "MN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MS = new State("MISSISSIPPI", "MS", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MO = new State("MISSOURI", "MO", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State MT = new State("MONTANA", "MT", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NE = new State("NEBRASKA", "NE", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NV = new State("NEVADA", "NV", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NH = new State("NEW HAMPSHIRE", "NH", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NJ = new State("NEW JERSEY", "NJ", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NM = new State("NEW MEXICO", "NM", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State NY = new State("NEW YORK", "NY", new ArrayList<>(), false);
	public static final State NC = new State("NORTH CAROLINA", "NC", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State ND = new State("NORTH DAKOTA", "ND", new ArrayList<>(), false);
	public static final State OH = new State("OHIO", "OH", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State OK = new State("OKLAHOMA", "OK", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), true);
	public static final State OR = new State("OREGON", "OR", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State PA = new State("PENNSYLVANIA", "PA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State RI = new State("RHODE ISLAND", "RI", new ArrayList<>(), false);
	public static final State SC = new State("SOUTH CAROLINA", "SC", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State SD = new State("SOUTH DAKOTA", "SD", new ArrayList<>(), false);
	public static final State TN = new State("TENNESSEE", "TN", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State TX = new State("TEXAS", "TX", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State UT = new State("UTAH", "UT", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State VT = new State("VERMONT", "VT", new ArrayList<>(), false);
	public static final State VA = new State("VIRGINIA", "VA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State WA = new State("WASHINGTON", "WA", new ArrayList<>(), false);
	public static final State WV = new State("WEST VIRGINIA", "WV", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State WI = new State("WISONSIN", "WI", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State WY = new State("WYOMING", "WY", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine())), false);
	public static final State AS = new State("AMERICAN SAMOA", "AS", new ArrayList<>(), false);
	public static final State DC = new State("DISTRICT OF COLUMBIA", "DC", new ArrayList<>(), false);
	public static final State FM = new State("FEDERATED STATES OF MICRONESIA", "FM", new ArrayList<>(), false);
	public static final State GU = new State("GUAM", "GU", new ArrayList<>(), false);
	public static final State MH = new State("MARSHALL ISLANDS", "MH", new ArrayList<>(), false);
	public static final State MP = new State("NORTHERN MAIRAN ISLANDS", "MP", new ArrayList<>(), false);
	public static final State PW = new State("PALAU", "PW", new ArrayList<>(), false);
	public static final State PR = new State("PUERTO RICO", "PR", new ArrayList<>(), false);
	public static final State VI = new State("VIRGIN ISLANDS", "VI", new ArrayList<>(), false);
	private boolean meetsLexisNexisCriteria;


	private State(String name, String abbreviation, List<ArrestRecordEngine> engines, boolean meetsCriteria) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.engines = engines;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name.toUpperCase(), this);
		if (!this.getEngines().isEmpty()) {
			allStates.add(this); 
		}
		this.meetsLexisNexisCriteria = meetsCriteria;
	}
	public void addEngine(ArrestRecordEngine engine) {
		this.getEngines().add(engine);
	}
	public String getName() {
		return this.name;
	}
	public List<ArrestRecordEngine> getEngines() {
		return this.engines;
	}
	public boolean meetsLexisNexisCriteria() { return this.meetsLexisNexisCriteria; }
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
			return abbreviationToState.get(value.toUpperCase());
		} else {
			return nameToState.get(value.toUpperCase());
		}
	}
}
