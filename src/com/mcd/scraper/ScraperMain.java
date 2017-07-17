package com.mcd.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mcd.scraper.entities.State;
import com.mcd.scraper.util.HTMLScraperConstants;
import com.mcd.scraper.util.InputUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @author U569220
 *
 */
public class ScraperMain {

	private ScraperMain(){}

	@SuppressWarnings("unchecked")
	public static void  main(String[] args) throws IOException {
		ScrapingEngine engine = new ScrapingEngine();
		InputUtil inputUtil = new InputUtil();
		
		//accept parameters to bypass the separate choices?
		String prompt = args.length==0?"What do you want to do?\n "
										+ "1 - Get words by frequency\n "
										+ "2 - Scrape for text\n "
										+ "3 - Search for a term\n "
										+ "4 - Arrest Records\n ":args[0];
		try {
			String choice = (String) inputUtil.getInput(prompt, 3, "");
			if (choice.toLowerCase().contains("frequen")
					|| choice.toLowerCase().contains("words")
					|| choice.equals("1")) {
				String url = (String) inputUtil.getInput("URL: ", 3, HTMLScraperConstants.URL_VALIDATION);
				int numberOfWords = (int) inputUtil.getInput("Number of words: ", 3, HTMLScraperConstants.NUMBER_VALIDATION);
				engine.getPopularWords(url, numberOfWords);
			} else if (choice.toLowerCase().contains("text")
					|| choice.toLowerCase().contains("scrape")
					|| choice.equals("2")) {
				String url = (String) inputUtil.getInput("URL: ", 3, HTMLScraperConstants.URL_VALIDATION);
				String selector = (String) inputUtil.getInput("Selector(s): ", 1, HTMLScraperConstants.NO_VALIDATION);
				engine.getTextBySelector(url, selector);
			} else if (choice.toLowerCase().contains("search")
					|| choice.toLowerCase().contains("term")
					|| choice.equals("3")) {
//				String url = engine.getInput("URL: ", 3, HTMLScraperConstants.URL_VALIDATION);
//				String term = engine.readLine("Term: ", 1, HTMLScraperConstants.NO_VALIDATION);
//				engine.getSearchTerms(url, term);
			} else if (choice.toLowerCase().contains("arrest")
					|| choice.toLowerCase().contains("record")
					|| choice.equals("4")) {
				List<State> states = (List<State>) inputUtil.getInput("State(s) or \"All\": ", 3, HTMLScraperConstants.STATE_VALIDATION);
				engine.getRecords(states);
			} else if (inputUtil.quitting(choice)) {
				System.exit(0);
			} else {
				main(new String[] {"I'm not sure what you want me to do. Type \"quit\" if you changed your mind. \n" + prompt});
			}
		}
		catch (IOException ioe) {
			System.err.println("Dunno what you did but I don't like it. I quit.");
			System.exit(0);
		}
	}

	public static void  mainSingle(String[] args) throws IOException {
		/** args[0] - method to call
		 *  
		 *  args[1] - tags to delimit by
		 *  args[2] - at work (true/false)  **/

		if (args.length == 0) {
			System.out.println("What do you want to do? Get popular words or scrape for text?\n use ");
			System.out.println("You must pass some arguments, separated by spaces, for scraping: ");
			System.out.println("	* First argument is the URL of the page to be parsed.");
			System.out.println("	* Second argument is the html tag(s) to get the text of.");
			System.out.println("	* Third argument is \"true\" only if using offline or behind a firewall.");
			System.out.println("  For example: " );
			System.out.println("	java -jar ScraperMain.jar http://en.wikipedia.org/ \"#mp-itn b a\"");
		} else if (args[0].contains("help")) {
			System.out.println("Usage: ");
			System.out.println("	* First argument is the URL of the page to be parsed.");
			System.out.println("	* Second argument is the html tag(s) to get the text of.");
			System.out.println("	* Third argument is true only if using offline or behind a firewall.");
			System.out.println("For example: " );
			System.out.println("	java -jar ScraperMain.jar https://www.intoxalock.com/iowa/installation-locations \"#dnn_dnnInstalationCenters_instalationLocationList .info-block\" true");
		} else {
			String htmlLocation = "";
			try {
				Document doc;
				if (args.length>=4 && Boolean.valueOf(args[3])) {
					switch (args[1]) {
						case "https://www.intoxalock.com/" : 									htmlLocation = "intoxalock-homepage.html";
																								break;
						case "https://en.wikipedia.org/" : 										htmlLocation = "wikipedia-homepage.html";
																								break;
						case "https://www.intoxalock.com/iowa/installation-locations" : 		htmlLocation = "intoxalock-iowa-locations.html";
																								break;
						default : 																htmlLocation = "";
																								break;
					}
					File input = new File("htmls/" + htmlLocation);
					doc = Jsoup.parse(input, "UTF-8", args[1]);
	
				} else {
					doc = Jsoup.connect(args[1]).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36").get();
				}
				Elements tags = doc.select(args[2]);
				for (Element tag : tags) {
					System.out.println(tag.text());
				}
	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
