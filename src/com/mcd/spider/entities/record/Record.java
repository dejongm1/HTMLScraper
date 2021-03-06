package com.mcd.spider.entities.record;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.CaseFormat;
import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.util.SpiderConstants;

import jxl.Sheet;
import jxl.write.WritableSheet;

/**
 * Created by MikeyDizzle on 7/18/2017.
 */
public interface Record {

	Logger logger = Logger.getLogger(ArrestRecord.class);

	String getId();

	void setId(String string);

	List<Field> getFieldsToOutput();

	List<ArrestRecord.RecordColumnEnum> getColumnEnums();

    static <T extends Comparable<? super T>> List<Record> getAsSortedList(Collection<Record> c, Comparator comparator) {
        List<Record> list = new ArrayList<Record>(c);
        if (comparator!=null) {
            java.util.Collections.sort(list, comparator);
        }
        return list;
    }

	static void getValueFromColumn(Object column, Class<?> clazz, Object recordInstance, Sheet sheet, int rowNumber, int c) {
		try {
			String cellContents = sheet.getCell(c, rowNumber).getContents();
			if (!column.equals("EXTRA_COLUMN") && !cellContents.equals("")) {
				Method enumSetter = column.getClass().getMethod("getSetterName");
				String setterName = (String) enumSetter.invoke(column);
				Class<?> fieldType = (Class<?>) column.getClass().getMethod("getType").invoke(column);
				Method fieldSetter = clazz.getMethod(setterName, fieldType);
                if (fieldType.getSimpleName().equalsIgnoreCase(Date.class.getSimpleName())) {
                    DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");
                    try {
                        Date date = formatter.parse(cellContents);
                        fieldSetter.invoke(recordInstance, fieldType.cast(date));
                    } catch (ParseException e) {
                        logger.error("Error parsing date string: "+cellContents);
                        fieldSetter.invoke(recordInstance, fieldType.cast(null));
                    }
                } else if (fieldType.getSimpleName().equalsIgnoreCase(Calendar.class.getSimpleName())) {
					DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
					Calendar calendar = Calendar.getInstance();
					try {
						calendar.setTime(formatter.parse(cellContents));
						fieldSetter.invoke(recordInstance, fieldType.cast(calendar));
					} catch (ParseException e) {
						logger.error("Error parsing date string: "+cellContents);
						fieldSetter.invoke(recordInstance, fieldType.cast(null));
					}
				} else if (fieldType.getSimpleName().equalsIgnoreCase(long.class.getSimpleName())) {
					fieldSetter.invoke(recordInstance, Long.parseLong(cellContents));
				} else if (fieldType.getSimpleName().equalsIgnoreCase(int.class.getSimpleName())) {
					fieldSetter.invoke(recordInstance, Integer.parseInt(cellContents));
				} else if (fieldType.getSimpleName().equalsIgnoreCase(String[].class.getSimpleName())) {
					String[] charges = cellContents.split("; ");
					fieldSetter.invoke(recordInstance, fieldType.cast(charges));
				} else {
					fieldSetter.invoke(recordInstance, fieldType.cast(cellContents));
				}
			}
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			logger.error("Error trying to read cell into record object, column " + c + " row " + rowNumber, e);
		} catch (Exception e) {
			logger.error("Some uhandled exception was caught while trying to parse record at column " + c + " row " + rowNumber, e);
		}
	}

	Record merge(Record record);

	boolean matches(Record record);

	CaseFormat getColumnCaseFormat();

	@SuppressWarnings({ "unchecked" })
	static List<Object> getColumnOrder(Class<?> clazz, Sheet sheet, Object rowRecord) {
		List<Object> columnEnums = new ArrayList<>();
		for (int c=0;c<sheet.getColumns();c++) {
			String labelContents = sheet.getCell(c, 0).getContents().replaceAll("\\s+", "");
			try {
				boolean matchingColumnFound = false;
				for (Object currentEnum : (List<Object>) clazz.getMethod("getColumnEnums").invoke(rowRecord)) {
					if (!matchingColumnFound) {
						String fieldTitle = (String)currentEnum.getClass().getMethod("getFieldName").invoke(currentEnum);
						String columnTitle = (String)currentEnum.getClass().getMethod("getColumnTitle").invoke(currentEnum);
						if (columnTitle.equalsIgnoreCase(labelContents) || fieldTitle.equalsIgnoreCase(labelContents)) {
							columnEnums.add(currentEnum);
							matchingColumnFound = true;
						}
					}
				}
				if (!matchingColumnFound) {
					columnEnums.add("EXTRA_COLUMN");
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				logger.error("Excpetion trying to get order of columns from sheet " + sheet.getName(), e);
			}
		}
		return columnEnums;
	}

	static Record readRowIntoRecord(Class<?> clazz, Sheet sheet, Object recordInstance, int rowNumber, List<Object> columnOrder) {
		if (columnOrder==null) {
			columnOrder = getColumnOrder(clazz, sheet, recordInstance);
		}
		int c = 0;
		for (Object column : columnOrder) {
			getValueFromColumn(column, clazz, recordInstance, sheet, rowNumber, c);
			c++;
		}
		return (Record) recordInstance;
	}

    WritableSheet addToExcelSheet(int rowNumber, WritableSheet sheet) throws IllegalAccessException;

	static <T> RecordWorkbook splitByField(List<Record> records, String fieldName, Class<T> clazz) {
		RecordWorkbook recordBook = new RecordWorkbook();
		RecordSheet allRecordSheet = new RecordSheet(SpiderConstants.MAIN_SHEET_NAME, records);
		recordBook.addSheet(allRecordSheet);
		Method fieldGetter = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equalsIgnoreCase("get" + fieldName)) {
				fieldGetter = method;
			}
		}
		Object groupingDelimiter = null;
		RecordSheet delimiterSheet = new RecordSheet(fieldGetter);
		for (Record record : records) {
			try {
				Object delimiterValue = fieldGetter.invoke(record)==null?"":fieldGetter.invoke(record);
				if (groupingDelimiter!=null && delimiterValue.equals(groupingDelimiter)) {
					delimiterSheet.addRecord(record);
				} else {
					if (!delimiterSheet.isEmpty()) {
						recordBook.addSheet(delimiterSheet);
					}
					groupingDelimiter = delimiterValue;
					delimiterSheet = new RecordSheet(fieldGetter);
					delimiterSheet.addRecord(record);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException e) {
				logger.error("Problem while trying to split records by specified field: " + fieldName, e);
			}
		}
		recordBook.addSheet(delimiterSheet);

		return recordBook;
	}

	static Class<?> getRecordClass(Record record) {
		try {
			return Class.forName(record.getClass().getCanonicalName());
		} catch (ClassNotFoundException e) {
			logger.error("Error trying to get record class", e);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Constructor<?> getConstructorForRecord(Class<?> clazz, Record record) {
		try {
			logger.debug("Record type determined as " + record.getClass().getSimpleName());
			return clazz.getConstructor();
		} catch (NoSuchMethodException e) {
			logger.error("Error trying to get record constructor", e);
		}
		return null;
	}

}
