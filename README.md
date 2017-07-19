# Scraper

Technical details

VM arguments: 
* -DrunInEclipse=true - used for console bug when testing
* -DrunOffline=true - used for offline testing


### How to use
`command` is a command to be executed in cmd.exe


#####__Open Command Prompt__

* Open start menu, type cmd.exe. Click enter
    
#####__Executing the jar__

* `cd {locationOfApp}`

#######__To follow the prompts__ - `$ java -jar HTMLScraper.jar`

* Choose an option by typing your choice
    * Get words by frequency - `word`, `words`, `frequency`, `frequent`, `1` will all work
        * Choices will be the url and number of words you'd like to get, ordered by decreasing value 
    * Scrape for text - `text`, `scrape`, `scraping`, `2` will all work
        * Choices will be the url and the css selector(s) (surround selectors with double quotes)
    * Search for term(s) - `search`, `term`, `terms`, `3` will all work
        * !!not coded yet!!
    * Get Arrest Records - `arrest`, `record`, `records`, `4` will all work
        * Choice will be state(s) - `IA`, `Iowa`, `IL, IA, TX`, or `All` will work
            
#######__To bypass prompts just include the options in the initial command__

 `java -jar HTMLScraper.jar 1 https://en.wikipedia.org 10` 
 
* This will get the 10 most common words from the homepage of Wikipedia.org
	
 `java -jar HTMLScraper.jar 2 https://en.wikipedia.org "#mw-content-text #mp-dyk li` 
 
* This will get the list of "Did you know"s from the homepage of  Wikipedia.org
	
 `java -jar HTMLScraper.jar 3 https://en.wikipedia.org cheetah` 
 
*  This will search Wikipedia for the fart as well as variations
	
 `java -jar HTMLScraper.jar 4 IA` 
 
* This will get arrest records for the state of IA 

