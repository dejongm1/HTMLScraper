package com.mcd.spider.util;

import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MainInputUtil {
	
	public MainInputUtil() {}

	public Object getInput(String prompt, int numberOfTries, String validationType) throws IOException {
		Object result;
		for(int t=1;t <= numberOfTries; t++){
			String input = readLine(prompt);
			if (validationType!= null && validationType.equals(SpiderConstants.URL_VALIDATION)) {
				result = convertToUrl(input);
			} else if (validationType!= null && validationType.equals(SpiderConstants.NUMBER_VALIDATION)) {
				result = convertToNumber(input);
            } else if (validationType!= null && validationType.equals(SpiderConstants.STATE_VALIDATION)) {
                result = convertToStates(input);
            } else if (validationType!= null && validationType.equals(SpiderConstants.FILTER_VALIDATION)) {
                result = convertToFilter(input);
            } else if (validationType!= null && validationType.equals(SpiderConstants.BOOLEAN_VALIDATION)) {
                result = convertToBoolean(input);
            } else {
				result = input;
			}
			if (result!=null) {
				return result;
			}
		}
		System.exit(0);
		return null;
	}
	
	public String convertToUrl(String input) {
		String result = null;
		String url = !input.startsWith("http")?"http://"+input:input;
		String[] schemes = {"http","https"};
	    UrlValidator urlValidator = new UrlValidator(schemes);
		if (urlValidator.isValid(url)) {
			result = url;
		} else {
			System.err.println("That's not a valid url\n");
		}
		return result;
		
	}

    public Integer convertToNumber(String input) {
        Integer result = null;
        try {
            result = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            if (input.equals("")) {
                result = 99999;
            } else {
                System.err.println("That's not a number\n");
            }
        }
        return result;
    }

    public List<State> convertToStates(String input) {
		List<State> result = new ArrayList<>();
		if (input.contains(",")) {
			String[] inputSplit = input.split("\\s*,\\s*");
			for (String inputPiece : inputSplit) {
				State state = State.getState(inputPiece);
				if (state!= null) {
					result.add(state);
				} else {
					System.err.println(inputPiece + " is not an American state");
				}
			}
		} else {
			result = State.confirmState(input);
		}
		if (result!=null && !result.isEmpty()) {
			return result;
		} else {
			System.out.println("That's not an American state\n");
			return null;
		}
	}

    public boolean convertToBoolean(String input) {
        Boolean result;
        Set<String> trueValues = new HashSet<>(Arrays.asList("true", "yes", "y", ""));
        Set<String> falseValues = new HashSet<>(Arrays.asList("no", "false", "n"));
    	if (trueValues.contains(input.toLowerCase())) {
    		result = true;
    	} else if (falseValues.contains(input.toLowerCase())) {
            result = false;
        } else {
    		System.err.println("Not sure what that meant\n");
    		result = null;
    	}
    	return result;
    }
	
	public RecordFilterEnum convertToFilter(String input) {
		RecordFilterEnum filter = RecordFilterEnum.findFilter(input);
		if (filter!=null) {
			return filter;
		} else {
			System.out.println("That's not one of the possible filters\n");
			return null;
		}
	}
	
	protected String readLine(String arg) throws IOException {
		if (System.console() != null && !Boolean.valueOf(System.getProperty("runInEclipse"))) {
			String input = System.console().readLine(arg);
			if (!quitting(input)) {
				return input;
			} else {
				System.exit(0);
				return null;
			}
		}
		
		//for debugging in Eclipse
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(arg);
		String input = reader.readLine();
		if (!quitting(input)) {
			return input;
		} else {
			System.exit(0);
			return null;
		}
	}
	
	public boolean quitting(String input) {
        return input.equalsIgnoreCase("quit");
	}
}
