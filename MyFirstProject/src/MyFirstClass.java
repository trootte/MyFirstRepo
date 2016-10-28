import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

public class MyFirstClass {

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
			
			//Stream<String> noOfLines = new Stream<String>();
			Stream<String> lines = reader.lines();
			
			/*
			for each (String string in lines) {
				
			}
			*/
			message = reader.readLine();
			
		}
		catch (MalformedURLException e1)
		{
			message = "Malformed URL Exception : " + e1.toString();
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			message = e2.toString();
		}
		
		return message;
		/*
		finally {
			if (message != "") {
			return message;
			}
			else return html;
		}
		*/
	}
	
	public static boolean appendLineToDB() {
		return true;
	}
}
