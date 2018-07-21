package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScriptDataHsqlDbTest {

	private AbstractScriptData scriptData = new ScriptDataHsqlDb("jdbc:hsqldb:mem:memdb", "SA", null);
	
	@Before
	public void setupDb() throws ClassNotFoundException, SQLException {
		try (Connection con = scriptData.getConnection();
				Statement stmt = con.createStatement()) {
			
			stmt.executeUpdate("CREATE TABLE Customer(ID INTEGER PRIMARY KEY, FirstName VARCHAR(20), LastName VARCHAR(30), Street VARCHAR(50), City VARCHAR(25))");
			stmt.executeUpdate("INSERT INTO Customer VALUES(0,'Laura','Steel','429 Seventh Av.','Dallas')");
			
			stmt.executeUpdate("CREATE TABLE Invoice(ID INTEGER PRIMARY KEY, CustomerID INTEGER, Total DECIMAL, FOREIGN KEY (CustomerId) REFERENCES Customer(ID) ON DELETE CASCADE)");
			stmt.executeUpdate("INSERT INTO Invoice VALUES(0,0,0.0)");
			
			stmt.executeUpdate("CREATE TABLE Product(ID INTEGER PRIMARY KEY, Name VARCHAR(30), Price DECIMAL)");
			stmt.executeUpdate("INSERT INTO Product VALUES(0,'Iron Iron',54)");
			stmt.executeUpdate("INSERT INTO Product VALUES(1,'Chair Shoe',248)");
			stmt.executeUpdate("INSERT INTO Product VALUES(2,'Telephone Clock',248)");
			stmt.executeUpdate("INSERT INTO Product VALUES(3,'Chair Chair',254)");
			stmt.executeUpdate("INSERT INTO Product VALUES(7,'Telephone Shoe',84)");
			stmt.executeUpdate("INSERT INTO Product VALUES(14,'Telephone Iron',124)");
			stmt.executeUpdate("INSERT INTO Product VALUES(47,'Ice Tea Iron',178)");
			
			stmt.executeUpdate("CREATE TABLE Item(ID INTEGER PRIMARY KEY, InvoiceID INTEGER, Item INTEGER, ProductID INTEGER, Quantity INTEGER, Cost DECIMAL, UNIQUE (InvoiceID, Item), FOREIGN KEY (InvoiceId) REFERENCES Invoice (ID) ON DELETE CASCADE, FOREIGN KEY (ProductId) REFERENCES Product(ID) ON DELETE CASCADE)");
			stmt.executeUpdate("INSERT INTO Item VALUES(0,0,2,47,3,1.5)");
			stmt.executeUpdate("INSERT INTO Item VALUES(1,0,1,14,19,1.5)");
			stmt.executeUpdate("INSERT INTO Item VALUES(2,0,0,7,12,1.5)");
			
			stmt.executeUpdate("UPDATE Item SET Cost = Cost * (SELECT Price FROM Product prod WHERE ProductID=prod.ID)");
			stmt.executeUpdate("UPDATE Invoice SET Total = (SELECT SUM(Cost*Quantity) FROM Item WHERE InvoiceID=Invoice.ID)");
		}
	}
	
	@After
	public void shutdownDb() throws ClassNotFoundException, SQLException {
		try (Connection con = scriptData.getConnection();
				Statement stmt = con.createStatement()) {
			
			stmt.executeUpdate("SHUTDOWN");
		}
	}
	
	@Test
	public void loadPrimaryKey() {
		scriptData.loadPrimaryKey();
	}
	
	@Test
	public void loadForeignKey() {
		scriptData.loadForeignKey();
	}
	
	@Test
	public void scriptData() {
		
		scriptData.loadPrimaryKey();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		PrintStream outStream = new PrintStream(out);
		scriptData.script(list, outStream);
		
		assertThat(out.toString(), CoreMatchers.containsString("-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO CUSTOMER (ID, FIRSTNAME, LASTNAME, STREET, CITY) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');"));
		
		assertThat(out.toString(), CoreMatchers.containsString("-- SELECT * FROM PRODUCT WHERE ID IN (7, 14, 47) ORDER BY ID;"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (7, 'Telephone Shoe', 84);"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (14, 'Telephone Iron', 124);"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (47, 'Ice Tea Iron', 178);"));
		
		assertThat(out.toString(), CoreMatchers.containsString("-- SELECT * FROM INVOICE WHERE ID IN (0) ORDER BY ID;"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO INVOICE (ID, CUSTOMERID, TOTAL) VALUES (0, 0, 3898);"));
		
		assertThat(out.toString(), CoreMatchers.containsString("-- SELECT * FROM ITEM WHERE InvoiceID = 0 ORDER BY ID;"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (0, 0, 2, 47, 3, 178);"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (1, 0, 1, 14, 19, 124);"));
		assertThat(out.toString(), CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (2, 0, 0, 7, 12, 84);"));
	}
}
