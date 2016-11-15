package com.trondelond.webscraper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbOperations {	
	
	private Connection conn;
	
	
	DbOperations(String dataBaseName){
		try {
			// initialize connection
			conn = DriverManager.getConnection("jdbc:mysql://localhost/sys?" +
                    "user=scrape_user&password=scrape_pwd");
			
		} catch (SQLException ex) {		   
			System.out.println("getConnection() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	DbOperations (String dataBaseName, String[] tableArray) {
		try {
			// initialize connection
			conn = DriverManager.getConnection("jdbc:mysql://localhost/sys?" +
                    "user=scrape_user&password=scrape_pwd");
			
			createDbIfNotExists(conn, dataBaseName);
			createTablesIfNotExists(conn, tableArray);
			
		} catch (SQLException ex) {
		    System.out.println("getConnection() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void setDb(String dataBaseName){
		System.out.println("setDb : dataBaseName : " + dataBaseName);
		try {
			Statement stmt = conn.createStatement();
			stmt.executeQuery("USE " + dataBaseName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public boolean createDbIfNotExists(Connection conn, String dataBaseName) {
		
		Statement stmt;
		String sql = "";
		try {
		    sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = \"" + dataBaseName + "\"";
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
		    ResultSet rs = stmt.getResultSet();
		    
		    if (!rs.next()) {
		    	//Create database
		    	System.out.println("createDbIfNotExists : No database found!");
		    	
		    	sql = "CREATE SCHEMA `" + dataBaseName + "` DEFAULT CHARACTER SET latin1 COLLATE latin1_danish_ci ;"; 
		    	stmt.execute(sql);
		    	
		    	System.out.println("createDbIfNotExists - Create database!");
		    	
		    	//Set schema
		    	sql = "USE " + dataBaseName;
		    	stmt.execute(sql);
		    }
		    else {
		    	System.out.println("createDbIfNotExists - Database exists!");
		    	
		    	sql = "USE " + dataBaseName;
				stmt = conn.createStatement();
				stmt.execute(sql);
		    }
		    rs.close();
		    stmt.close();
		} catch (SQLException ex) {
		    // handle any errors
			System.out.println("createDbIfNotExists() Error");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return true;
	}
	
	public boolean createTablesIfNotExists (Connection conn, String[] tableArray) {
		//TODO legg statements i filer/config
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT Database()";
			String dataBaseName = "";
			
		    stmt.executeQuery(sql);
			ResultSet rs = stmt.getResultSet();
			
			if (rs.next()){
				//Fetch databasename
				//rs.next();
				dataBaseName = rs.getString(1);
				System.out.println("createTablesIfNotExists database name : " + dataBaseName);
			}
			else {
				System.out.println("createTablesIfNotExists no database found : " + dataBaseName);				
			}
			
			//loop table array and create tables
			for (int i = 0; i < tableArray.length; i++) {
				sql = "SELECT IF (EXISTS (" +
						"select 1 from information_schema.TABLES where table_schema = \"" + dataBaseName + "\" and table_name = \"" + tableArray[i].toString() + "\"),1 ,0)";
				
				stmt.execute(sql);
				
				rs = stmt.getResultSet();
				
				while (rs.next()) {
					if (rs.getString(1).equals("0")) {						
						switch (tableArray[i]) {
							case "sites" :
								sql = "CREATE TABLE `" + dataBaseName + "`.`Sites` (" +
										  "`id` INT NOT NULL AUTO_INCREMENT," +
										  "`url` VARCHAR(45) NOT NULL," +
										  "`https` VARCHAR(45) NOT NULL," +
										  "PRIMARY KEY (`id`)," +
										  "UNIQUE INDEX `id_UNIQUE` (`id` ASC))";
								break;
							case "lines" : 
								sql = "CREATE TABLE `" + dataBaseName + "`.`Lines` (" +
										"`id` INT NOT NULL AUTO_INCREMENT," +
										"`siteId` INT NOT NULL," +
										"`text` BLOB NULL," +
										"PRIMARY KEY (`id`)," +
										"UNIQUE INDEX `id_UNIQUE` (`id` ASC));";
								break;
							default : sql = "";
						}
						System.out.println("createTablesIfNotExists : table " + tableArray[i].toString() + " created!");
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
	
	public int appendSiteToDB(String url) {
		System.out.println("appendSiteToDB : Start!" );
		
		String sql = "SELECT IF (EXISTS (" +
				"SELECT 1 FROM Sites " + 
				"WHERE url = \"" + url + "\"),1 ,0)";
		Statement stmt;
		int siteId = -1;
		ResultSet rs;
		
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
			rs = stmt.getResultSet();
			if (rs.next()) {
				if (rs.getString(1).equals("0")) {	
					//No Site registered, insert into DB
					System.out.println("appendSiteToDB : No site found, insert!");
					sql = "INSERT INTO Sites (url, https) " +
					"VALUES ('" + url + "','1')";
					
					stmt.execute(sql);
				}				
			}
			else {
				System.out.println("appendSiteToDB : Site found, deleting existing data!");
				
				//Find databasename
				PreparedStatement pS = conn.prepareStatement("SELECT Database()");
				rs = pS.executeQuery();
				
				if (rs != null) rs.next();
				
				String dataBaseName = rs.getString(1);
				
				//Site found, remove linked data
				pS = conn.prepareStatement("DELETE FROM " + dataBaseName + ".Lines WHERE siteId = " + siteId);
				pS.executeQuery();
				rs.close();
				pS.close();
			}
			
			//Find id of added site
			sql = "SELECT sites.id " + 
			"FROM sites " +
			"WHERE sites.url = '" + url + "'";
			
			stmt.executeQuery(sql);
			rs = stmt.getResultSet();
			if (rs.next()) siteId = rs.getInt(1);
			rs.close();
			
			System.out.println("appendSiteToDB : Site id = " + siteId);
			
			return siteId;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.out.println("appendSiteToDB : Site id = " + siteId);
			
			return siteId;
		}
	}
	
	public int getSiteId(String url){
		//Find id of added site
		int siteId;
		//Find databasename
		PreparedStatement pS;
		
		try {
			pS = conn.prepareStatement("use webscraper");
			pS.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			pS = conn.prepareStatement("SELECT sites.id FROM sites WHERE sites.url LIKE ?");
			pS.setString(1, "%" + url + "%");
			ResultSet rs = pS.executeQuery();
			if (rs.next()) {
				siteId = rs.getInt(1);
			}
			else {
				siteId = -1;
			}
			rs.close();
			pS.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			siteId = -1;
		}
		
		return siteId;
	}
	
	public String getScriptFromDB(String dataBaseName, int siteId){
		StringBuilder sb = new StringBuilder();
		PreparedStatement pS;
		String newLine = "\n";
		try {
			pS = conn.prepareStatement("SELECT text FROM " + dataBaseName + ".Lines WHERE siteId = ?");
			pS.setInt(1, siteId);
			ResultSet rs = pS.executeQuery();			
			boolean found = false;
			
			while (rs.next()){
				if (rs.getString(1) == null) continue; //Some NULL values in Db
				
				if (rs.getString(1).toLowerCase().contains("<script>")) found = true;

				if (found) {
					sb.append(rs.getString(1));
					sb.append(newLine);
				}
				
				if (found && rs.getString(1).toLowerCase().contains("</script>")) found = false;

			}
			rs.close();
			return sb.toString();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.out.println("getScriptFromDB : Site id = " + siteId);
			
			return null;
		}
	}
	
	public String runQuerySingleValue(String sql) {
		Statement stmt;
		ResultSet rs;
		String result;
		
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
			rs = stmt.getResultSet();
			if (rs.next()) {
				result = rs.getString(1);
			}
			else result = "";
			stmt.close();
			rs.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public boolean appendLineToDB(String readerLine, int siteId) {
		if (readerLine == null) readerLine = "";
		
		try {
			PreparedStatement pS = conn.prepareStatement("INSERT INTO webscraper.lines (siteId, text) VALUES (?, ?)");
			pS.setInt(1, siteId);
			pS.setString(2, readerLine);
			pS.executeUpdate();			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public void removeLinesFromSite(String dataBaseName, int siteId){
		Statement stmt;
		String sql = "DELETE FROM " + dataBaseName + ".lines WHERE lines.siteId = " + siteId;
		
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> doGetScrapedUrls(String dataBaseName){
		Statement stmt;
		ResultSet rs;
		List<String> urlList = new ArrayList<String>();
		
		String sql = "SELECT sites.url FROM " + dataBaseName + ".sites ORDER BY sites.url";
		urlList.add("(scrape new webpage)");
		
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
			rs = stmt.getResultSet();
			while (rs.next()) {
				urlList.add(rs.getString(1));
			}
			stmt.close();
			rs.close();
			return urlList;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			urlList.add("(No sites scraped)");
			return urlList;
		}
	}
}
