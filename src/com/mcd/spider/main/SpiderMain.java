package com.mcd.spider.main;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.mcd.spider.main.engine.SpiderEngine;
import com.mcd.spider.main.entities.audit.AuditParameters;
import com.mcd.spider.main.entities.audit.Term;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.exception.StateNotReadyException;
import com.mcd.spider.main.util.InputUtil;
import com.mcd.spider.main.util.SpiderConstants;

/**
 * 
 * @author U569220
 *
 */

public class SpiderMain {

	private static final Logger logger = Logger.getLogger(SpiderMain.class);
	private static InputUtil inputUtil;
	private static SpiderEngine engine;
	
	private static String prompt;
	
	private SpiderMain(){}

	public static void  main(String[] args) throws IOException {
		logger.info("Application started");
		inputUtil = new InputUtil();
		engine = new SpiderEngine();
		
		if (prompt==null) {
			prompt = SpiderConstants.PROMPT;
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
//            } else if (scrapeTypeChoice.toLowerCase().contains("connect")
//                    || scrapeTypeChoice.toLowerCase().contains("test")
//                    || scrapeTypeChoice.equals("5")) {
//                testConnectionGetter(args);
            } else if (scrapeTypeChoice.toLowerCase().contains("seo")
					|| scrapeTypeChoice.toLowerCase().contains("audit")
					|| scrapeTypeChoice.equals("5")) {
            	if (args.length!=0) {
            		args[0] = ""; //remove first scrapeTypechoice and continue
            	}
				getSEOAudit(String.join(" ", args));
			} else if (inputUtil.quitting(scrapeTypeChoice)) {
				System.exit(0);
			} else {
				prompt = SpiderConstants.UNKNOWN_COMMAND + SpiderConstants.PROMPT;
				main(new String[] {});
			}
		} catch (IOException ioe) {
			System.err.println("Dunno what you did but I don't like it. I quit.");
			System.exit(0);
		} catch (NullPointerException npe) {
			prompt = "I didn't understand this parameter, please try again. Type \"quit\" if you changed your mind. \n" + prompt;
			main(new String[] {});
		} catch (StateNotReadyException snre) {
		    //Can i start over here when i catch exceptions? just pass something to start where it left off? Maybe count number of failures before giving up
			logger.error(snre.getState().getName() + " has not been set up \n" );
			prompt = snre.getState().getName() + " is not ready for scraping yet. Please try another\n" + prompt;
			main(new String[] {"4"});
		} catch (Exception e) {
			logger.error("Exception caught but not handled", e);
		}
	}

	private static void getPopularWords(String[] args) throws IOException {
		String url = args.length>=2?inputUtil.convertToUrl(args[1]):(String) inputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
		int numberOfWords = args.length>=3?inputUtil.convertToNumber(args[2]):(int) inputUtil.getInput("Number of words: ", 3, SpiderConstants.NUMBER_VALIDATION);
		engine.getPopularWords(url, numberOfWords);
	}
	
	private static void getTextBySelector(String[] args) throws IOException {
		String url = args.length>=2?inputUtil.convertToUrl(args[1]):(String) inputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
		String selector = args.length>=3?args[2]:(String) inputUtil.getInput("Selector(s): ", 1, SpiderConstants.NO_VALIDATION);
		engine.getTextBySelector(url, selector);
	}
	
	private static void getSearchTerms(String[] args) throws IOException {
		String url = (String) inputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
		String words = (String) inputUtil.getInput("Words: ", 1, SpiderConstants.NO_VALIDATION);
		engine.search(url, words);
	}

	@SuppressWarnings("unchecked")
	private static void getArrestRecords(String[] args) throws IOException, StateNotReadyException {
		List<State> states = args.length>=2?inputUtil.convertToStates(args[1]):(List<State>) inputUtil.getInput("State(s) or \"All\": ", 3, SpiderConstants.STATE_VALIDATION);
		long maxNumberOfResults = args.length>=3?inputUtil.convertToNumber(args[2]):999999;
		engine.getArrestRecordsByState(states, maxNumberOfResults);
	}

	private static void getSEOAudit(String argString) throws IOException {
		//levels deep
		//output type or location??
		AuditParameters parameters = new AuditParameters(argString);
		engine.performSEOAudit(parameters);
	}

//	private static void testConnectionGetter(String[] args) throws IOException {
//        int numberOfTries = args.length>=2?inputUtil.convertToNumber(args[1]):(int) inputUtil.getInput("Number of connections to make: ", 3, SpiderConstants.NUMBER_VALIDATION);
//        engine.testRandomConnections(numberOfTries);
//    }
}
