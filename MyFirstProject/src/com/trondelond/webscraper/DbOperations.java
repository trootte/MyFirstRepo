package com.trondelond.webscraper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
	
	public static int appendSiteToDB(Connection conn, String url) {
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
					System.out.print("appendSiteToDB : No site found, insert!");
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
				
				pS.close();
			}
			
			//Find id of added site
			sql = "SELECT sites.id " + 
			"FROM sites " +
			"WHERE sites.url = '" + url + "'";
			
			stmt.executeQuery(sql);
			
			rs = stmt.getResultSet();
			
			if (rs.next()) siteId = rs.getInt(1);

			System.out.println("appendSiteToDB : Site id = " + siteId);
			
			return siteId;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.out.println("appendSiteToDB : Site id = " + siteId);
			
			return siteId;
		}
	}
	
	
	public String getScriptFromDB(String dataBaseName, int siteId){
		System.out.println("getScriptFromDB : Start!" );
		
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
			System.out.println("getScriptFromDB : Finished!" );
			return sb.toString();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.out.println("getScriptFromDB : Site id = " + siteId);
			
			return null;
		}
	}
	
	public String runQuerySingleValue(String sql) {
		//Find id of added site
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
		/*
		String escapedHtml = readerLine.replace("\"", "x");
		escapedHtml = escapedHtml.replace("<", "y");
		escapedHtml = escapedHtml.replace(">", "z");
		*/
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
}
