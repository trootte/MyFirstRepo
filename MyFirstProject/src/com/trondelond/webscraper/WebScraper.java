import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Properties;

public class WebScraper {

	private static String dataBaseName; 
	private static String url;
	private static String[] tableArray;
	
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			if (args[0].equals("1")) {
				System.out.println("Main : write config!");
				writeConfig();
			}
		}
		
		if (readConfig()) {
			DbOperations DbOp = new DbOperations(dataBaseName, tableArray);
			//Connection webScraperConnection = DbOp.getConnection();
			
			//DbOperations.appendSiteToDB(webScraperConnection);
		}
		else {
			System.out.println("main feil");
		}
	}
	
	private static String scrapeWebPage (String url) {
		String message = "";
		
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
	
	private static boolean readConfig() {
		Properties prop = new Properties();
		InputStream input = null;
		boolean success = false;
		try {
			try {
				input = new FileInputStream("config.properties");
			}
			catch (Exception e) {
				System.out.println("readConfig feil : " + e.getMessage());
				success = false;
				return success;
			}

			prop.load(input);
			dataBaseName = prop.getProperty("dataBaseName");
			
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
			success = false;
			return success;
		} finally {
			if (input != null) {
				try {
					input.close();
					success = true;
					return success;
				} catch (IOException e) {
					e.printStackTrace();
					success = false;
					return success;
				}
			}
		}
		success = true;
		return success;
	}
	
	private static boolean readLines(BufferedReader inReader) {
		try {
			while (inReader.readLine() != null) {
				DbOperations.appendLineToDB(inReader.readLine());
			}
		}
		catch (Exception e) {
			System.out.println("Feil! : " + e.getMessage());
			return false;
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
