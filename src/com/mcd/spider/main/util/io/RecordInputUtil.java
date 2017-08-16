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
import java.io.FileNotFoundException;
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

	private String docName;
	private boolean offline;
	private File idFile;
	private Record record;

	public RecordInputUtil(RecordIOUtil ioUtil) {
		this.docName = ioUtil.getDocName();
		this.record = ioUtil.getRecord();
        this.offline = System.getProperty("offline").equals("true");
        this.idFile = ioUtil.getIdFile();
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
                    logger.error("Error reading previous IDs file", ioe);
                }
            }
        }
        return ids;
    }

    public Set<Record> readDefaultSpreadsheet() {
		return readSpreadsheet(new File(docName));
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<Record> readSpreadsheet(File fileToRead) {
    	Set<Record> storedRecords = new HashSet<>();
    	int foundRecordsCount = 0;
    	try {
    		logger.debug("Attempting to read previous records from " + fileToRead.getName() + " into memory");
    		Workbook workbook = Workbook.getWorkbook(fileToRead);
    		if (workbook !=null) {
    			Sheet mainSheet = workbook.getSheet(0);
    			foundRecordsCount = mainSheet.getRows();
    			//ignore headers since we have enum
    			//does this logic belong in ArrestRecord?
    			try {
    				Class<?> clazz = Class.forName(record.getClass().getCanonicalName());
    	    		logger.debug("Record type determined as " + record.getClass().getSimpleName());
    				Constructor<?> constructor = clazz.getConstructor();
    				//starting with the first data row, read records into set
    				for (int r=1;r<foundRecordsCount;r++) {
    					//loop over columnEnums for each row
    					try {
    						Object rowRecord = constructor.newInstance();
    						for (int c=0;c<record.getFieldsToOutput().size();c++) {
    							try {
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
	    									try {
	    										calendar.setTime(formatter.parse(cellContents));
	    									} catch (ParseException e) {
	    										logger.error("Error parsing date string: " + cellContents, e);
	    									}
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
    							} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
    	    						logger.error("Error trying to read cell into record object, column " + c + " row " + r, e);
    							}
    						}
    						storedRecords.add((Record)rowRecord);
    					} catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
    						logger.error("Error trying to read row into record object, row " + r, e);
    					}
    				}
    			} catch (ClassNotFoundException | NoSuchMethodException e) {
					logger.error("Error trying to get record and constructor", e);
    			}
    		}
    	} catch (FileNotFoundException e) {
    		logger.error("No record file found", e);
    	} catch (IOException | BiffException e) {
    		logger.error("Exception caught while trying to read in previous records", e);
    	} catch (Exception e) {
    		logger.error("Unexpected exception while trying to read in records, refer to backup file created", e);
    	}

		logger.debug("Found " +  (foundRecordsCount-1) + " and retrieved " + storedRecords.size());
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
