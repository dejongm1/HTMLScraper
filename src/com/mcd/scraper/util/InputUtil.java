package com.mcd.scraper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;

import com.mcd.scraper.entities.State;

public class InputUtil {
	
	public InputUtil() {}

	public Object getInput(String prompt, int numberOfTries, String validationType) throws IOException {
		for(int t=1;t <= numberOfTries; t++){
			if (validationType!= null && validationType.equals(ScraperConstants.URL_VALIDATION)) {
				String url = readLine(prompt);
				url = !url.startsWith("http")?"http://"+url:url;
				if (validURL(url)){
					return url;
				} else {
					System.out.println("That's not a valid url\n");
				}
			} else if (validationType!= null && validationType.equals(ScraperConstants.NUMBER_VALIDATION)) {
				try {
					int number = Integer.parseInt(readLine(prompt));
					return number;
				} catch (NumberFormatException nfe) {
					System.out.println("That's not a number\n");
				}
			} else if (validationType!= null && validationType.equals(ScraperConstants.STATE_VALIDATION)) {
				String input = readLine(prompt);
				List<State> states = new ArrayList<>();
				if (input.contains(",")) {
					String[] inputSplit = input.split("\\s*,\\s*");
					for (String inputPiece : inputSplit) {
						State state = State.getState(inputPiece);
						if (state!= null) {
							states.add(state);
						} else {
							System.out.println(inputPiece + " is not an American state");
						}
					}
				} else {
					states = State.confirmState(input);	
				}
				if (states!=null && !states.isEmpty()) {
					return states;
				} else {
					System.out.println("That's not an American state\n");
				}
			} else {
				return readLine(prompt);
			}
		}
		System.exit(0);
		return null;
	}
	
	protected boolean validURL(String url) throws IOException {
		String[] schemes = {"http","https"};
	    UrlValidator urlValidator = new UrlValidator(schemes);
	    return urlValidator.isValid(url);
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
