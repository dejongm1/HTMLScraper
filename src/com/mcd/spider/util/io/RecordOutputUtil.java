package com.mcd.spider.util.io;

import com.google.common.base.CaseFormat;
import com.mcd.spider.entities.io.RecordSheet;
import com.mcd.spider.entities.io.RecordWorkbook;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.Site;
import com.mcd.spider.util.SpiderConstants;
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
import java.util.*;

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
	private Site site;
	private File crawledIdFile;
	private File uncrawledIdFile;
	private Record record;
	private File oldBook;
	private File newBook;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private RecordIOUtil ioUtil;

	public RecordOutputUtil(RecordIOUtil ioUtil, Site site) {
        this.docPath = ioUtil.getMainDocPath();
		this.record = ioUtil.getRecord();
		this.crawledIdFile = ioUtil.getCrawledIdFile();
		this.ioUtil = ioUtil;
        this.uncrawledIdFile = ioUtil.getUncrawledIdFile();
        this.site = site;
	}

    public static String getBackupSuffix() {
        return BACKUP_SUFFIX;
    }

/*    public void createWorkbook(String workbookName, Set<Record> recordSet, boolean backUpExisting, Comparator comparator) {
        List<Set<Record>> recordSetList = new ArrayList<>();
        recordSetList.add(recordSet==null?new HashSet<>():recordSet);
        createWorkbook(workbookName, recordSetList, backUpExisting, new String[]{state.getName()}, comparator);
    }*/

    public void createWorkbook(String workbookName, RecordSheet recordSheet, boolean backUpExisting, Comparator comparator) {
        RecordWorkbook recordBook = new RecordWorkbook();
        recordBook.addSheet(recordSheet==null?new RecordSheet():recordSheet);
        createWorkbook(workbookName, recordBook, backUpExisting, null, comparator);
    }
    
    /*public void createWorkbook(String workbookName, List<Set<Record>> recordSetList, boolean backUpExisting, String[] sheetNames, Comparator comparator) {
        WritableWorkbook newWorkbook = null;
        File mainBook = new File(docPath);
        try {
            if (mainBook.exists() && backUpExisting) {
                logger.info("Backing up " + workbookName + " as " + docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT + " and starting a new workbook");
                createWorkbookCopy(docPath,
                        docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT);
                handleBackup(docPath, false);
            }
            try {
            	newWorkbook = Workbook.createWorkbook(new File(workbookName));
            } catch (IOException ioe) {
            	logger.error("Output workbook might be open. You have 15 seconds to close it.");
                try {
                    Thread.sleep(15000);
                    newWorkbook = Workbook.createWorkbook(new File(workbookName));
                } catch (InterruptedException ie) {
                    logger.error("Couldn't sleep for 15 seconds");
                }
            }
            
            logger.info("Creating " + sheetNames.length + " sheets in workbook " + workbookName);
            for (int rs = 0;rs<recordSetList.size();rs++) {
	            WritableSheet excelSheet = newWorkbook.createSheet(sheetNames[rs], rs);
	            createColumnHeaders(excelSheet);
            }
            if (!recordSetList.isEmpty() && !recordSetList.get(0).isEmpty()) {
                saveRecordsToWorkbook(recordSetList, newWorkbook, comparator);
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
    }*/

    public void createWorkbook(String workbookName, RecordWorkbook recordBook, boolean backUpExisting, String[] sheetNames, Comparator comparator) {
        WritableWorkbook newWorkbook = null;
        File mainBook = new File(docPath);
        try {
            if (mainBook.exists() && backUpExisting) {
                logger.info("Backing up " + workbookName + " as " + docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT + " and starting a new workbook");
                createWorkbookCopy(docPath,
                        docPath.substring(0, docPath.indexOf(EXT))+BACKUP_SUFFIX+EXT);
                handleBackup(docPath, false);
            }
            try {
            	newWorkbook = Workbook.createWorkbook(new File(workbookName));
            } catch (IOException ioe) {
            	logger.error("Output workbook might be open. You have 15 seconds to close it.");
                try {
                    Thread.sleep(15000);
                    newWorkbook = Workbook.createWorkbook(new File(workbookName));
                } catch (InterruptedException ie) {
                    logger.error("Couldn't sleep for 15 seconds");
                }
            }
            if (sheetNames==null) {
            	sheetNames = new String[]{SpiderConstants.MAIN_SHEET_NAME};
            }
            logger.info("Creating " + sheetNames.length + " sheets in workbook " + workbookName);
            for (int rs = 0;rs<recordBook.sheetCount();rs++) {
	            WritableSheet excelSheet = newWorkbook.createSheet(sheetNames[rs], rs);
	            createColumnHeaders(excelSheet);
            }
            if (!recordBook.isEmpty() && !recordBook.getSheet(0).isEmpty()) {
            	saveRecordsToWorkbook(recordBook, newWorkbook, comparator);
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

	public String getMergedDocPath(String baseDocPath) {
		String mergedName;
		if (baseDocPath!=null) {
			mergedName = baseDocPath.substring(0, baseDocPath.indexOf(EXT)).replace("_" + site.getName(), "") + "_" + "MERGED" + EXT;
		} else {
			mergedName = docPath.substring(0, docPath.lastIndexOf('_')) + "_" + "MERGED" + EXT;
		}
		return mergedName;
	}

	public String getLNPath() {
		return docPath.substring(0, docPath.indexOf(EXT)) + "_" + "LN" + EXT;
	}

	private void handleBackup(String docName, boolean deleteBackup) throws IOException, WriteException {
		copyWorkbook.write();
		copyWorkbook.close();
		currentWorkbook.close();

		if (deleteBackup) {
			if (!oldBook.delete()) {
				// making sure we don't lose data or override good data
                logger.error("Can't save record to current workbook, is it open? You have 10 seconds to close it or I'm starting a new workbook to finish processing");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error("Tried to sleep but I couldn't");
                }
                if (!oldBook.delete()) {
                    this.docPath = docName.substring(0, docName.indexOf(EXT))+"_"+System.currentTimeMillis()+EXT;
                    ioUtil.setMainDocPath(this.docPath);
                }
			}
			newBook.renameTo(new File(docName));
		}
	}

/*	public void saveRecordsToWorkbook(List<Set<Record>> recordSetList, WritableWorkbook workbook, Comparator comparator) {
		try {
			logger.info("Beginning to add " + recordSetList.size() + " sheets to workbook");
			int rs = 0;
			for (Set<Record> recordSet : recordSetList) {
                logger.info("Sorting records before trying to save");
                List<Record> sortedList = Record.getAsSortedList(recordSet, comparator);
				int rowNumber = 1;
				WritableSheet sheet = workbook.getSheet(rs);
				logger.info(recordSet.size() + " records in sheet " + rs);
				for (Record currentRecord : sortedList) {
					currentRecord.addToExcelSheet(rowNumber, sheet);
					rowNumber++;
				}
				rs++;
			}
		} catch (IllegalAccessException e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}*/

	public void saveRecordsToWorkbook(RecordWorkbook recordBook, WritableWorkbook workbook, Comparator comparator) {
		try {
			logger.info("Beginning to add " + recordBook.sheetCount() + " sheets to workbook");
			int rs = 0;
			for (RecordSheet recordSet : recordBook.getSheets()) {
                logger.info("Sorting records before trying to save");
                List<Record> sortedList = Record.getAsSortedList(recordSet.getRecords(), comparator);
				int rowNumber = 1;
				WritableSheet sheet = workbook.getSheet(rs);
				logger.info(recordSet.recordCount() + " records in sheet " + rs);
				for (Record currentRecord : sortedList) {
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
			int rowNumber = ioUtil.getInputter().getNonEmptyRowCount(copyWorkbook.getSheet(0));
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

/*	public void saveRecordsToWorkbook(Set<Record> records, WritableWorkbook workbook, Comparator comparator) {
		List<Set<Record>> recordSetList = new ArrayList<>();
		recordSetList.add(records);
		saveRecordsToWorkbook(recordSetList, workbook, comparator);
	}*/

	public void saveRecordsToWorkbook(RecordSheet recordSheet, WritableWorkbook workbook, Comparator comparator) {
		RecordWorkbook recordBook = new RecordWorkbook();
		recordBook.addSheet(recordSheet);
		saveRecordsToWorkbook(recordBook, workbook, comparator);
	}

   /* public boolean splitIntoSheets(String docName, String delimiterColumn, List<Set<Record>> recordsSetList, Class clazz, Comparator comparator) {
    	logger.info("Splitting " + docName + " into sheets by " + delimiterColumn);
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
//                    Record[] recordArray = recordsSetList.get(s).toArray(new Record[recordsSetList.get(s).size()]);
                    logger.info("Sorting records before trying to save");
                    List<Record> sortedList = Record.getAsSortedList(recordsSetList.get(s), comparator);
                    for (int r = 0; r < sortedList.size(); r++) {
                    	sortedList.get(r).addToExcelSheet(r+1, excelSheet);
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
    }*/

    public boolean splitIntoSheets(String docName, String delimiterColumn, RecordWorkbook recordsBook, Class clazz, Comparator comparator) {
    	logger.info("Splitting " + docName + " into sheets by " + delimiterColumn);
        boolean successful = false;
        Method fieldGetter = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equalsIgnoreCase("get" + delimiterColumn)) {
                fieldGetter = method;
            }
        }
        if (!new File(docName).exists()) {
        	//create a workbook with first sheet
        	createWorkbook(docName, recordsBook.getSheet(0), false, comparator);
        }
        //remove first sheet from book as it should already be written
        recordsBook.removeSheet(0);
        try {
            createWorkbookCopy(docName, getTempFileName() + EXT);
            for (int s = 0; s < recordsBook.sheetCount(); s++) {
                try {
                    String delimitValue = (String) fieldGetter.invoke(recordsBook.getFirstRecordFromSheet(s));
                    WritableSheet excelSheet = copyWorkbook.getSheet(delimitValue);
                    if (excelSheet == null) {
                        //append a new sheet for each
                        excelSheet = copyWorkbook.createSheet(delimitValue == null ? "empty" : delimitValue, s + 1);
                    }
                    createColumnHeaders(excelSheet);
//                    Record[] recordArray = recordsSetList.get(s).toArray(new Record[recordsSetList.get(s).size()]);
                    List<Record> sortedList = Record.getAsSortedList(recordsBook.getRecordsFromSheet(s), comparator);
                    for (int r = 0; r < sortedList.size(); r++) {
                    	sortedList.get(r).addToExcelSheet(r+1, excelSheet);
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

    protected void writeIdToFile(File outputFile, String id) {
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
			    //make sure it hasn't been crawled and it's a url that contains a record i.e. has a recordId
			    if (!entry.getValue().startsWith("CRAWLED") && site.obtainRecordId(entry.getValue())!=null) {
                    writeIdToFile(uncrawledIdFile, String.valueOf(entry.getKey()));
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
