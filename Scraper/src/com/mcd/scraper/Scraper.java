package com.mcd.scraper;

import java.io.Console;
import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author U569220
 *
 */
public class Scraper {

	public static void  main(String[] args) throws IOException {
		
		Console console = System.console();
		if (console == null) {
            System.err.println("No console.");
            System.exit(1);
        }
		ScrapingEngine engine = new ScrapingEngine();
		
		String scrapingType = console.readLine("What do you want to do? Get popular words or scrape for text?");
		if (scrapingType.toLowerCase().contains("popular")) {
			String url = console.readLine("URL: ");
			int numberOfWords = 0;
			try {
				numberOfWords = Integer.valueOf(console.readLine("Number of words: "));
			} catch (Exception e) {
				try {
					numberOfWords = Integer.valueOf(console.readLine("Please enter a enter between 1 and 100: "));
				} catch (Exception e2) {
					console.writer().println("Since you don't want to listen, I'll give you 5 words");
					numberOfWords = 5;
				}
			}
			engine.getPopularWords(url, numberOfWords);
		} else if (scrapingType.toLowerCase().contains("text")) {
			String url = console.readLine("URL: ");
			String selector = console.readLine("selector: ");
			
			engine.getTextBySelector(url, selector);
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
			System.out.println("	java -jar Scraper.jar http://en.wikipedia.org/ \"#mp-itn b a\"");
		} else if (args[0].contains("help")) {
			System.out.println("Usage: ");
			System.out.println("	* First argument is the URL of the page to be parsed.");
			System.out.println("	* Second argument is the html tag(s) to get the text of.");
			System.out.println("	* Third argument is true only if using offline or behind a firewall.");
			System.out.println("For example: " );
			System.out.println("	java -jar Scraper.jar https://www.intoxalock.com/iowa/installation-locations \"#dnn_dnnInstalationCenters_instalationLocationList .info-block\" true");
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
