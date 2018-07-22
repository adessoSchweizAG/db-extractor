package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScriptDataHsqlDbTest {

	private AbstractScriptData scriptData = new ScriptDataHsqlDb("jdbc:hsqldb:mem:memdb", "SA", null);
	
	@Before
	public void setupDb() throws ClassNotFoundException, SQLException {
		try (Connection con = scriptData.getConnection()) {
			runSqlScript(con, ScriptDataHsqlDbTest.class.getResourceAsStream("ScriptDataHsqlDbTest.create.sql"));
			runSqlScript(con, ScriptDataHsqlDbTest.class.getResourceAsStream("ScriptDataHsqlDbTest.data.sql"));
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
	public void scriptData() throws ClassNotFoundException, SQLException {
		
		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		scriptData.script(list, new PrintStream(out));

		String generateScript = out.toString();

		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO CUSTOMER (ID, FIRSTNAME, LASTNAME, STREET, CITY) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');"));
		
		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM PRODUCT WHERE ID IN (7, 14, 47) ORDER BY ID;"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (7, 'Telephone Shoe', 84);"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (14, 'Telephone Iron', 124);"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (47, 'Ice Tea Iron', 178);"));
		
		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM INVOICE WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO INVOICE (ID, CUSTOMERID, TOTAL) VALUES (0, 0, 3898);"));
		
		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM ITEM WHERE InvoiceID = 0 ORDER BY ID;"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (0, 0, 2, 47, 3, 178);"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (1, 0, 1, 14, 19, 124);"));
		assertThat(generateScript, CoreMatchers.containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (2, 0, 0, 7, 12, 84);"));
	}

	@Test
	public void scriptDataAndRun() throws ClassNotFoundException, SQLException {
		
		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		scriptData.script(list, new PrintStream(out));
	
		try (Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:memdbTest", "SA", null);
				Statement stmt = con.createStatement()) {

			runSqlScript(con, ScriptDataHsqlDbTest.class.getResourceAsStream("ScriptDataHsqlDbTest.create.sql"));
			runSqlScript(con, new ByteArrayInputStream(out.toByteArray()));
			stmt.executeUpdate("SHUTDOWN");
		}
	}

	private static void runSqlScript(Connection con, InputStream stream) throws SQLException {

		Pattern pattern = Pattern.compile("(?:;(?:\\r|\\n)+)|(?:--.*(?:\\r|\\n)+)");
		try (Statement stmt = con.createStatement()) {
			for (Scanner s = new Scanner(stream).useDelimiter(pattern); s.hasNext();) {

				String sql = s.next().trim();
				if (sql.length() > 0) {
					stmt.executeUpdate(sql);
				}
			}
		}
	}
}
