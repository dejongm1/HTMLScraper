package com.mcd.spider.util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.CaseFormat;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 
 * @author u569220
 *
 */
public class RecordOutputUtil {

	public static final Logger logger = Logger.getLogger(RecordOutputUtil.class);
	private static final String EXT = ".xls";
    private static final Calendar WORKBOOK_CREATE_DATE = Calendar.getInstance();
    private static final String BACKUP_SUFFIX = "_" + (WORKBOOK_CREATE_DATE.get(Calendar.MONTH) + 1) + "-"
                                                    + WORKBOOK_CREATE_DATE.get(Calendar.DAY_OF_MONTH) + "-"
                                                    + WORKBOOK_CREATE_DATE.get(Calendar.YEAR);

	private String docPath;
	private State state;
	private File crawledIdFile;
	private File uncrawledIdFile;
	private Record record;
	private File oldBook;
	private File newBook;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private RecordIOUtil ioutil;

	public RecordOutputUtil(RecordIOUtil ioUtil, State state) {
        this.docPath = ioUtil.getMainDocPath();
		this.state = state;
		this.record = ioUtil.getRecord();
		this.crawledIdFile = ioUtil.getCrawledIdFile();
		this.ioutil = ioUtil;
        this.uncrawledIdFile = ioUtil.getUncrawledIdFile();
	}

	public State getState() {
		return state;
	}
    public static String getBackupSuffix() {
        return BACKUP_SUFFIX;
    }

    public void createWorkbook(String workbookName, Set<Record> recordSet, boolean backUpExisting) {
        List<Set<Record>> recordSetList = new ArrayList<>();
        recordSetList.add(recordSet==null?new HashSet<>():recordSet);
        createWorkbook(workbookName, recordSetList, backUpExisting, new String[]{state.getName()});
    }
    
    public void createWorkbook(String workbookName, List<Set<Record>> recordSetList, boolean backUpExisting, String[] sheetNames) {
        WritableWorkbook newWorkbook = null;
        try {
            //backup existing workbook first
            if (backUpExisting) logger.info("Backing up " + workbookName + " as " + docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT + " and starting a new workbook");
            if (new File(docPath).exists() && backUpExisting) {
                createWorkbookCopy(docPath,
                        docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT);
                handleBackup(docPath, false);
            }
            newWorkbook = Workbook.createWorkbook(new File(workbookName));
            
            logger.info("Creating " + sheetNames.length + " sheets in workbook " + workbookName);
            for (int rs = 0;rs<recordSetList.size();rs++) {
	            WritableSheet excelSheet = newWorkbook.createSheet(sheetNames[rs], rs);
	            createColumnHeaders(excelSheet);
            }
            if (!recordSetList.isEmpty() && !recordSetList.get(0).isEmpty()) {
                saveRecordsToWorkbook(recordSetList, newWorkbook);
            }
            newWorkbook.write();
        } catch (IOException | WriteException | BiffException e) {
            logger.error("Create " + workbookName + "spreadsheet error", e);
        } finally {
            if (newWorkbook != null) {
                try {
                    newWorkbook.close();
                } catch (IOException | WriteException e) {
                    logger.error("Close " + workbookName + "spreadsheet error", e);
                }
            }
        }
    }

    public String getTempFileName() {
	    String tempFileName = RecordIOUtil.getOUTPUT_DIR() + "temp_copy" + "_" + Calendar.getInstance().getTimeInMillis();
	    if (new File(tempFileName).exists()) {
	        return tempFileName + "different";
        } else {
            return tempFileName;
        }
    }

	private void createWorkbookCopy(String oldBookName, String backupBookName) throws BiffException, IOException {
		oldBook = new File(oldBookName);
		newBook = new File(backupBookName);
		currentWorkbook = Workbook.getWorkbook(oldBook);
		copyWorkbook = Workbook.createWorkbook(newBook, currentWorkbook);
	}

	public String getFilteredDocPath(RecordFilterEnum filter) {
		return docPath.substring(0, docPath.indexOf(EXT)) + "_" + filter.filterName() + EXT;
	}

	public String getMergedDocPath() {
		return docPath.substring(0, docPath.lastIndexOf('_')) + "_" + "MERGED" + EXT;
	}

	public void saveRecordsToWorkbook(Set<Record> records, WritableWorkbook workbook) {
		List<Set<Record>> recordSetList = new ArrayList<>();
		recordSetList.add(records);
		saveRecordsToWorkbook(recordSetList, workbook);
	}

