package com.main.mcd.spider.entities;

import com.main.mcd.spider.entities.site.ArrestsDotOrgSite;
import com.main.mcd.spider.entities.site.PolkCountyIowaGovSite;
import com.main.mcd.spider.entities.site.Site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class State {
	private static Map<String, State> abbreviationToState = new HashMap();
	private static Map<String, State> nameToState = new HashMap();
	private static List<State> allStates = new ArrayList<>();

	private String name;
	private String abbreviation;
	private Site[] sites;

	public static final State AL = new State("Alabama", "AL", new Site[]{});
	public static final State AK = new State("Alaska", "AK", new Site[]{});
	public static final State AZ = new State("Arizona", "AZ", new Site[]{});
	public static final State AR = new State("Arkansas", "AR", new Site[]{});
	public static final State CA = new State("California", "CA", new Site[]{});
	public static final State CO = new State("Colorado", "CO", new Site[]{});
	public static final State CT = new State("Connecticut", "CT", new Site[]{});
	public static final State DE = new State("Delaware", "DE", new Site[]{});
	public static final State FL = new State("Florida", "FL", new Site[]{});
	public static final State GA = new State("Georgia", "GA", new Site[]{});
	public static final State HI = new State("Hawaii", "HI", new Site[]{});
	public static final State ID = new State("Idaho", "ID", new Site[]{});
	public static final State IL = new State("Illinois", "IL", new Site[]{});
	public static final State IN = new State("Indiana", "IN", new Site[]{});
	public static final State IA = new State("Iowa", "IA", new Site[]{new PolkCountyIowaGovSite()});
    //public static final State IA = new State("Iowa", "IA", new Site[]{new ArrestsDotOrgSite()});
	public static final State KS = new State("Kansas", "KS", new Site[]{});
	public static final State KY = new State("Kentucky", "KY", new Site[]{});
	public static final State LA = new State("Louisiana", "LA", new Site[]{});
	public static final State ME = new State("Maine", "ME", new Site[]{});
	public static final State MD = new State("Maryland", "MD", new Site[]{});
	public static final State MA = new State("Massachusetts", "MA", new Site[]{});
	public static final State MI = new State("Michigan", "MI", new Site[]{});
	public static final State MN = new State("Minnesota", "MN", new Site[]{});
	public static final State MS = new State("Mississippi", "MS", new Site[]{});
	public static final State MO = new State("Missouri", "MO", new Site[]{});
	public static final State MT = new State("Montana", "MT", new Site[]{});
	public static final State NE = new State("Nebraska", "NE", new Site[]{});
	public static final State NV = new State("Nevada", "NV", new Site[]{});
	public static final State NH = new State("New Hampshire", "NH", new Site[]{});
	public static final State NJ = new State("New Jersey", "NJ", new Site[]{});
	public static final State NM = new State("New Mexico", "NM", new Site[]{});
	public static final State NY = new State("New York", "NY", new Site[]{});
	public static final State NC = new State("North Carolina", "NC", new Site[]{});
	public static final State ND = new State("North Dakota", "ND", new Site[]{});
	public static final State OH = new State("Ohio", "OH", new Site[]{});
	public static final State OK = new State("Oklahoma", "OK", new Site[]{});
	public static final State OR = new State("Oregon", "OR", new Site[]{});
	public static final State PA = new State("Pennsylvania", "PA", new Site[]{});
	public static final State RI = new State("Rhode Island", "RI", new Site[]{});
	public static final State SC = new State("South Carolina", "SC", new Site[]{});
	public static final State SD = new State("South Dakota", "SD", new Site[]{});
	public static final State TN = new State("Tennessee", "TN", new Site[]{});
	public static final State TX = new State("Texas", "TX", new Site[]{});
	public static final State UT = new State("Utah", "UT", new Site[]{});
	public static final State VT = new State("Vermont", "VT", new Site[]{});
	public static final State VA = new State("Virginia", "VA", new Site[]{});
	public static final State WA = new State("Washington", "WA", new Site[]{});
	public static final State WV = new State("West Virginia", "WV", new Site[]{});
	public static final State WI = new State("Wisconsin", "WI", new Site[]{});
	public static final State WY = new State("Wyoming", "WY", new Site[]{});
	public static final State AS = new State("American Samoa", "AS", new Site[]{});
	public static final State DC = new State("District of Columbia", "DC", new Site[]{});
	public static final State FM = new State("Federated States of Micronesia", "FM", new Site[]{});
	public static final State GU = new State("Guam", "GU", new Site[]{});
	public static final State MH = new State("Marshall Islands", "MH", new Site[]{});
	public static final State MP = new State("Northern Mariana Islands", "MP", new Site[]{});
	public static final State PW = new State("Palau", "PW", new Site[]{});
	public static final State PR = new State("Puerto Rico", "PR", new Site[]{});
	public static final State VI = new State("Virgin Islands", "VI", new Site[]{});


	private State(String name, String abbreviation, Site[] sites) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.sites = sites;
		abbreviationToState.put(abbreviation, this);
		nameToState.put(name, this);
		if (this.getSites().length>0) {
			allStates.add(this); 
		}
	}
	public String getName() {
		return name;
	}
	public String getAbbreviation() {
		return abbreviation;
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
