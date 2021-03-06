package com.mcd.spider.entities.record;

import com.google.common.base.CaseFormat;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


public class ArrestRecord implements Record, Comparable<ArrestRecord>{

	public static final Logger logger = Logger.getLogger(ArrestRecord.class);
	public static final String MERGE_SEPARATOR = "---";

	//create a person entity?	
	private String id;
	private String fullName;			
	private String firstName;			
	private String middleName;			
	private String lastName;
	private Date dob;
	private Calendar arrestDate;		
	private Long totalBond;				
	private Integer arrestAge;				
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
	public void setId(String id) {
		this.id = id;
	}

    @Override
	public String getId() {
		return id;
	}

	@Override
    public List<RecordColumnEnum> getColumnEnums() {
        return Arrays.asList(RecordColumnEnum.values());
    }

	public String getFullName() {
		if (fullName==null && firstName!=null) {
	        fullName = firstName;
	        fullName += middleName!=null && !middleName.equals("")?" " + middleName:"";
	        fullName += lastName!=null && !lastName.equals("")?" " + lastName:"";
		}
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
    public Date getDob() {
        return dob;
    }
    public void setDob(Date dob) {
        this.dob = dob;
    }
	public Calendar getArrestDate() {
		return arrestDate;
	}
	public void setArrestDate(Calendar arrestDate) {
		this.arrestDate = arrestDate;
	}
	public Long getTotalBond() {
		return totalBond;
	}
	public void setTotalBond(long totalBond) {
		this.totalBond = totalBond;
	}
	public Integer getArrestAge() {
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
	    for (RecordColumnEnum columnEnum : RecordColumnEnum.values()) {
	        fields.add(columnEnum.getField());
        }
//		List<Field> fields = new ArrayList<>();
//		try {
//			fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
//			for (int f=0;f<fields.size();f++) {
//				if (fields.get(f).getName().contains("logger") || fields.get(f).getName().contains("_COLUMN") || fields.get(f).getName().toLowerCase().contains("compar")) {
//					fields.remove(f);
//				}
//			}
//		} catch (/**NoSuchFieldException |*/ SecurityException e) {
//			e.printStackTrace();
//		}
		return fields;
	}

	@Override
	public CaseFormat getColumnCaseFormat() {
	    return CaseFormat.UPPER_UNDERSCORE;
    }

	@Override
	public int compareTo(ArrestRecord record) {
		Calendar arrestDateToCompare = record.getArrestDate();
		//ascending order
		return this.arrestDate.compareTo(arrestDateToCompare);
	}

	@Override
	public WritableSheet addToExcelSheet(int rowNumber, WritableSheet sheet) throws IllegalAccessException {
		int columnNumber = 0;
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
				} else if (fieldValue instanceof Date) {
                    SimpleDateFormat formatter=new SimpleDateFormat("MMM-dd-yyyy");
                    fieldValueString.append(formatter.format(((Date)field.get(this)).getTime()));
                } else if (fieldValue instanceof Calendar) {
                    SimpleDateFormat formatter=new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
                    fieldValueString.append(formatter.format(((Calendar)field.get(this)).getTime()));
                } else if (fieldValue instanceof Integer || fieldValue instanceof Long) {
					fieldValueString.append(String.valueOf(field.get(this)));
				}
				try {
					Label label = new Label(columnNumber, rowNumber, fieldValueString.toString().toUpperCase());
					sheet.addCell(label);

				} catch (WriteException | NullPointerException e) {
					logger.error("Trouble writing info from " + this.getFullName() + " into row " + rowNumber + ", column " + columnNumber, e);
				}
				columnNumber++;
		}
		return sheet;
	}

	@Override
	public Record merge(Record record) {
		ArrestRecord recordToMerge = (ArrestRecord) record;
		//combine ids to indicate a merged record
		this.id = this.id + MERGE_SEPARATOR + record.getId();
		List<Field> fieldsToMerge = getFieldsToOutput();
		for (Field field : fieldsToMerge) {
			try {
				if (isNotPopulated(field.get(this))) {
					field.set(this, recordToMerge.getClass().getDeclaredField(field.getName()).get(recordToMerge));
				}
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException e) {
				logger.error("Error merging field " + field.getName() + " for records " + this.getId(), e);
			}
		}
		return this;
	}
	
	private boolean isNotPopulated(Object object) {
		//check type of Object before comparing
		if (object==null) {
			return true;
		} else if (object instanceof String) {
			return object.equals("");
		} else if (object instanceof Integer) {
			return (Integer) object==0;
		} else if (object instanceof Long) {
			return (Long) object==0;
		} /*else if (object instanceof Calendar) {
			return object.equals("");
		} */else {
			return false;
		}
	}

