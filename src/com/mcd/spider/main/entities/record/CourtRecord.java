package com.mcd.spider.main.entities.record;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class CourtRecord implements Record {

	public enum RecordColumnEnum {
		ID_COLUMN(0, "ID"),
		FULLNAME_COLUMN(1, "Full Name");

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

	public static final Logger logger = Logger.getLogger(CourtRecord.class);

	//create a person entity?
	private String id;
	private String fullName;

	public CourtRecord(){}

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
				} else if (fieldValue instanceof Calendar) {
					SimpleDateFormat formatter=new SimpleDateFormat("MMM-dd-yyyy hh:mm a"); 
					fieldValueString.append(formatter.format(((Calendar)field.get(this)).getTime()));
				} else if (fieldValue instanceof Integer) {
					fieldValueString.append(String.valueOf(field.get(this)));
				}
				try {
					Label label = new Label(columnNumber, rowNumber, fieldValueString.toString());
					sheet.addCell(label);

				} catch (WriteException | NullPointerException e) {
					logger.error("Trouble writing info from " + this.getFullName() + " into row " + rowNumber + ", column " + columnNumber, e);
				}
				columnNumber++;
		}
		return sheet;
	}
//	
//	public outputAsText() {
//	
//	}
}
