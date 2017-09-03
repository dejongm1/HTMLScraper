package com.mcd.spider.util;

public class SpiderConstants {
	
	public static final String URL_VALIDATION = "url";
	public static final String NUMBER_VALIDATION = "number";
	public static final String STATE_VALIDATION = "state";
    public static final String FILTER_VALIDATION = "filter";
    public static final String BOOLEAN_VALIDATION = "boolean";
	public static final String NO_VALIDATION = "";
	
	public static final String PROMPT = "What do you want to do?\n "
											+ "\t 1 - Get words by frequency\n "
											+ "\t 2 - Scrape for text\n "
											+ "\t 3 - Search for a term\n "
											+ "\t 4 - Get arrest records\n "
											+ "\t 5 - SEO Audit\n "
								            //+ "\t 6 - Test random connection getter \n"
											;
	public static final String UNKNOWN_COMMAND = "I'm not sure what you want me to do. Type \"quit\" if you changed your mind. \n";
	
	public static final String HELP_MESSAGE_1 = "1 - Get words by frequency: " + "\n"
			+ "  Crawl a webpage of your choice and list the most frequently used words, by popularity" + "\n"
			+ "  URL: The url of the page you want to crawl (http:// will be used unless you specify a protocol" + "\n"
			+ "  Number of words: How many words you want listed" + "\n\n";
	public static final String HELP_MESSAGE_2 = "2 - Scrape for text: " + "\n"
            + "  This will scrape a webpage for text based on the CSS selectors passed" + "\n"
            + "  URL: The url of the page you want to crawl (http:// will be used unless you specify a protocol" + "\n"
            + "  Selector(s): The CSS selector(s) to find and select the text you want. Surround with \" if there is more than one" + "\n"
            + "    e.g. \"body tr .pretty_font\" " + "\n\n";
 public static final String HELP_MESSAGE_3 = "3 - Search for a term: " + "\n"
            + "  Search a webpage for specific word(s) or variations of the word(s)" + "\n"
            + "  URL: The url of the page you want to crawl (http:// will be used unless you specify a protocol" + "\n"
            + "  Word(s): The word(s) you want to search for. Surround with \" if there is more than one" + "\n"
            + "  Flexibility: From 1-3, how flexible should the search be (1 is not flexible at all)" + "\n\n";
 public static final String HELP_MESSAGE_4 = "4 - Get records: " + "\n"
            + "  Scrape records and output the results to a spreadsheet" + "\n"
            + "  State: The state(s) you want records for. Use \"All\" to get every state" + "\n"
            + "  Filter: NOT READY YET. Default for now is arrest records" + "\n"
            + "  Max Number of Results: Put a limit on how many recrds to retrieve in a single run" + "\n"
            + "          Note - Only uncrawled records will be obtained on subseqent runs" + "\n"
            + "  Retrieve uncrawled records: If a list of uncrawled records exists, crawl those alone." + "\n"
            + "          Otherwise, look through all posted records for any missed. \n\n";
 public static final String HELP_MESSAGE_5 = "5 - SEO Audit: " + "\n"
            + "  Perform a customizable SEO audit of an entire website " + "\n"
            + "  All parameters are optional and should be included together, separated by spaces" + "\n"
            + "  -url" + "\n"
            + "   Follow with the domain url of the website you want audited" + "\n"
            + "  -term/-search" + "\n"
            + "   Follow with word(s) you want to search for. Surround with \" if there is more than one" + "\n"
            + "  -performance/-load NOT READY YET" + "\n"
            + "   Flag to include a performance test" + "\n"
            + "  -sleep" + "\n"
            + "   Follow with number of seconds to sleep between each page crawl (for reducing strain on site)" + "\n"
            + "  -full NOT READY YET, default setting right now" + "\n"
            + "   Flag to get a full report instead of lean report. Will overwrite most other optional flags" + "\n"
            + "  -common/-frequent" + "\n"
            + "   Flag to include frequently used words on each page" + "\n"
            + "  -sitemap/-map" + "\n"
            + "   Flag to try to find the sitemap, or generate a new one based off crawling results" + "\n"
            + "   " + "\n"
            + "  MORE TO COME" + "\n\n";
	public static final String HELP_MESSAGE_ALL = HELP_MESSAGE_1 + "\n" +
													HELP_MESSAGE_2 + "\n" +
													HELP_MESSAGE_3 + "\n" +
													HELP_MESSAGE_4 + "\n" +
													HELP_MESSAGE_5 + "\n";
	
}
