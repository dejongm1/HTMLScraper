package com.mcd.scraper.entities;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by MikeyDizzle on 7/18/2017.
 */
public interface Record {

    String getId();

    void setId(String string);

    List<Field> getFieldsToOutput();

    WritableSheet addToExcelSheet(WritableWorkbook workbook, int rowNumber) throws IllegalAccessException;
}
