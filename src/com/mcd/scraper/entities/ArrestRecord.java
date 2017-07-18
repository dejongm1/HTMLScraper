package com.mcd.scraper.entities;

import java.util.Calendar;

public class ArrestRecord {
	
	//create a person entity?	
	private String id;
	private String fullName;
	private Calendar arrestDate;
	private int totalBond;
	private int arrestAge;
	private String gender;
	private String city;
	private String state;
	private String county;
	private String height;
	private String weight;
	private String hairColor;
	private String eyeColor;
	private String birthPlace;
	private String[] charges;

	public ArrestRecord(){};
	
	public ArrestRecord(String id, String fullName, Calendar arrestDate, int totalBond, int arrestAge, String gender,
			String city, String state, String county, String height, String weight, String hairColor, String eyeColor, String birthPlace, String[] charges) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.arrestDate = arrestDate;
		this.totalBond = totalBond;
		this.arrestAge = arrestAge;
		this.gender = gender;
		this.city = city;
		this.state = state;
		this.county = county;
		this.height = height;
		this.weight = weight;
		this.hairColor = hairColor;
		this.eyeColor = eyeColor;
		this.birthPlace = birthPlace;
		this.charges = charges;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String string) {
		this.id = string;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Calendar getArrestDate() {
		return arrestDate;
	}
	public void setArrestDate(Calendar arrestDate) {
		this.arrestDate = arrestDate;
	}
	public int getTotalBond() {
		return totalBond;
	}
	public void setTotalBond(int totalBond) {
		this.totalBond = totalBond;
	}
	public int getArrestAge() {
		return arrestAge;
	}
	public void setArrestAge(int arrestAge) {
		this.arrestAge = arrestAge;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getHairColor() {
		return hairColor;
	}
	public void setHairColor(String hairColor) {
		this.hairColor = hairColor;
	}
	public String getEyeColor() {
		return eyeColor;
	}
	public void setEyeColor(String eyeColor) {
		this.eyeColor = eyeColor;
	}
	public String getBirthPlace() {
		return birthPlace;
	}
	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}
	public String[] getCharges() {
		return charges;
	}
	public void setCharges(String[] charges) {
		this.charges = charges;
	}
	
	

}
