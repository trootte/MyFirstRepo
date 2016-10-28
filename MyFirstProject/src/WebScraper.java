import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class WebScraper {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub test
		
		String url = "http://vg.no";
		
		String returnValue = scrapeWebPage(url);
		System.out.println("Start...");
		System.out.println(returnValue);
		System.out.println("End...");
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
}
