package com.mcd.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * 
 * @author U569220
 *
 */
public class ScrapingEngine {



	protected void getPopularWords(String url, int numberOfWords) {

	}

	protected void getTextBySelector(String url, String selector) {

	}

	protected String validateURL(String url) throws IOException {
		String validUrl = "";
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (!urlValidator.isValid(url)) {
			validUrl = readLine("Please enter a valid URL, including protocol (http://, https://, etc)): ");
			if (!urlValidator.isValid(validUrl)) {
				validUrl = readLine("Still no good. Try again or I'm kicking you out: ");
				if (!urlValidator.isValid(validUrl)) {
					System.exit(0);
				}
			}
		} 
		return validUrl;
	}

	protected int validateNumber(String numberOfWordsString) throws IOException {
		int numberOfWords = 0;
		try {
			numberOfWords = Integer.valueOf(numberOfWordsString);
		} catch (NumberFormatException nfe) {
			try {
				numberOfWords = Integer.valueOf(readLine("Please enter a enter a number between 1 and 100: "));
			} catch (NumberFormatException nfe2) {
				try {
					numberOfWords = Integer.valueOf(readLine("Last chance. Enter a number or you're just gonna get 5. "));
				} catch (NumberFormatException nfe3) {
					numberOfWords = 5;
				}
			}
		}
		return numberOfWords;
	}

	protected String readLine(String arg) throws IOException {
		if (System.console() != null) {
			return System.console().readLine(arg);
		}
		System.out.print(arg);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		return reader.readLine();
	}

}
