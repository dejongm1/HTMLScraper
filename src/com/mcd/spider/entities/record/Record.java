package com.mcd.spider.entities.record;

import com.google.common.base.CaseFormat;
import jxl.Sheet;
import jxl.write.WritableSheet;
import org.apache.log4j.Logger;

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
    
    Record merge(Record record);
    
    boolean matches(Record record);

    CaseFormat getColumnCaseFormat();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static Record readRowIntoRecord(Class clazz, Sheet sheet, Object rowRecord, int r) {
    	int c = 0;
    	try {
	    	for (Object currentEnum : (List<Object>) clazz.getMethod("getColumnEnums").invoke(rowRecord)) {
				try {
		    		String cellContents = sheet.getCell(c, r).getContents();
		    		String labelContents = sheet.getCell(c, 0).getContents().replaceAll("\\s+", "");
		    		String fieldTitle = (String)currentEnum.getClass().getMethod("getFieldName").invoke(currentEnum);
		    		String columnTitle = (String)currentEnum.getClass().getMethod("getColumnTitle").invoke(currentEnum);
		    		if (cellContents.equals("")) {
                        c++;
                    } else if (columnTitle.equalsIgnoreCase(labelContents)
                    			|| fieldTitle.equalsIgnoreCase(labelContents)) {
                        Method enumSetter = currentEnum.getClass().getMethod("getSetterName");
                        String setterName = (String) enumSetter.invoke(currentEnum);
                        Class fieldType = (Class) currentEnum.getClass().getMethod("getType").invoke(currentEnum);
                        Method fieldSetter = clazz.getMethod(setterName, fieldType);
                        if (fieldType.getSimpleName().equalsIgnoreCase(Calendar.class.getSimpleName())) {
                            DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
                            Calendar calendar = Calendar.getInstance();
                            try {
                                calendar.setTime(formatter.parse(cellContents));
                                fieldSetter.invoke(rowRecord, fieldType.cast(calendar));
                            } catch (ParseException e) {
                                logger.error("Error parsing date string: "+cellContents);
                                fieldSetter.invoke(rowRecord, fieldType.cast(null));
                            }
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
		    		logger.error("Some uhandled exception was caught while trying to parse record at column " + c + " row " + r, e);
		    	}
			}
    	} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
    		logger.error("Error getting list of record enums", e);
    	}
    	return (Record) rowRecord;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static List<Object> getColumnOrder(Class clazz, Sheet sheet, Object rowRecord) {
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
					//TODO create a new EXTRA_COLUMN ENUM??
					columnEnums.add("EXTRA_COLUMN");
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				logger.error("Excpetion trying to get order of columns from sheet " + sheet.getName(), e);
			}
    	}
		return columnEnums;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static Record readUnorderedRowIntoRecord(Class clazz, Sheet sheet, Object rowRecord, int rowNumber, List<Object> columnOrder) {
    	int c = 0;
    	for (Object column : columnOrder) {
    		try {
    			String cellContents = sheet.getCell(c, rowNumber).getContents();
    			if (!column.equals("EXTRA_COLUMN") && !cellContents.equals("")) {
    				//TODO split this into another method
    				Method enumSetter = column.getClass().getMethod("getSetterName");
    				String setterName = (String) enumSetter.invoke(column);
    				Class fieldType = (Class) column.getClass().getMethod("getType").invoke(column);
    				Method fieldSetter = clazz.getMethod(setterName, fieldType);
    				if (fieldType.getSimpleName().equalsIgnoreCase(Calendar.class.getSimpleName())) {
    					DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
    					Calendar calendar = Calendar.getInstance();
    					try {
    						calendar.setTime(formatter.parse(cellContents));
    						fieldSetter.invoke(rowRecord, fieldType.cast(calendar));
    					} catch (ParseException e) {
    						logger.error("Error parsing date string: "+cellContents);
    						fieldSetter.invoke(rowRecord, fieldType.cast(null));
    					}
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
    			}
    			c++;
    		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
    			logger.error("Error trying to read cell into record object, column " + c + " row " + rowNumber, e);
    		} catch (Exception e) {
    			logger.error("Some uhandled exception was caught while trying to parse record at column " + c + " row " + rowNumber, e);
    		}
    	}
    	return (Record) rowRecord;
    }

    static <T> List<List<Record>> splitByField(List<Record> records, String fieldName, Class<T> clazz) {
		List<List<Record>> recordListList = new ArrayList<>();
		Method fieldGetter = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equalsIgnoreCase("get" + fieldName.replace(" ", ""))) {
				fieldGetter = method;
			}
		}
		Object groupingDelimiter = null;
		List<Record> delimiterList = new ArrayList<>();
		for (Record record : records) {
			try {
				Object delimiterValue = fieldGetter.invoke(record)==null?"":fieldGetter.invoke(record);
//				//to set the initial group
//				if (groupingDelimiter == null) {
//					groupingDelimiter = delimiterValue;
//				}
				if (groupingDelimiter!=null && delimiterValue.equals(groupingDelimiter)) {
					delimiterList.add(record);
				} else {
					if (!delimiterList.isEmpty()) {
						recordListList.add(delimiterList);
					}
					groupingDelimiter = delimiterValue;
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
    
}
