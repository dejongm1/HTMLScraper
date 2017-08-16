package com.mcd.spider.main.entities.record;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import jxl.Cell;
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

    WritableSheet addToExcelSheet(int rowNumber, WritableSheet sheet) throws IllegalAccessException;
    
    Set<Record> merge(Record record);
    
    boolean matches(Record record);
    
    static <T> List<List<Record>> splitByField(List<Record> records, String fieldName, Class<T> clazz) {
		List<List<Record>> recordListList = new ArrayList<>();
		Method fieldGetter = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equalsIgnoreCase("get" + fieldName.replace(" ", ""))) {
				fieldGetter = method;
			}
		}
		Object previousDelimiter = null;
		List<Record> delimiterList = new ArrayList<>();
		for (Record record : records) {
			try {
				Object delimiterValue = fieldGetter.invoke(record);
				if (previousDelimiter == null && delimiterValue!= null) {
					previousDelimiter = delimiterValue;
				}
				if (previousDelimiter!=null && delimiterValue!= null && delimiterValue.equals(previousDelimiter)) {
					delimiterList.add(record);
				} else {
					recordListList.add(delimiterList);
					previousDelimiter = fieldGetter.invoke(record);
					delimiterList = new ArrayList<>();
					delimiterList.add(record);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException e) {
				logger.error("Problem while trying to split records by specified field: " + fieldName, e);
			}
		}
		recordListList.add(delimiterList);
    	
    	return recordListList;
    }
    
    static Class getRecordClass(Record record) {
    	try {
			return Class.forName(record.getClass().getCanonicalName());
		} catch (ClassNotFoundException e) {
			logger.error("Error trying to get record class", e);
		}
    	return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static Constructor<?> getConstructorForRecord(Class clazz, Record record) {
    	try {
    		logger.debug("Record type determined as " + record.getClass().getSimpleName());
			return clazz.getConstructor();
    	} catch (NoSuchMethodException e) {
			logger.error("Error trying to get record constructor", e);
		}
    	return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static Record readRowIntoRecord(Class clazz, Sheet mainSheet, Object rowRecord, int r, int numberOfColumns) {
    	int c = 0;
    	try {
		//for (int c=0;c<numberOfColumns;c++) {
	    	for (Object currentEnum : (List<Object>) clazz.getMethod("getColumnEnums").invoke(rowRecord)) {
				try {
	//	    		List<Object> enums = (List<Object>) clazz.getMethod("getColumnEnums").invoke(rowRecord);
	//				Object currentEnum = enums.get(c);
		    		String cellContents = mainSheet.getCell(c, r).getContents();
		    		if (!cellContents.equals("") && ((String)currentEnum.getClass().getMethod("getColumnTitle").invoke(currentEnum)).equals(mainSheet.getCell(c, 0).getContents())) {
		    			Method enumSetter = currentEnum.getClass().getMethod("getSetterName");
		    			String setterName = (String) enumSetter.invoke(currentEnum);
		    			Class fieldType = (Class) currentEnum.getClass().getMethod("getType").invoke(currentEnum);
		    			Method fieldSetter = clazz.getMethod(setterName, fieldType);
		    			if (fieldType.getSimpleName().equalsIgnoreCase(Calendar.class.getSimpleName())) {
		    				DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
		    				Calendar calendar = Calendar.getInstance();
		    				try {
		    					calendar.setTime(formatter.parse(cellContents));
		    				} catch (ParseException e) {
		    					logger.error("Error parsing date string: " + cellContents, e);
		    				}
		    				fieldSetter.invoke(rowRecord, fieldType.cast(calendar));
		    			} else if (fieldType.getSimpleName().equalsIgnoreCase(long.class.getSimpleName())) {
		    				fieldSetter.invoke(rowRecord, Long.parseLong(cellContents));
		    			} else if (fieldType.getSimpleName().equalsIgnoreCase(int.class.getSimpleName())) {
		    				fieldSetter.invoke(rowRecord, Integer.parseInt(cellContents));
		    			} else if (fieldType.getSimpleName().equalsIgnoreCase(String[].class.getSimpleName())) {
		    				String[] charges = cellContents.split("; ");
		    				fieldSetter.invoke(rowRecord, fieldType.cast(charges));
		    			} else {
		    				fieldSetter.invoke(rowRecord, fieldType.cast(cellContents));
		    			}
		    			c++;
		    		}
		    	} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
		    		logger.error("Error trying to read cell into record object, column " + c + " row " + r, e);
		    	} catch (Exception e) {
		    		logger.error("Some uhandled exception was cuaght while trying to parse record at column " + c + " row " + r, e);
		    	}
			}
    	} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
    		logger.error("Error getting list of record enums", e);
    	}
    	return (Record) rowRecord;
    }
}