//	private String formatNoWhitespace(String originalString) {
//		return originalString!=null?originalString.replaceAll("\\s+", "").trim().toLowerCase():null;
//	}
	
	@Override
	public boolean matches(Record recordToMatch) {
		//ranking system, needs testing
		//score of 6 or higher means a match
		//one or more points for each element matched

		//for height and weight, remove special characters, letters and whitespace
		//for charges remove special characters, whitespace and numbers
		//can we account for people that are arrested multiple times? look for --- in ID ?
        int score = 0;
        ArrestRecord record = (ArrestRecord) recordToMatch;
        try {
            String thisFullName = formatLettersOnly(this.fullName);
            String matchingFullName = formatLettersOnly(record.getFullName());

            String thisFirstName = formatLettersOnly(this.firstName);
            String matchingFirstName = formatLettersOnly(record.getFirstName());

            String thisMiddleName = formatLettersOnly(this.middleName);
            String matchingMiddleName = formatLettersOnly(record.getMiddleName());

            String thisLastName = formatLettersOnly(this.lastName);
            String matchingLastName = formatLettersOnly(record.getLastName());

            String thisCounty = formatLettersOnly(this.county);
            String matchingCounty = formatLettersOnly(record.getCounty());

            String thisEyeColor = formatLettersOnly(this.eyeColor);
            String matchingEyeColor = formatLettersOnly(record.getEyeColor());

            String thisHairColor = formatLettersOnly(this.hairColor);
            String matchingHairColor = formatLettersOnly(record.getHairColor());

            String thisGender = formatLettersOnly(this.gender);
            String matchingGender = formatLettersOnly(record.getGender());

            int thisHeight = convertToInches(this.height);
            int matchingHeight = convertToInches(record.getHeight());

            String thisWeight = formatNumbersOnly(this.weight);
            String matchingWeight = formatNumbersOnly(record.getWeight());

//		int thisArrestAge = this.arrestAge;
//		int matchingArrestAge = record.getArrestAge();
//
//		Calendar thisArrestDate = this.arrestDate;
//		Calendar matchingArrestDate = record.getArrestDate();

            String[] thisCharges = formatArray(this.charges);
            String[] matchingCharges = formatArray(record.getCharges());

            if (neitherIsNull(thisFullName, matchingFullName) && thisFullName.equals(matchingFullName)
                    || (neitherIsNull(thisFirstName, matchingFirstName) && thisFirstName.equals(matchingFirstName)
                    && neitherIsNull(thisLastName, matchingLastName) && thisLastName.equals(matchingLastName))) {
                score += 5;
            }
            if (neitherIsNull(thisMiddleName, matchingMiddleName) && thisMiddleName.equals(matchingMiddleName)) {
                score++;
            }
            if (neitherIsNull(thisCounty,  matchingCounty) && thisCounty.equals(matchingCounty)) {
                score++;
            }
            if (neitherIsNull(thisEyeColor, matchingEyeColor) && thisEyeColor.equals(matchingEyeColor)
                    && neitherIsNull(thisHairColor, matchingHairColor) && thisHairColor.equals(matchingHairColor)) {
                score++;
            }
            if (neitherIsNull(thisGender, matchingGender) && thisGender.equals(matchingGender)) {
                score++;
            }
            if (thisHeight!=0 && matchingHeight!=0 && thisHeight==matchingHeight) {
                score++;
            }
            if (neitherIsNull(thisWeight, matchingWeight) && thisWeight.equals(matchingWeight)) {
                score++;
            }
            if (neitherIsNull(this.getArrestDate(), record.getArrestDate()) && this.getArrestDate().equals(record.getArrestDate())) {
                score++;
            }
            if (neitherIsNull(this.getTotalBond(), record.getTotalBond()) && this.getTotalBond()==(record.getTotalBond())) {
                score++;
            }
            if (thisCharges!=null && matchingCharges!=null) {
                for (String thisCharge : thisCharges) {
                    for (String matchingCharge : matchingCharges) {
                        if (thisCharge.contains(matchingCharge) || matchingCharge.contains(thisCharge)) {
                            score++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught trying to match records " + this.getId() + " and " + record.getId() + ". Considering it a non-match", e);
        }
		return score >= 8;
	}

	protected String formatLettersOnly(String originalString) {
		return originalString!=null?originalString.replaceAll("[^a-zA-Z]", "").trim().toLowerCase():null;
	}

    protected int convertToInches(String heightString) {
	    String numberString = formatNumbersOnly(heightString);
	    if (numberString!=null && !numberString.equals("")) {
            int feet = 0;
            int inches = 0;
            //if starts with 0, get first two digita, else get first digits
            //multiply by 12
            //add to rest of string
            int inchIndex = numberString.startsWith("0")?2:1;
            feet = Integer.parseInt(numberString.substring(0, inchIndex));
            inches = Integer.parseInt(numberString.substring(inchIndex));
            return feet*12 + inches;
        } else {
            return 0;
        }
    }

    protected String formatNumbersOnly(String originalString) {
		return originalString!=null?originalString.replaceAll("[^^0-9]", ""):null;
	}
	
    protected String[] formatArray(String[] stringArray) {
		String[] resultCharges = null;
		if (stringArray!=null && stringArray.length>0) {
			resultCharges = new String[stringArray.length];
			for (int c=0;c<resultCharges.length;c++) {
				resultCharges[c] = formatLettersOnly(stringArray[c]);
			}
		}
		return resultCharges;
	}

	private boolean neitherIsNull(Object first, Object second) {
	    return first!=null && second!=null;
    }
    public static final Comparator<Record> ArrestDateComparator = new Comparator<Record>() {
        @Override
        public int compare(Record record1, Record record2) {
            Calendar recordDate1 = ((ArrestRecord) record1).getArrestDate()!=null?((ArrestRecord) record1).getArrestDate():Calendar.getInstance();
            Calendar recordDate2 = ((ArrestRecord) record2).getArrestDate()!=null?((ArrestRecord) record2).getArrestDate():Calendar.getInstance();
            //ascending order
            int result = recordDate1.compareTo(recordDate2);
            return result;
        }
    };

	public static final Comparator<Record> CountyComparator = new Comparator<Record>() {
		@Override
		public int compare(Record record1, Record record2) {
			String recordCounty1 = ((ArrestRecord) record1).getCounty()!=null?((ArrestRecord) record1).getCounty().toUpperCase():"No County";
			String recordCounty2 = ((ArrestRecord) record2).getCounty()!=null?((ArrestRecord) record2).getCounty().toUpperCase():"No County";
			//ascending order
			int result = recordCounty1.compareTo(recordCounty2);
			return result;
		}
	};
	
	public static final Comparator<Record> CityComparator = new Comparator<Record>() {
		@Override
		public int compare(Record record1, Record record2) {
			String recordCity1 = ((ArrestRecord) record1).getCity()!=null?((ArrestRecord) record1).getCity().toUpperCase():"No City";
			String recordCity2 = ((ArrestRecord) record2).getCity()!=null?((ArrestRecord) record2).getCity().toUpperCase():"No City";
			//ascending order
			return recordCity1.compareTo(recordCity2);
		}
	};
	
    public enum RecordColumnEnum {
        //the column titles should match the fields names (camel case, spaces removed)
        ID_COLUMN(0, "ID", String.class),
        FULLNAME_COLUMN(1, "Full Name", String.class),
        FIRSTNAME_COLUMN(2, "First Name", String.class),
        MIDDLENAME_COLUMN(3, "Middle Name", String.class),
        LASTNAME_COLUMN(4, "Last Name", String.class),
        DOB_COLUMN(5, "DOB", Date.class),
        ARRESTDATE_COLUMN(6, "Arrest Date", Calendar.class),
        TOTALBOND_COLUMN(7, "Total Bond", long.class),
        ARRESTAGE_COLUMN(8, "Arrest Age", int.class),
        GENDER_COLUMN(9, "Gender", String.class),
        CITY_COLUMN(10, "City", String.class),
        STATE_COLUMN(11, "State", String.class),
        COUNTY_COLUMN(12, "County", String.class),
        HEIGHT_COLUMN(13, "Height", String.class),
        WEIGHT_COLUMN(14, "Weight", String.class),
        HAIRCOLOR_COLUMN(15, "Hair Color", String.class),
        EYECOLOR_COLUMN(16, "Eye Color", String.class),
        BIRTHPLACE_COLUMN(17, "Birth Place", String.class),
        CHARGES_COLUMN(18, "Charges", String[].class),
        OFFENDERID_COLUMN(19, "Offender ID", String.class),
        RACE_COLUMN(20, "Race", String.class);

        private int columnIndex;
        private String columnTitle;
        private Field field;
        private String fieldName;
        private String getterName;
        private String setterName;
        private Class type;

        RecordColumnEnum(int columnIndex, String columnTitle, Class type) {
            this.columnIndex = columnIndex;
            this.fieldName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnTitle.toUpperCase().replace(" ", "_"));
            this.columnTitle = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
            try {
                this.field = this.getDeclaringClass().getEnclosingClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException  e) {
                logger.error("Error tying field to enum: "+fieldName);
            }
            this.getterName = "get" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnTitle.replace(" ", "_"));
            this.setterName = "set" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnTitle.replace(" ", "_"));
            this.type = type;
        }

        public int getColumnIndex() {
            return columnIndex;
        }
        public String getColumnTitle() {
            return columnTitle;
        }
        public Field getField() {
            return field;
        }
        public String getFieldName() {
            return fieldName;
        }
        public String getGetterName() {
            return getterName;
        }
        public String getSetterName() {
            return setterName;
        }
        public Class getType() { return type; }
    }

}
