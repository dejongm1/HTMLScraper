package com.mcd.spider.main.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;

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
	private Site site;
	private File idFile;
	private Record record;
	private static final String OUTPUT_DIR = "output/";
    private static final String TRACKING_DIR = "tracking/";
	private File oldBook;
	private File newBook;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private WritableWorkbook backupWorkbook;
	private Calendar workbookCreateDate;

	public ExcelWriter(State state, Record record, Site site) {
	    Calendar date = Calendar.getInstance();
		this.workbookCreateDate = date;
		this.docName = state.getName() 
		+ "_" + (date.get(Calendar.MONTH)+1)
		+ "-" + date.get(Calendar.DAY_OF_MONTH)
		+ "-" + date.get(Calendar.YEAR) + "_"
		+ record.getClass().getSimpleName() + "_"
        + site.getName() + ".xls";
		this.site = site;
		this.state = state;
		this.record = record;
		this.idFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Archive.txt");
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
    public WritableWorkbook getBackupWorkbook() {
        return backupWorkbook;
    }
	public Calendar getWorkbookCreateDate() {return this.workbookCreateDate; }
	public void setCurrentWorkbook(Workbook currentWorkbook) {
		this.currentWorkbook = currentWorkbook;
	}
	public WritableWorkbook getCopyWorkbook() {
		return copyWorkbook;
	}
	public void setCopyWorkbook(WritableWorkbook copyWorkbook) {
		this.copyWorkbook = copyWorkbook;
	}

    public Set<String> getPreviousIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        //check name as well to make sure it's the right state/site
        BufferedReader br = null;
        try {
            if (!idFile.exists()) {
                idFile.createNewFile();
            }
            br = new BufferedReader(new FileReader(idFile));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                ids.add(sCurrentLine);
            }
        } catch (IOException e) {
            throw new IDCheckException();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                throw new IDCheckException();
            }
        }
        return ids;
    }

    public void createSpreadsheet() {
        WritableWorkbook newWorkbook = null;
        try {
			createWorkbookCopy();
	        workbook = copyWorkbook;
	        replaceOldBookWithNew();
        } catch (BiffException | IOException | WriteException e) {
        	logger.error(e.getMessage());
		}
        try {
            if (workbook==null) {
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
                workbook = newWorkbook;//this only works if I create one spreadsheet per ExcelWriter
            }
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

    public void saveRecordsToWorkbook(List<Record> records) {
        try {
            int rowNumber = 0;
            for (Record currentRecord : records) {
                currentRecord.addToExcelSheet(workbook, rowNumber);
                rowNumber++;
            }
        } catch (IllegalAccessException e) {
            logger.error("Error trying to save data to workbook", e);
        }
    }

    public void addRecordToWorkbook(Record record) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            createWorkbookCopy();
            int rowNumber = copyWorkbook.getSheet(0).getRows();
            record.addToExcelSheet(copyWorkbook, rowNumber);
            fw = new FileWriter(idFile, true);
            bw = new BufferedWriter(fw);
            bw.write(record.getId());
            bw.newLine();
            replaceOldBookWithNew();
        } catch (IOException | WriteException | IllegalAccessException | BiffException  e) {
            logger.error("Error trying to save record to workbook", e);
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                logger.error("Error trying to save data to workbook", e);
            }
        }
    }

	public void findPossibleDuplicates() {
		//use name
	}

	public boolean removeColumnsFromSpreadsheet(int[] args) {
		boolean successful = false;
		try {
			createWorkbookCopy();
			
			WritableSheet sheet = copyWorkbook.getSheet(0);
			
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
		oldBook = new File(OUTPUT_DIR + docName);
		newBook = new File(OUTPUT_DIR + "temp_copy.xls");
		currentWorkbook = Workbook.getWorkbook(oldBook);
		copyWorkbook = Workbook.createWorkbook(newBook, currentWorkbook);
	}
	
	private void replaceOldBookWithNew() throws IOException, WriteException {
		copyWorkbook.write();
		copyWorkbook.close();
		currentWorkbook.close();

		if (oldBook.delete()) {
			newBook.renameTo(new File(OUTPUT_DIR + docName));
		} else {
			//making sure we don't lose data or override good data
			newBook.renameTo(new File(OUTPUT_DIR + docName + System.currentTimeMillis()));
		}
	}
}
