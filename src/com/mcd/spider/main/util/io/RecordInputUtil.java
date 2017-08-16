package com.mcd.spider.main.util.io;

import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.IDCheckException;
import com.mcd.spider.main.exception.SpiderException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author u569220
 *
 */
public class RecordInputUtil {

	public static final Logger logger = Logger.getLogger(RecordInputUtil.class);
    private static final String OUTPUT_DIR = "output/";
    private static final String TRACKING_DIR = "tracking/";

	private String docName;
//	private WritableWorkbook workbook;
//	private Record record;
	private boolean offline;
	private Site site;
	private File idFile;
	private Record record;

	public RecordInputUtil(String docName, File idFile, Site site, Record record) {
		Calendar date = Calendar.getInstance();
		this.docName = docName;
//		this.state = outputUtil.getState();
		this.record = record;
		this.site = site;
        this.offline = System.getProperty("offline").equals("true");
        this.idFile = idFile;
	}

	public String getDocName() {
		return docName;
	}


    public Set<String> getPreviousIds() throws SpiderException {
        Set<String> ids = new HashSet<>();
        //TODO check name as well to make sure it's the right state/site
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

    public Set<Record> readDefaultSpreadsheet() {
		return readSpreadsheet(new File(docName));
	}

	public Set<Record> readSpreadsheet(File fileToRead) {
		Set<Record> storedRecords = new HashSet<>();
        try {
            Workbook workbook = Workbook.getWorkbook(fileToRead);
            if (workbook !=null) {
                Sheet mainSheet = workbook.getSheet(0);
                //ignore headers since we have enum
                //does this logic belong in ArrestRecord?
                Class<?> clazz = Class.forName(record.getClass().getCanonicalName());
                Constructor<?> constructor = clazz.getConstructor();
                //starting with the first data row, read records into set
                for (int r=1;r<mainSheet.getRows();r++) {
                    //loop over columnEnums for each row
                    Object rowRecord = constructor.newInstance();
                    for (int c=0;c<record.getFieldsToOutput().size();c++) {
                        List<Object> enums = (List<Object>) clazz.getMethod("getColumnEnums").invoke(rowRecord);
                        String cellContents = mainSheet.getCell(c, r).getContents();
                        if (!cellContents.equals("")) {
                            Object currentEnum = enums.get(c);
                            Method enumSetter = currentEnum.getClass().getMethod("getSetterName");
                            String setterName = (String) enumSetter.invoke(currentEnum);
                            Class fieldType = (Class) currentEnum.getClass().getMethod("getType").invoke(currentEnum);
                            Method fieldSetter = clazz.getMethod(setterName, fieldType);
                            if (fieldType.getSimpleName().equalsIgnoreCase(Calendar.class.getSimpleName())) {
                                DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(formatter.parse(cellContents));
                                fieldSetter.invoke(rowRecord, fieldType.cast(calendar));
                            } else if (fieldType.getSimpleName().equalsIgnoreCase(long.class.getSimpleName())) {
                                fieldSetter.invoke(rowRecord, Long.parseLong(cellContents));
                            } else if (fieldType.getSimpleName().equalsIgnoreCase(int.class.getSimpleName())) {
                                fieldSetter.invoke(rowRecord, Integer.parseInt(cellContents));
                            } else if (fieldType.getSimpleName().equalsIgnoreCase(String[].class.getSimpleName())) {
                                String[] charges = cellContents.split("; ");
                                fieldSetter.invoke(rowRecord, fieldType.cast(charges));
                            } else {
                                fieldSetter.invoke(rowRecord, fieldType.cast(cellContents));
                            }
                        }
                    }
                    storedRecords.add((Record)rowRecord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return storedRecords;
	}

	public Set<Record> mergeRecordsFromSpreadsheets(File fileOne, File fileTwo) {
		//TODO
		Set<Record> storedRecordsOne = readSpreadsheet(fileOne);
		Set<Record> storedRecordsTwo = readSpreadsheet(fileTwo);
		Set<Record> compiledRecords = new HashSet<>();
		
		
		
		
		return compiledRecords;
	}
}
