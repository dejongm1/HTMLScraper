package com.mcd.spider.main.util;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

public class ExcelWriter {

	public static final Logger logger = Logger.getLogger(ExcelWriter.class);

	private String docName;
	private WritableWorkbook workbook;
	private State state;
	private Record record;
	private static final String OUTPUT_DIR = "output/";
	private File oldBook;
	private File newBook;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;

	public ExcelWriter(State state, Record record, Site site) {
		Calendar date = Calendar.getInstance();
		this.docName = state.getName() 
		+ "_" + (date.get(Calendar.MONTH)+1) 
		+ "-" + date.get(Calendar.DAY_OF_MONTH) 
		+ "-" + date.get(Calendar.YEAR) + "_" 
		+ record.getClass().getSimpleName() + "_"
        + site.getName() + ".xls";
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
	public File getOldBook() {
		return oldBook;
	}
	public void setOldBook(File oldBook) {
		this.oldBook = oldBook;
	}
	public File getNewBook() {
		return newBook;
	}
	public void setNewBook(File newBook) {
		this.newBook = newBook;
	}
	public Workbook getCurrentWorkbook() {
		return currentWorkbook;
	}

	public void setCurrentWorkbook(Workbook currentWorkbook) {
		this.currentWorkbook = currentWorkbook;
	}

	public WritableWorkbook getCopyWorkbook() {
		return copyWorkbook;
	}

	public void setCopyWorkbook(WritableWorkbook copyWorkbook) {
		this.copyWorkbook = copyWorkbook;
	}

	public void createSpreadhseet() {
		WritableWorkbook newWorkbook = null;
		try {
			//currently overwrites previous workbook - need something different?
			newWorkbook = Workbook.createWorkbook(new File(OUTPUT_DIR + docName));

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

	public void saveRecordsToWorkbook(List<Record> records) {
		try {
			createWorkbookCopy();
			
			int rowNumber = getCopyWorkbook().getSheet(0).getRows();
			for (Record currentRecord : records) {
				currentRecord.addToExcelSheet(getCopyWorkbook(), rowNumber);
				rowNumber++;
			}
			
			replaceOldBookWithNew();
		} catch (IOException | WriteException | IllegalAccessException | BiffException  e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}

    public void addRecordToWorkbook(Record record) {
        try {
            createWorkbookCopy();

            int rowNumber = getCopyWorkbook().getSheet(0).getRows();

            record.addToExcelSheet(getCopyWorkbook(), rowNumber);

            replaceOldBookWithNew();
        } catch (IOException | WriteException | IllegalAccessException | BiffException  e) {
            logger.error("Error trying to save record to workbook", e);
        }
    }
	
	public void findPossibleDuplicates() {
		//use name
	}
	
	public boolean removeColumnsFromSpreadsheet(int[] args) {
		boolean successful = false;
		try {
			createWorkbookCopy();
			
			WritableSheet sheet = getCopyWorkbook().getSheet(0);
			
			for (int c=0;c<args.length;c++) {
				sheet.removeColumn(args[c]);
			}

			replaceOldBookWithNew();
		} catch (IOException | WriteException | BiffException e) {
			logger.error("Error trying to remove ID column from workbook", e);
		}
		return successful;
	}
	
	private void createWorkbookCopy() throws BiffException, IOException {
		setOldBook(new File(OUTPUT_DIR + docName));
		setNewBook(new File(OUTPUT_DIR + "temp_copy.xls"));
		setCurrentWorkbook(Workbook.getWorkbook(getOldBook()));
		setCopyWorkbook(Workbook.createWorkbook(getNewBook(), getCurrentWorkbook()));
	}
	
	private void replaceOldBookWithNew() throws IOException, WriteException {
		getCopyWorkbook().write(); 
		getCopyWorkbook().close();
		getCurrentWorkbook().close();
		
		if (getOldBook().delete()) {
			getNewBook().renameTo(new File(OUTPUT_DIR + docName));
		} else {
			//making sure we don't lose data or override good data
			getNewBook().renameTo(new File(OUTPUT_DIR + docName + System.currentTimeMillis()));
		}
	}

}
