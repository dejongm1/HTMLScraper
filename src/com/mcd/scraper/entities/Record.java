package com.mcd.scraper.entities;

import java.lang.reflect.Field;
import java.util.List;

public interface Record {

	public String getId();
	public void setId(String string);
	List<Field> getFieldsToOutput();

//	public Field[] getFieldsToOutput();
//	public  outputAsExcel();
//	public  outputAsText();
	
}
