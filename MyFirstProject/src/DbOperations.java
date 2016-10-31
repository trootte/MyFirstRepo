import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbOperations {	
	
	private Connection conn;
	
	
	DbOperations (String dataBaseName, String[] tableArray) {
		try {
			// initialize connection
			conn = DriverManager.getConnection("jdbc:mysql://localhost/sys?" +
                    "user=scrape_user&password=scrape_pwd");
			
			try {
				conn.setSchema(dataBaseName);
				if (conn.getSchema() == null) conn.setSchema("sys");
				System.out.println("DbOperations Construct getschema : " + conn.getSchema());
			}
			catch (SQLException e){
				System.out.print("DbOperations Construct SqlException : " + e.getMessage()); 
			}
			
			createDbIfNotExists(conn, dataBaseName);
			createTablesIfNotExists(conn, tableArray);
			
		} catch (SQLException ex) {
		    // handle any errors
			System.out.println("getConnection() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public static boolean createDbIfNotExists(Connection conn, String dataBaseName) {
		try {
		    String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = \"" + dataBaseName + "\"";
		    
		    Statement stmt = conn.createStatement();
		    stmt.executeQuery(sql);
		    
		    ResultSet rs = stmt.getResultSet();
		    
		    if (!rs.next()) {
		    	sql = "CREATE SCHEMA `" + dataBaseName + "` DEFAULT CHARACTER SET latin1 COLLATE latin1_danish_ci ;"; 
		    	
		    	stmt.execute(sql);
		    	//TODO catch
		    }
		    
		    rs.close();
		    stmt.close();
		    
		    conn.setSchema(dataBaseName);
		    System.out.println("createDb getSchema : " + conn.getSchema());
		    
		} catch (SQLException ex) {
		    // handle any errors
			System.out.println("createDbIfNotExists() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return true;
	}
	
	public static boolean createTablesIfNotExists (Connection conn, String[] tableArray) {
		//TODO legg statements i filer/config
		try {
			Statement stmt = conn.createStatement();
			String sql = "";
			System.out.println("createTables Schema : " + conn.getSchema());
			for (int i = 0; i < tableArray.length; i++) {
				sql = "SELECT IF (EXISTS (" +
						"select 1 from information_schema.TABLES where table_schema = \"" + conn.getSchema() + "\" and table_name = \"" + tableArray[i] + "\"),1 ,0)";
				
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				
				while (rs.next()) {
					if (rs.getString(1).equals("0")) {
						
						switch (tableArray[i]) {
							case "sites" :
								sql = "CREATE TABLE `" + conn.getSchema() + "`.`Sites` (" +
										  "`id` INT NOT NULL AUTO_INCREMENT," +
										  "`url` VARCHAR(45) NOT NULL," +
										  "`https` VARCHAR(45) NOT NULL," +
										  "PRIMARY KEY (`id`)," +
										  "UNIQUE INDEX `id_UNIQUE` (`id` ASC))";
								break;
							case "lines" : 
								sql = "CREATE TABLE `" + conn.getSchema() + "`.`Lines` (" +
										"`id` INT NOT NULL AUTO_INCREMENT," +
										"`siteId` INT NOT NULL," +
										"`text` VARCHAR(255) NULL," +
										"PRIMARY KEY (`id`)," +
										"UNIQUE INDEX `id_UNIQUE` (`id` ASC));";
								break;
							default : sql = "";
						}
					}
				}
				
				stmt.execute(sql);
				
			}
						
			stmt.close();
			
		}
		catch (SQLException ex){
		    // handle any errors
			System.out.println("createTablesIfNotExists() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return true;
	}
	
	public static boolean appendSiteToDB(Connection conn) {
		return true;
	}
	
	public static boolean appendLineToDB(String readerLine) {
		System.out.println(readerLine);
		return true;
	}
}
