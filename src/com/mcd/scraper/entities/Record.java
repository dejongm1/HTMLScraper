package com.mcd.scraper.entities;

import jxl.write.WritableSheet;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by MikeyDizzle on 7/18/2017.
 */
public interface Record {

    String getId();

    void setId(String string);

    List<Field> getFieldsToOutput();

    WritableSheet addToExcelSheet(WritableSheet worksheet, int rowNumber) throws IllegalAccessException;
}
