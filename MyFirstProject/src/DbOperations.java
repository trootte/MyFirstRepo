
public class DbOperations {	
	static String databaseName;
	
	DbOperations (String dbName) {
		databaseName = dbName;
		
		System.out.println(dbName);
	}
	
	public static boolean appendLineToDB(String readerLine) {
		System.out.println(readerLine);
		return true;
	}
	
	public static boolean createDbIfNotExists() {
		System.out.println(databaseName);
		return true;
	}
}
