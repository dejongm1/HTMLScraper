package com.mcd.scraper.util;

import com.mcd.scraper.entities.Record;
import com.mcd.scraper.entities.State;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;

public class ExcelWriter {

	public static final Logger logger = Logger.getLogger(ExcelWriter.class);

	private String baseDocName;
	private WritableWorkbook workbook;
	private State state;

	public ExcelWriter(State state) {
		Calendar date = Calendar.getInstance();
		this.baseDocName = "output/" + state.getName() + "_" + (date.get(Calendar.MONTH)+1) + "-" + date.get(Calendar.DAY_OF_MONTH) + "-" + date.get(Calendar.YEAR);
		this.state = state;
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
	public State getState() {
		return state;
	}
	
	public void createSpreadhseet(Record record) {
		WritableWorkbook newWorkbook = null;
		try {
			//currently overwrites previous workbook - need something different
			newWorkbook = Workbook.createWorkbook(new File(getBaseDocName() + "_" + record.getClass().getSimpleName() + ".xls"));

			// create an Excel sheet
			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);

			//create columns based on Record.getFieldsToOutput()
			int columnNumber = 0;
			for (Field recordField : record.getFieldsToOutput()) {
				Label columnLabel = new Label(columnNumber, 0, recordField.getName().toUpperCase());
				excelSheet.addCell(columnLabel);
				columnNumber++;
			}
			newWorkbook.write();
            setWorkbook(newWorkbook);//this only works if I create one spreadsheet per ExcelWriter
		} catch (IOException | WriteException e) {
			logger.error(e.getMessage());
		} finally {
        	if (newWorkbook != null) {
        		try {
        			newWorkbook.close();
        		} catch (IOException e) {
        			logger.error(e.getMessage());
        		} catch (WriteException e) {
        			logger.error(e.getMessage());
        		}
        	}
        }
	}

	public void checkForRecord() {
		//use ID

	}

	public WritableSheet getWorksheet(int sheetNumber) {
	    return getWorkbook().getSheet(sheetNumber);
    }

	public void findPossibleDuplicates() {
		//use name
	}
	
	public boolean removeIDColumnFromSpreadsheet(String excelFilePath) {
		boolean successful = false;
		
		return successful;
	}

}
