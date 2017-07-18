package com.mcd.scraper.entities;

import java.util.Calendar;

public class ArrestRecord {
	
	//create a person entity?	
	private long id;
	private String fullName;
	private Calendar arrestDate;
	private int bond;
	private int arrestAge;
	private String gender;
	private String city;
	private String state;
	private String height;
	private int weight;
	private String hairColor;
	private String eyeColor;
	private String[] charges;

	public ArrestRecord(long id, String fullName, Calendar arrestDate, int bond, int arrestAge, String gender,
			String city, String state, String height, int weight, String hairColor, String eyeColor, String[] charges) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.arrestDate = arrestDate;
		this.bond = bond;
		this.arrestAge = arrestAge;
		this.gender = gender;
		this.city = city;
		this.state = state;
		this.height = height;
		this.weight = weight;
		this.hairColor = hairColor;
		this.eyeColor = eyeColor;
		this.charges = charges;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public int getBond() {
		return bond;
	}
	public void setBond(int bond) {
		this.bond = bond;
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
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
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
	public String[] getCharges() {
		return charges;
	}
	public void setCharges(String[] charges) {
		this.charges = charges;
	}
	
	

}
