package com.mcd.spider.main.util.io;

import com.google.common.base.CaseFormat;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.main.entities.site.Site;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author u569220
 *
 */
public class RecordOutputUtil {

	public static final Logger logger = Logger.getLogger(RecordOutputUtil.class);
	private static final String TEMP = "temp_copy";
	private static final String EXT = ".xls";
    private static final String OUTPUT_DIR = "output/";
    private static final String TRACKING_DIR = "tracking/";

	private String docName;
	private WritableWorkbook workbook;
	private State state;
	private File idFile;
	private File uncrawledIdFile;
	private Record record;
	private File oldBook;
	private File newBook;
	private Site site;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private Calendar workbookCreateDate;
	private RecordIOUtil ioutil;

	public RecordOutputUtil(RecordIOUtil ioUtil, State state, Site site) {
		Calendar date = Calendar.getInstance();
		this.workbookCreateDate = date;
		this.docName = ioUtil.getDocName();
		this.state = state;
		this.record = ioUtil.getRecord();
		this.site = site;
		this.idFile = ioUtil.getIdFile();
		this.ioutil = ioUtil;
		this.uncrawledIdFile = new File(OUTPUT_DIR + TRACKING_DIR + site.getName() + "_Uncrawled.txt");
	}

	public String getDocName() {
		return docName;
	}
//
//	public WritableWorkbook getWorkbook() {
//		return workbook;
//	}
	public State getState() {
		return state;
	}
	public Record getRecord() {
		return record;
	}
	public boolean splitIntoSheets(String docName, String delimiter, List<List<Record>> recordsListList, Class clazz) {
		boolean successful = false;
		Method fieldGetter = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equalsIgnoreCase("get" + delimiter.replace(" ", ""))) {
				fieldGetter = method;
			}
		}
		try {
			createWorkbookCopy(docName, getTempFileName() + EXT);
			for (int s = 0; s < recordsListList.size(); s++) {
				try {
					String delimitValue = (String) fieldGetter.invoke(recordsListList.get(s).get(0));
					WritableSheet excelSheet = copyWorkbook.getSheet(delimitValue);
					if (excelSheet == null) {
						//append a new sheet for each
						excelSheet = copyWorkbook.createSheet(delimitValue == null ? "empty" : delimitValue, s + 1);
					}
					createColumnHeaders(excelSheet);
					for (int r = 0; r < recordsListList.get(s).size(); r++) {
						recordsListList.get(s).get(r).addToExcelSheet(r+1, excelSheet);
					}
				} catch (NullPointerException e) {
					logger.error("Error trying split workbook into sheets by " + fieldGetter.getName(), e);
				}
			}
			handleBackup(docName, true);
		} catch (IOException | WriteException | BiffException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error trying split workbook into sheets", e);
		}
		return successful;
	}

	public void createSpreadsheet() {
		WritableWorkbook newWorkbook = null;
		try {
			createWorkbookCopy(docName,
					docName.substring(0, docName.indexOf(EXT)) + "_" + (workbookCreateDate.get(Calendar.MONTH) + 1) + "-"
							+ workbookCreateDate.get(Calendar.DAY_OF_MONTH) + "-"
							+ workbookCreateDate.get(Calendar.YEAR) + EXT);
			workbook = copyWorkbook;
			handleBackup(docName, false);
		} catch (BiffException | IOException | WriteException e) {
			logger.error("Create spreadhseet error", e);
		}
		try {
			if (workbook == null) {
				newWorkbook = Workbook.createWorkbook(new File(docName));
				WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);
				createColumnHeaders(excelSheet);
				newWorkbook.write();
				workbook = newWorkbook;
			}
		} catch (IOException | WriteException e) {
			logger.error("Create spreadhseet error", e);
		} finally {
			if (newWorkbook != null) {
				try {
					newWorkbook.close();
				} catch (IOException | WriteException e) {
					logger.error("Create spreadhseet error", e);
				}
			}
		}
	}

    public String getTempFileName() {
        return TEMP + "_" + Calendar.getInstance().getTimeInMillis();
    }

	private void createWorkbookCopy(String oldBookName, String backupBookName) throws BiffException, IOException {
		oldBook = new File(oldBookName);
		newBook = new File(backupBookName);
		currentWorkbook = Workbook.getWorkbook(oldBook);
		copyWorkbook = Workbook.createWorkbook(newBook, currentWorkbook);
	}

	public String getFilteredDocName(RecordFilterEnum filter) {
		return docName.substring(0, docName.indexOf(EXT)) + "_" + filter.filterName() + EXT;
	}

	public String getMergedDocName() {
		return docName.substring(0, docName.indexOf(EXT)) + "_" + "MERGED" + EXT;
	}

	public void saveRecordsToWorkbook(List<Record> records, WritableWorkbook workbook) {
		try {
			int rowNumber = 1;
			for (Record currentRecord : records) {
				WritableSheet sheet = workbook.getSheet(0);
				currentRecord.addToExcelSheet(rowNumber, sheet);
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
				WritableSheet sheet = workbook.getSheet(0);
				currentRecord.addToExcelSheet(rowNumber, sheet);
				rowNumber++;
			}
		} catch (IllegalAccessException e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}

	public void addRecordToMainWorkbook(Record record) {
		try {
			createWorkbookCopy(docName, getTempFileName() + EXT);
			int rowNumber = ioutil.getInputter().getNonEmptyRowCount(copyWorkbook.getSheet(0));
			WritableSheet sheet = copyWorkbook.getSheet(0);
			record.addToExcelSheet(rowNumber, sheet);
			writeIdToFile(idFile, record.getId());
			handleBackup(docName, true);
		} catch (IOException | WriteException | IllegalAccessException | BiffException e) {
			logger.error("Error trying to save record to workbook: " + record.getId(), e);
		}
	}

	public boolean removeColumnsFromSpreadsheet(int[] args) {
		boolean successful = false;
		try {
			createWorkbookCopy(docName, getTempFileName() + EXT);

			WritableSheet sheet = copyWorkbook.getSheet(0);

			for (int c = 0; c < args.length; c++) {
				sheet.removeColumn(args[c]);
			}

			handleBackup(docName, true);
		} catch (IOException | WriteException | BiffException e) {
			logger.error("Error trying to remove ID column from workbook", e);
		}
		return successful;
	}

	private void handleBackup(String docName, boolean deleteBackup) throws IOException, WriteException {
		copyWorkbook.write();
		copyWorkbook.close();
		currentWorkbook.close();

		if (deleteBackup) {
			if (oldBook.delete()) {
				newBook.renameTo(new File(docName));
			} else {
				// making sure we don't lose data or override good data
				newBook.renameTo(new File(docName + System.currentTimeMillis()));
			}
		}
	}

	public void createFilteredSpreadsheet(RecordFilterEnum filter, List<Record> records) {
		WritableWorkbook newWorkbook = null;
		try {
			newWorkbook = Workbook.createWorkbook(new File(getFilteredDocName(filter)));

			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);
			createColumnHeaders(excelSheet);
			saveRecordsToWorkbook(records, newWorkbook);
			newWorkbook.write();
		} catch (IOException | WriteException e) {
			logger.error("Create filtered spreadhseet error", e);
		} finally {
			if (newWorkbook != null) {
				try {
					newWorkbook.close();
				} catch (IOException | WriteException e) {
					logger.error("Create filtered spreadhseet error", e);
				}
			}
		}
	}
	
	public void createMergedSpreadsheet(List<Record> records) {
		WritableWorkbook newWorkbook = null;
		try {
			newWorkbook = Workbook.createWorkbook(new File(getMergedDocName()));

			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);
			createColumnHeaders(excelSheet);
			saveRecordsToWorkbook(records, newWorkbook);
			newWorkbook.write();
		} catch (IOException | WriteException e) {
			logger.error("Create filtered spreadhseet error", e);
		} finally {
			if (newWorkbook != null) {
				try {
					newWorkbook.close();
				} catch (IOException | WriteException e) {
					logger.error("Create filtered spreadhseet error", e);
				}
			}
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
		// save, at minimum IDs, to a file
		// wipe out file first?
		for (Map.Entry<Object, String> entry : recordsDetailsUrlMap.entrySet()) {
			try {
				String id = site.generateRecordId(entry.getValue());
				writeIdToFile(uncrawledIdFile, id);
			} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
				// not a record detail url so ID could not be parsed
			}
		}
	}

	private void createColumnHeaders(WritableSheet excelSheet) throws WriteException {
		// create columns based on Record.getFieldsToOutput()
		int columnNumber = 0;
		for (Field recordField : record.getFieldsToOutput()) {
			// ********extract to createLabelMethod????
            CaseFormat format = record.getColumnCaseFormat();
			Label columnLabel = new Label(columnNumber, 0, CaseFormat.LOWER_CAMEL.to(format, recordField.getName()));
			excelSheet.addCell(columnLabel);
			columnNumber++;
		}
	}
}
