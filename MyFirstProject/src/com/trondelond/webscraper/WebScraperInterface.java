/**
 * 
 */
package com.trondelond.webscraper;

import java.util.List;

/**
 * @author Trond
 *
 */
public interface WebScraperInterface {
		public String scrapeWebPage(String URL);
		
		public String getWebPageFromDb(String URL);
		
		public List<String> getScrapedUrls();
}
