package com.mcd.spider.main.entities.record;

import jxl.write.WritableSheet;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
}
