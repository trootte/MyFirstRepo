package com.trondelond.webscraper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
//import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class WebScraper implements WebScraperInterface{

	public static String dataBaseName; 
	//private static String url;
	private static String[] tableArray;
	private static DbOperations DbOp;
	//private static Connection conn;
	private static int siteId;
	
	public static void main(String[] args) throws IOException {
		System.out.println("Main Start");
		/*
		if (args.length > 0) {
			if (args[0].equals("1")) {
				System.out.println("Main : write config!");
				writeConfig();
			}
		}
		
		DbOp = new DbOperations(dataBaseName, tableArray);
		
		if (readConfig()) {
			
			conn = DbOp.getConnection();
			
			if(DbOperations.appendSiteToDB(conn, url) == -1) {
				System.out.println("Main : Site retrieval error.");
			}
			
			String message = doScrapeWebPage(url);
			System.out.println("Main : scrapeWebPageMessage = " + message);
		}
		else {
			System.out.println("main feil");
		}
		
		String script = getScript();
		System.out.println(script);
		*/
	}
	
	public WebScraper(){
		writeConfig();
		readConfig();
		
		//bOperations 
		DbOp = new DbOperations(dataBaseName);
		DbOp.setDb(dataBaseName);
	}
	
	public String getWebPageFromDb(String Url) {
		//DbOperations DbOpz = new DbOperations(dataBaseName);
			 
		int siteId = DbOp.getSiteId(Url);
		if (siteId < 0) {
			return "getWebPageFromDb error - site Id not found!";
		}
		else {
			return getScript(siteId);	
		}
	}
	
	public String scrapeWebPage(String Url){
		return doScrapeWebPage(Url);		
	}
	
	public List<String> getScrapedUrls(){
		
		return DbOp.doGetScrapedUrls(dataBaseName); 
	}
	private static String doScrapeWebPage(String url) {
		String message = "OK";
		
		siteId = DbOp.getSiteId(url);
		
		if (siteId < 0) {
			System.out.println("doScrapeWebPage : no such url scraped, create new entry.");
			siteId = DbOp.appendSiteToDB(url);
		}
		else {
			System.out.println("doScrapeWebPage : site exists, remove old lines...");
			DbOp.removeLinesFromSite(dataBaseName, siteId);
		}
		
		System.out.println("scrapeWebPage : siteId = " + siteId);
		try {
			URL myUrl = new URL(url);
			InputStream in = myUrl.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			readLines(reader);
			}
		catch (MalformedURLException e1)
		{
			message = "Malformed URL Exception : " + e1.toString();			
		} catch (IOException e2) {
			message = e2.toString();
		}
		
		return message;
	}
	
	private static boolean readLines(BufferedReader inReader) {
		try {
			while (inReader.readLine() != null) {
				if (!DbOp.appendLineToDB(inReader.readLine(), siteId)) break;
			}
		}
		catch (Exception e) {
			System.out.println("Feil! : " + e.getMessage());
			return false;
		}
		return true;
	}

	private static String getScript(int siteId){
		return DbOp.getScriptFromDB(dataBaseName, siteId);
	}
	
	private boolean readConfig() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			try {
				input = new FileInputStream("config.properties");
			}
			catch (Exception e) {
				System.out.println("readConfig feil : " + e.getMessage());
				return false;
			}

			prop.load(input);
			
			dataBaseName = prop.getProperty("dataBaseName");
			//url = prop.getProperty("url");
			
			// how many properties with tablenames? -> initialize tableArray
			int j = 0;
			Enumeration<?> e = prop.propertyNames();
			
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				if (key.startsWith("table")) j++;	
				}
			
			tableArray = new String[j];
			
			//fetch tablenames
			int i = 1;
						
			String propertyTable = "table" + i;
			String configTableName = prop.getProperty(propertyTable);
			
			do {
				tableArray[i-1] = configTableName;
				i++;
				propertyTable = "table" + i;
				configTableName = prop.getProperty(propertyTable);
			}
			while (configTableName != null);
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	private static void writeConfig() {
		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream("config.properties");

			prop.setProperty("dataBaseName", "WebScraper");
			prop.setProperty("url", "http://vg.no");
			prop.setProperty("table1", "sites");
			prop.setProperty("table2", "lines");
			
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
	