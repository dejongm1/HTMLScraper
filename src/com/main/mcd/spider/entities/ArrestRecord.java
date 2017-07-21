package com.main.mcd.spider.entities;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.main.mcd.spider.entities.ArrestRecord.RecordColumnEnum;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class ArrestRecord implements Record {
	
	public enum RecordColumnEnum {
		ID_COLUMN(0, "ID"),
		FULLNAME_COLUMN(1, "Full Name"),
		FIRSTNAME_COLUMN(2, "First Name"),
		MIDDLENAME_COLUMN(3, "Middle Name"),
		LASTNAME_COLUMN(4, "Last Name"),
		ARRESTDATE_COLUMN(5, "Arrest Date"),
		TOTALBOND_COLUMN(6, "Total Bond"),
		ARRESTAGE_COLUMN(7, "Arrest Age"),
		GENDER_COLUMN(8, "Gender"),
		CITY_COLUMN(9, "City"),
		STATE_COLUMN(10, "State"),
		COUNTY_COLUMN(11, "County"),
		HEIGHT_COLUMN(12, "Height"),
		WEIGHT_COLUMN(13, "Weight"),
		HAIRCOLOR_COLUMN(14, "Hair Color"),
		EYECOLOR_COLUMN(15, "Eye Color"),
		BIRTHPLACE_COLUMN(16, "Birth Place"),
		CHARGES_COLUMN(17, "Charges"),
		OFFENDERID_COLUMN(18, "Offender ID"),
		RACE(19, "Race");
	
		private int columnIndex;
		private String columnTitle;
		
		RecordColumnEnum(int columnIndex, String columnTitle) {
			this.columnIndex = columnIndex;
			this.columnTitle = columnTitle;
		}
		public int index() {
			return columnIndex;
		}
		public String title() {
			return columnTitle;
		}
		
	}

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
	private String offenderId;
	private String race;

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
	public String getOffenderId() {
		return offenderId;
	}
	public void setOffenderId(String offenderId) {
		this.offenderId = offenderId;
	}
	
	public String getRace() {
		return race;
	}

	public void setRace(String race) {
		this.race = race;
	}

	@Override
	public List<Field> getFieldsToOutput() {
		List<Field> fields = new ArrayList<>();
		try {
			fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
			for (int f=0;f<fields.size();f++) {
				if (fields.get(f).getName().contains("logger") || fields.get(f).getName().contains("_COLUMN")) {
					fields.remove(f);
				}
			}
//			fields.add(this.getClass().getDeclaredField("id"));
//			fields.add(this.getClass().getDeclaredField("fullName"));
//			fields.add(this.getClass().getDeclaredField("gender"));
//			fields.add(this.getClass().getDeclaredField("height"));
//			fields.add(this.getClass().getDeclaredField("weight"));
//			fields.add(this.getClass().getDeclaredField("hairColor"));
//			fields.add(this.getClass().getDeclaredField("eyeColor"));
//			fields.add(this.getClass().getDeclaredField("birthPlace"));
//			fields.add(this.getClass().getDeclaredField("city"));
//			fields.add(this.getClass().getDeclaredField("county"));
//			fields.add(this.getClass().getDeclaredField("arrestDate"));
//			fields.add(this.getClass().getDeclaredField("charges"));
		} catch (/**NoSuchFieldException |*/ SecurityException e) {
			e.printStackTrace();
		}
		return fields;	
	}

	@Override
	public WritableSheet addToExcelSheet(WritableWorkbook workbook, int rowNumber) throws IllegalAccessException {
		int columnNumber = 0;
		WritableSheet worksheet = workbook.getSheet(0);
		for (Field field : getFieldsToOutput()) {
			Object fieldValue = field.get(this);
			StringBuilder fieldValueString = new StringBuilder();
				if (fieldValue instanceof String) {
					fieldValueString.append((String) field.get(this));
				} else if (fieldValue instanceof String[]) {
					for (String stringItem : (String[]) field.get(this)) {
						fieldValueString.append(stringItem + "; " );
						//fieldValueString.append(stringItem + "\n" );
					}
				} else if (fieldValue instanceof Calendar) {
					SimpleDateFormat formatter=new SimpleDateFormat("DD-MMM-yyyy hh:mm a"); 
					fieldValueString.append(formatter.format(((Calendar)field.get(this)).getTime()));
				} else if (fieldValue instanceof Integer) {
					fieldValueString.append(String.valueOf(field.get(this)));
				}
				try {
					Label label = new Label(columnNumber, rowNumber, fieldValueString.toString());
					worksheet.addCell(label);

				} catch (WriteException we) {
					logger.error("Trouble writing info from " + this.getFullName() + " into row " + rowNumber + ", column " + columnNumber, we);
				} catch (NullPointerException npe) {
					logger.error("Trouble writing info from " + this.getFullName() + " into row " + rowNumber + ", column " + columnNumber, npe);
				}
				columnNumber++;
		}
		return worksheet;
	}
//	
//	public outputAsText() {
//	
//	}
}
