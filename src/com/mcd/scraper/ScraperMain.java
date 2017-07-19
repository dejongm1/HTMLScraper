package com.mcd.scraper;

import com.mcd.scraper.entities.State;
import com.mcd.scraper.util.InputUtil;
import com.mcd.scraper.util.ScraperConstants;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author U569220
 *
 */
public class ScraperMain {

	private static final Logger logger = Logger.getLogger(ScraperMain.class);
	private static InputUtil inputUtil;
	private static ScrapingEngine engine;
	
	private static String prompt;
	
	private ScraperMain(){}

	public static void  main(String[] args) throws IOException {
		logger.info("Application started");
		inputUtil = new InputUtil();
		engine = new ScrapingEngine();
		
		if (prompt==null) {
			prompt = "What do you want to do?\n "
						+ "\t 1 - Get words by frequency\n "
						+ "\t 2 - Scrape for text\n "
						+ "\t 3 - Search for a term\n "
						+ "\t 4 - Get arrest records\n ";
		}
		String scrapeTypeChoice = "";
		
		if (args.length==0) {
			scrapeTypeChoice = (String) inputUtil.getInput(prompt, 3, "");
		} else if (args.length>=1) {
			scrapeTypeChoice = args[0];
		}
		try {
			if (scrapeTypeChoice.toLowerCase().contains("frequen")
					|| scrapeTypeChoice.toLowerCase().contains("words")
					|| scrapeTypeChoice.equals("1")) {
				getPopularWords(args);
			} else if (scrapeTypeChoice.toLowerCase().contains("text")
					|| scrapeTypeChoice.toLowerCase().contains("scrap")
					|| scrapeTypeChoice.equals("2")) {
				getTextBySelector(args);
			} else if (scrapeTypeChoice.toLowerCase().contains("search")
					|| scrapeTypeChoice.toLowerCase().contains("term")
					|| scrapeTypeChoice.equals("3")) {
				getSearchTerms(args);
			} else if (scrapeTypeChoice.toLowerCase().contains("arrest")
					|| scrapeTypeChoice.toLowerCase().contains("record")
					|| scrapeTypeChoice.equals("4")) {
				getArrestRecords(args);
			} else if (inputUtil.quitting(scrapeTypeChoice)) {
				System.exit(0);
			} else {
				prompt = "I'm not sure what you want me to do. Type \"quit\" if you changed your mind. \n" + prompt;
				main(new String[] {});
			}
		} catch (IOException ioe) {
			System.err.println("Dunno what you did but I don't like it. I quit.");
			System.exit(0);
		} catch (NullPointerException npe) {
		    //Can i start over here when i catch exceptions? just pass something to start where it left off? Maybe count number of failures before giving up
			//Catch states that haven't been coded yet
			prompt = "I didn't understand this parameter, please try again. Type \"quit\" if you changed your mind. \n" + prompt;
			main(new String[] {});
		}
	}

	private static void getPopularWords(String[] args) throws IOException {
		String url = args.length>=2?inputUtil.convertToUrl(args[1]):(String) inputUtil.getInput("URL: ", 3, ScraperConstants.URL_VALIDATION);
		int numberOfWords = args.length>=3?inputUtil.convertToNumber(args[2]):(int) inputUtil.getInput("Number of words: ", 3, ScraperConstants.NUMBER_VALIDATION);
		engine.getPopularWords(url, numberOfWords);
	}
	
	private static void getTextBySelector(String[] args) throws IOException {
		String url = args.length>=2?inputUtil.convertToUrl(args[1]):(String) inputUtil.getInput("URL: ", 3, ScraperConstants.URL_VALIDATION);
		String selector = args.length>=3?args[2]:(String) inputUtil.getInput("Selector(s): ", 1, ScraperConstants.NO_VALIDATION);
		engine.getTextBySelector(url, selector);
	}
	
	private static void getSearchTerms(String[] args) throws IOException {
//		String url = (String) inputUtil.getInput("URL: ", 3, ScraperConstants.URL_VALIDATION);
//		String term = (String) inputUtil.getInput("Term: ", 1, ScraperConstants.NO_VALIDATION);
//		engine.getSearchTerms(url, term);
	}
	
	@SuppressWarnings("unchecked")
	private static void getArrestRecords(String[] args) throws IOException {
		List<State> states = args.length>=2?inputUtil.convertToStates(args[1]):(List<State>) inputUtil.getInput("State(s) or \"All\": ", 3, ScraperConstants.STATE_VALIDATION);
		long maxNumberOfResults = args.length>=3?inputUtil.convertToNumber(args[2]):999999;
		engine.getArrestRecords(states, maxNumberOfResults);
	}
}
