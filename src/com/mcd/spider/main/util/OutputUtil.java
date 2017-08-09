package com.mcd.spider.main.util;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OutputUtil {

	public static final Logger logger = Logger.getLogger(OutputUtil.class);

	private String docName;
	private WritableWorkbook workbook;
	private State state;
	private File idFile;
	private File uncrawledIdFile;
	private Record record;
	private static final String OUTPUT_DIR = "output/";
	private static final String TRACKING_DIR = "tracking/";
	private File oldBook;
	private File newBook;
	private Site site;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private WritableWorkbook backupWorkbook;
	private Calendar workbookCreateDate;
	private boolean offline;

	public OutputUtil(State state, Record record, Site site) {
		Calendar date = Calendar.getInstance();
		this.workbookCreateDate = date;
		this.docName = state.getName() 
				+ "_" + (date.get(Calendar.MONTH)+1)
				+ "-" + date.get(Calendar.DAY_OF_MONTH)
				+ "-" + date.get(Calendar.YEAR) + "_"
				+ record.getClass().getSimpleName() + "_"
				+ site.getName() + ".xls";
		this.state = state;
		this.record = record;
		this.site = site;
		this.idFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Archive.txt");
		this.uncrawledIdFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Uncrawled.txt");
		this.offline = System.getProperty("offline").equals("true");
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
		if (!offline) {
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
				workbook = newWorkbook;//this only works if I create one spreadsheet per OutputUtil
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

	public void createFilteredSpreadsheet(RecordFilterEnum filter, List<Record> records) {
		WritableWorkbook newWorkbook = null;
		try {
			newWorkbook = Workbook.createWorkbook(new File(OUTPUT_DIR + getFilteredDocName(filter)));

			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);

			//create columns based on Record.getFieldsToOutput()
			int columnNumber = 0;
			for (Field recordField : record.getFieldsToOutput()) {
				//********extract to createLabelMethod????
				Label columnLabel = new Label(columnNumber, 0, recordField.getName().toUpperCase());
				excelSheet.addCell(columnLabel);
				columnNumber++;
			}
			saveRecordsToWorkbook(records, newWorkbook);
			newWorkbook.write();
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

	private String getFilteredDocName(RecordFilterEnum filter) {
		return docName.substring(0, docName.indexOf(".xls")) + filter.filterName() + ".xls";
	}

	public void saveRecordsToWorkbook(List<Record> records, WritableWorkbook workbook) {
		try {
			int rowNumber = workbook.getSheet(0).getRows();
			for (Record currentRecord : records) {
				currentRecord.addToExcelSheet(workbook, rowNumber);
				rowNumber++;
			}
		} catch (IllegalAccessException e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}

	public void saveRecordsToMainWorkbook(List<Record> records) {
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

	public void addRecordToMainWorkbook(Record record) {
		try {
			createWorkbookCopy();
			int rowNumber = copyWorkbook.getSheet(0).getRows();
			record.addToExcelSheet(copyWorkbook, rowNumber);
			writeIdToFile(idFile, record.getId());
			replaceOldBookWithNew();
		} catch (IOException | WriteException | IllegalAccessException | BiffException  e) {
			logger.error("Error trying to save record to workbook: " + record.getId(), e);
		}
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

	private void writeIdToFile(File outputFile, String id) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(outputFile, true);
			bw = new BufferedWriter(fw);
			bw.write(id);
			bw.newLine();
		} catch (IOException e) {
			logger.error("Error trying to write id to file: " + id, e);
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				logger.error("Error trying to write id to file: " + id, e);
			}
		}
	}

	public void backupUnCrawledRecords(Map<Object, String> recordsDetailsUrlMap) {
		//save, at minimum IDs, to a file
		//wipe out file first?
		for (Map.Entry<Object,String> entry : recordsDetailsUrlMap.entrySet()) {
			try {
				String id = (String) site.generateRecordId(entry.getValue());
				writeIdToFile(uncrawledIdFile, id);
			} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
				//not a record detail url so ID could not be parsed
			}
		}
	}
}
