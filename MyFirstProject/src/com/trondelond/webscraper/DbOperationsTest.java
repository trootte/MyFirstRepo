package com.trondelond.webscraper;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
//import com.trondelond.*;

public class DbOperationsTest {
	
	final String DBNAME = "webscraper";
	final String URL = "http://vg.no";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void appendSiteToDBReturnsSite() {
		//fail("Not yet implemented");
		//DbOperations DbOp = new DbOperations();
		DbOperations DbOp = new DbOperations(DBNAME);
		assertEquals(1, DbOp.appendSiteToDB(URL));		
	}

}
