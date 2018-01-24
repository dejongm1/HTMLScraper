package com.mcd.spider.entities.record;

import com.mcd.spider.engine.record.ArrestRecordEngine;
import com.mcd.spider.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.engine.record.various.ArrestsDotOrgEngine;
import com.mcd.spider.entities.site.SpiderWeb;

import java.util.*;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap<>();
	private static Map<String, State> nameToState = new HashMap<>();
	private static List<State> allStates = new ArrayList<>();

	private String name;
	private String abbreviation;
	private List<ArrestRecordEngine> engines;
	private boolean meetsLexisNexisCriteria;
	
	public final static State AL = new State("ALABAMA", "AL", new ArrayList<>(), false);
	public final static State AK = new State("ALASKA", "AK", new ArrayList<>(), false);
	public final static State AZ = new State("ARIZONA", "AZ", new ArrayList<>(), false);
	public final static State AR = new State("ARKANSAS", "AR", new ArrayList<>(), false);
	public final static State CA = new State("CALIFORNIA", "CA", new ArrayList<>(), false);
	public final static State CO = new State("COLORADO", "CO", new ArrayList<>(), false);
	public final static State CT = new State("CONNECTICUT", "CT", new ArrayList<>(), false);
	public final static State DE = new State("DELAWARE", "DE", new ArrayList<>(), false);
	public final static State FL = new State("FLORIDA", "FL", new ArrayList<>(), false);
	public final static State GA = new State("GEORGIA", "GA", new ArrayList<>(), false);
	public final static State HI = new State("HAWAII", "HI", new ArrayList<>(), false);
	public final static State ID = new State("IDAHO", "ID", new ArrayList<>(), false);
	public final static State IL = new State("ILLINOIS", "IL", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine("illinois"))), false);
	public final static State IN = new State("INDIANA", "IN", new ArrayList<>(), false);
    public final static State IA = new State("IOWA", "IA", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine("iowa"), new DesMoinesRegisterComEngine())), false);
	public final static State KS = new State("KANSAS", "KS", new ArrayList<>(), false);
	public final static State KY = new State("KENTUCKY", "KY", new ArrayList<>(), false);
	public final static State LA = new State("LOUISIANA", "LA", new ArrayList<>(), false);
	public final static State ME = new State("MAINE", "ME", new ArrayList<>(), false);
	public final static State MD = new State("MARYLAND", "MD", new ArrayList<>(), false);
	public final static State MA = new State("MASSACHUSETTS", "MA", new ArrayList<>(), false);
	public final static State MI = new State("MICHIGAN", "MI", new ArrayList<>(), false);
	public final static State MN = new State("MINNESOTA", "MN", new ArrayList<>(), false);
	public final static State MS = new State("MISSISSIPPI", "MS", new ArrayList<>(), false);
	public final static State MO = new State("MISSOURI", "MO", new ArrayList<>(), false);
	public final static State MT = new State("MONTANA", "MT", new ArrayList<>(), false);
	public final static State NE = new State("NEBRASKA", "NE", new ArrayList<>(), false);
	public final static State NV = new State("NEVADA", "NV", new ArrayList<>(), false);
	public final static State NH = new State("NEW HAMPSHIRE", "NH", new ArrayList<>(), false);
	public final static State NJ = new State("NEW JERSEY", "NJ", new ArrayList<>(), false);
	public final static State NM = new State("NEW MEXICO", "NM", new ArrayList<>(), false);
	public final static State NY = new State("NEW YORK", "NY", new ArrayList<>(), false);
	public final static State NC = new State("NORTH CAROLINA", "NC", new ArrayList<>(), false);
	public final static State ND = new State("NORTH DAKOTA", "ND", new ArrayList<>(), false);
	public final static State OH = new State("OHIO", "OH", new ArrayList<>(), false);
	public final static State OK = new State("OKLAHOMA", "OK", new ArrayList<>(Arrays.asList(new ArrestsDotOrgEngine("oklahoma"))), true);
	public final static State OR = new State("OREGON", "OR", new ArrayList<>(), false);
	public final static State PA = new State("PENNSYLVANIA", "PA", new ArrayList<>(), false);
	public final static State RI = new State("RHODE ISLAND", "RI", new ArrayList<>(), false);
	public final static State SC = new State("SOUTH CAROLINA", "SC", new ArrayList<>(), false);
	public final static State SD = new State("SOUTH DAKOTA", "SD", new ArrayList<>(), false);
	public final static State TN = new State("TENNESSEE", "TN", new ArrayList<>(), false);
	public final static State TX = new State("TEXAS", "TX", new ArrayList<>(), false);
	public final static State UT = new State("UTAH", "UT", new ArrayList<>(), false);
	public final static State VT = new State("VERMONT", "VT", new ArrayList<>(), false);
	public final static State VA = new State("VIRGINIA", "VA", new ArrayList<>(), false);
	public final static State WA = new State("WASHINGTON", "WA", new ArrayList<>(), false);
	public final static State WV = new State("WEST VIRGINIA", "WV", new ArrayList<>(), false);
	public final static State WI = new State("WISONSIN", "WI", new ArrayList<>(), false);
	public final static State WY = new State("WYOMING", "WY", new ArrayList<>(), false);
	public final static State AS = new State("AMERICAN SAMOA", "AS", new ArrayList<>(), false);
	public final static State DC = new State("DISTRICT OF COLUMBIA", "DC", new ArrayList<>(), false);
	public final static State FM = new State("FEDERATED STATES OF MICRONESIA", "FM", new ArrayList<>(), false);
	public final static State GU = new State("GUAM", "GU", new ArrayList<>(), false);
	public final static State MH = new State("MARSHALL ISLANDS", "MH", new ArrayList<>(), false);
	public final static State MP = new State("NORTHERN MAIRAN ISLANDS", "MP", new ArrayList<>(), false);
	public final static State PW = new State("PALAU", "PW", new ArrayList<>(), false);
	public final static State PR = new State("PUERTO RICO", "PR", new ArrayList<>(), false);
	public final static State VI = new State("VIRGIN ISLANDS", "VI", new ArrayList<>(), false);


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
	public String getAbbreviation() {
		return this.abbreviation;
	}
	public List<ArrestRecordEngine> getEngines() {
		return this.engines;
	}	
	public void setEngines(List<ArrestRecordEngine> engines) {
		this.engines = engines;
	}
	public boolean meetsLexisNexisCriteria() { 
		return this.meetsLexisNexisCriteria; 
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
		//TODO build state with non-static values here?
		//way to ignore case here?
		if (value.length()==2) {
			return abbreviationToState.get(value.toUpperCase());
		} else {
			return nameToState.get(value.toUpperCase());
		}
	}
	public void primeStateEngines(SpiderWeb web) {
		for (ArrestRecordEngine engine : this.getEngines()) {
		    //give each engine it's own web
            SpiderWeb engineWeb = new SpiderWeb(web.getMaxNumberOfResults(), web.getMisc(), web.retrieveMissedRecords(), web.getFilter(), this);
			engine.setSpiderWeb(engineWeb);
		}
	}
}
