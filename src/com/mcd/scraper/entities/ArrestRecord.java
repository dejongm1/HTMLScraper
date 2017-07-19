package com.mcd.scraper.entities;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ArrestRecord implements Record {

	public static final Logger logger = Logger.getLogger(ArrestRecord.class);

	//create a person entity?	
	private String id;
	private String fullName;
	private String firstName;
	private String middleName;
	private String lastName;
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

	public ArrestRecord(){}

    @Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String string) {
		this.id = string;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
	
	@Override
	public List<Field> getFieldsToOutput() {
		List<Field> fields = new ArrayList<>();
		try {
			fields.add(this.getClass().getDeclaredField("id"));
			fields.add(this.getClass().getDeclaredField("fullName"));
			fields.add(this.getClass().getDeclaredField("gender"));
			fields.add(this.getClass().getDeclaredField("height"));
			fields.add(this.getClass().getDeclaredField("weight"));
			fields.add(this.getClass().getDeclaredField("hairColor"));
			fields.add(this.getClass().getDeclaredField("eyeColor"));
			fields.add(this.getClass().getDeclaredField("birthPlace"));
			fields.add(this.getClass().getDeclaredField("city"));
			fields.add(this.getClass().getDeclaredField("county"));
			fields.add(this.getClass().getDeclaredField("charges"));
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return fields;	
	}

	@Override
	public WritableSheet addToExcelSheet(WritableSheet worksheet, int rowNumber) throws IllegalAccessException {
		int columnNumber = 0;
		for (Field field : getFieldsToOutput()) {
			Label label = new Label(columnNumber, rowNumber, field.get(this).toString());
			try {
				worksheet.addCell(label);
			} catch (WriteException e) {
				logger.error("Trouble writing info from " + this.getFullName() + " into row " + rowNumber + ", column " + columnNumber);
			}
		}
		return worksheet;
	}
//	
//	public outputAsText() {
//	
//	}

}