	public void saveRecordsToWorkbook(List<Set<Record>> recordSetList, WritableWorkbook workbook) {
		try {
			logger.info("Beginning to add " + recordSetList.size() + " sheets to workbook");
			int rs = 0;
			for (Set<Record> recordSet : recordSetList) {
				int rowNumber = 1;
				WritableSheet sheet = workbook.getSheet(rs);
				logger.info(recordSet.size() + " records in sheet " + rs);
				for (Record currentRecord : recordSet) {
					currentRecord.addToExcelSheet(rowNumber, sheet);
					rowNumber++;
				}
				rs++;
			}
		} catch (IllegalAccessException e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}

	public void addRecordToMainWorkbook(Record record) {
		try {
			createWorkbookCopy(docPath, getTempFileName() + EXT);
			int rowNumber = ioutil.getInputter().getNonEmptyRowCount(copyWorkbook.getSheet(0));
			WritableSheet sheet = copyWorkbook.getSheet(0);
			record.addToExcelSheet(rowNumber, sheet);
			writeIdToFile(crawledIdFile, record.getId());
			handleBackup(docPath, true);
		} catch (IOException | WriteException | IllegalAccessException | BiffException e) {
			logger.error("Error trying to save record to workbook: " + record.getId(), e);
		}
	}

	public boolean removeColumnsFromSpreadsheet(Integer[] args, String docName) {
		boolean successful = false;
		try {
			createWorkbookCopy(docName, getTempFileName() + EXT);

			WritableSheet sheet = copyWorkbook.getSheet(0);

			//need to do this in reverse order or removing columns will change the index of all following columns
			Arrays.sort(args, Collections.reverseOrder());
			for (int c = 0; c < args.length; c++) {
				sheet.removeColumn(args[c]);
			}

			handleBackup(docName, true);
		} catch (IOException | WriteException | BiffException e) {
			logger.error("Error trying to remove column(s) from workbook", e);
		}
		return successful;
	}

	private void handleBackup(String docName, boolean deleteBackup) throws IOException, WriteException {
		copyWorkbook.write();
		copyWorkbook.close();
		currentWorkbook.close();

		if (deleteBackup) {
			if (!oldBook.delete()) {
				// making sure we don't lose data or override good data
                logger.error("Can't save record to current workbook, is it open? Starting a new workbook to finish processing");
                this.docPath = docName.substring(0, docName.indexOf(EXT)) + "_" + System.currentTimeMillis() + EXT;
                ioutil.setMainDocPath(this.docPath);
			}
			newBook.renameTo(new File(docName));
		}
	}

    public boolean splitIntoSheets(String docName, String delimiterColumn, List<Set<Record>> recordsSetList, Class clazz) {
    	//TODO this is appending split records, not overriding
        boolean successful = false;
        Method fieldGetter = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equalsIgnoreCase("get" + delimiterColumn)) {
                fieldGetter = method;
            }
        }
        try {
            createWorkbookCopy(docName, getTempFileName() + EXT);
            for (int s = 0; s < recordsSetList.size(); s++) {
                try {
                    String delimitValue = (String) fieldGetter.invoke(recordsSetList.get(s).toArray()[0]);
                    WritableSheet excelSheet = copyWorkbook.getSheet(delimitValue);
                    if (excelSheet == null) {
                        //append a new sheet for each
                        excelSheet = copyWorkbook.createSheet(delimitValue == null ? "empty" : delimitValue, s + 1);
                    }
                    createColumnHeaders(excelSheet);
                    Record[] recordArray = recordsSetList.get(s).toArray(new Record[recordsSetList.get(s).size()]);
                    for (int r = 0; r < recordArray.length; r++) {
                    	recordArray[r].addToExcelSheet(r+1, excelSheet);
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
		for (Map.Entry<Object, String> entry : recordsDetailsUrlMap.entrySet()) {
			try {
			    if (!entry.getValue().startsWith("CRAWLED")) {
                    writeIdToFile(uncrawledIdFile, (String)entry.getKey());
                }
			} catch (Exception e) {
				logger.error("Error backing up " + entry.getKey() + "=" + entry.getValue() + "-" + e.getMessage());
			}
		}
	}

	protected void createColumnHeaders(WritableSheet excelSheet) throws WriteException {
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
