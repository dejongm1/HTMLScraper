package com.mcd.scraper.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.mcd.scraper.entities.Record;
import com.mcd.scraper.entities.State;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelWriter {

	public static final Logger logger = Logger.getLogger(ExcelWriter.class);

	private String docName;
	private WritableWorkbook workbook;
	private State state;
	private Record record;
	private static final String OUTPUT_DIR = "output/";

	public ExcelWriter(State state, Record record) {
		Calendar date = Calendar.getInstance();
		this.docName = state.getName() 
		+ "_" + (date.get(Calendar.MONTH)+1) 
		+ "-" + date.get(Calendar.DAY_OF_MONTH) 
		+ "-" + date.get(Calendar.YEAR) + "_" 
		+ record.getClass().getSimpleName() + ".xls";
		this.state = state;
		this.record = record;
	}

	public String getDocName() {
		return docName;
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
	
	public void createSpreadhseet() {
		WritableWorkbook newWorkbook = null;
		try {
			//currently overwrites previous workbook - need something different
			newWorkbook = Workbook.createWorkbook(new File(OUTPUT_DIR + docName));

			// create an Excel sheet
			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);

			//create columns based on Record.getFieldsToOutput()
			int columnNumber = 0;
			for (Field recordField : record.getFieldsToOutput()) {
				//********extract to createLabelMethod????
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

	public void saveRecordsToWorkbook(List<Record> records) {
		File oldWorkbook = new File(OUTPUT_DIR + docName);
		File newWorkbook = new File(OUTPUT_DIR + "temp.xls");
		try {
			Workbook currentWorkbook = Workbook.getWorkbook(oldWorkbook);
			WritableWorkbook copy = Workbook.createWorkbook(newWorkbook, currentWorkbook);
			int rowNumber = copy.getSheet(0).getRows();
			for (Record currentRecord : records) {
				currentRecord.addToExcelSheet(copy, rowNumber);
				rowNumber++;
			}
			copy.write(); 
			copy.close();
			currentWorkbook.close();
			
			//swap temp book for new book
		} catch (IOException | BiffException | WriteException | IllegalAccessException  e) {
			logger.error("Error trying to save data to workbook", e);
		}
		if (oldWorkbook.delete()) {
			newWorkbook.renameTo(new File(OUTPUT_DIR + docName));
		} else {
			//making sure we don't lose data or override good data
			newWorkbook.renameTo(new File(OUTPUT_DIR + docName + System.currentTimeMillis()));
		}
		
	}
	
	public void findPossibleDuplicates() {
		//use name
	}
	
	public boolean removeIDColumnFromSpreadsheet(String excelFilePath) {
		boolean successful = false;
		
		return successful;
	}

}
