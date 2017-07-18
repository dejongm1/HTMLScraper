package com.mcd.scraper.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.mcd.scraper.entities.ArrestRecord;
import com.mcd.scraper.entities.State;

import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;

public class ExcelWriter {
	
	private String baseDocName;
	private WritableWorkbook workbook;
	
	public ExcelWriter(State state) {
		Calendar date = Calendar.getInstance();
		this.baseDocName = state.getName() + "_" + (date.get(Calendar.MONTH)+1) + "-" + date.get(Calendar.DAY_OF_MONTH) + "-" + date.get(Calendar.YEAR);
	}

	public String getBaseDocName() {
		return baseDocName;
	}
	public WritableWorkbook getWorkbook() {
		return workbook;
	}
	public void setWorkbook(WritableWorkbook workbook) {
		this.workbook = workbook;
	}

	public void createSpreadhseet(Class<ArrestRecord> recordClass) {
		WritableWorkbook newWorkbook = null;
        try {
            newWorkbook = Workbook.createWorkbook(new File(getBaseDocName() + "_" + recordClass.getSimpleName() + ".xls"));

            // create an Excel sheet
            WritableSheet excelSheet = newWorkbook.createSheet("Sheet 1", 0);

            //create columns based on ArrestRecord.getFieldsToOutput()
            
//            // add something into the Excel sheet
//            Label label = new Label(0, 0, "Test Count");
//            excelSheet.addCell(label);
//
//            Number number = new Number(0, 1, 1);
//            excelSheet.addCell(number);
//
//            label = new Label(1, 0, "Result");
//            excelSheet.addCell(label);
//
//            label = new Label(1, 1, "Passed");
//            excelSheet.addCell(label);
//
//            number = new Number(0, 2, 2);
//            excelSheet.addCell(number);
//
//            label = new Label(1, 2, "Passed 2");
//            excelSheet.addCell(label);

            newWorkbook.write();


        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (WriteException e) {
            e.printStackTrace();
        }*/ finally {

            if (newWorkbook != null) {
                try {
                	setWorkbook(newWorkbook);//this only works if I create one spreadsheet per ExcelWriter
                    newWorkbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            }


        }
		
	}
	
	public void checkForRecord() {
		
	}

}
