package ch.adesso.dbextractor.core;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DbSupportMySqlIT {

	private DataSource dataSource;
	private DbSupport dbSupport;

	@Before
	public void setupDb() throws Exception {

		Properties properties = dataSourceProperties("jdbc:mysql://localhost/demo?useSSL=false");
		dataSource = BasicDataSourceFactory.createDataSource(properties);

		try (Connection con = dataSource.getConnection()) {
			runSqlScript(con, DbSupportMySqlIT.class.getResourceAsStream("DbSupportMySqlIT.create.sql"));
			runSqlScript(con, DbSupportMySqlIT.class.getResourceAsStream("DbSupportMySqlIT.data.sql"));
		}

		dbSupport = DbSupportFactory.createInstance(dataSource);
	}

	private Properties dataSourceProperties(String url) {

		Properties properties = new Properties();
		properties.setProperty("driverClassName", DbSupportMySql.DRIVER_CLASS_NAME);
		properties.setProperty("url", url);
		properties.setProperty("username", "root");
		properties.setProperty("password", "");
		return properties;
	}

	@After
	public void shutdownDb() throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			runSqlScript(con, DbSupportMySqlIT.class.getResourceAsStream("DbSupportMySqlIT.drop.sql"));
		}
	}

	@Test
	public void loadPrimaryKey() {
		Map<DatabaseObject, String> loadPrimaryKey = dbSupport.loadPrimaryKey();
		assertNotNull(loadPrimaryKey);
		assertThat(loadPrimaryKey, Matchers.hasEntry(new DatabaseObject("Customer"), "ID"));
	}

	@Test
	public void loadForeignKey() {
		List<ForeignKey> loadForeignKey = dbSupport.loadForeignKey();
		assertNotNull(loadForeignKey);

		DatabaseObject customer = new DatabaseObject("Customer");
		DatabaseObject invoice = new DatabaseObject("Invoice");

		for (ForeignKey foreignKey : loadForeignKey) {
			if (invoice.equals(foreignKey.getFkTable()) && foreignKey.getFkColumnNames().contains("CustomerID")) {
				assertEquals(customer, foreignKey.getPkTable());
				assertThat(foreignKey.getPkColumnNames(), CoreMatchers.hasItem("ID"));
				return;
			}
		}
		fail();
	}

	@Test
	public void toSqlValueString() throws SQLException, ParseException {

		toSqlValueString(stmt -> stmt.setNull(1, Types.VARCHAR), "CHAR");
		toSqlValueString(stmt -> stmt.setString(1, "string with '"), "CHAR");
		toSqlValueString(stmt -> stmt.setLong(1, Long.MAX_VALUE), "SIGNED");
		toSqlValueString(stmt -> stmt.setInt(1, Integer.MAX_VALUE), "SIGNED");
		toSqlValueString(stmt -> stmt.setByte(1, Byte.MAX_VALUE), "SIGNED");
		toSqlValueString(stmt -> stmt.setBoolean(1, true), "SIGNED");
		toSqlValueString(stmt -> stmt.setBoolean(1, false), "SIGNED");

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		toSqlValueString(stmt -> stmt.setTimestamp(1, new Timestamp(df.parse("2018-07-25T18:28:38.048").getTime())), "DATETIME");
		toSqlValueString(stmt -> stmt.setTimestamp(1, new Timestamp(df.parse("2018-07-25T18:28:38.000").getTime())), "DATETIME");
		toSqlValueString(stmt -> stmt.setDate(1, new Date(df.parse("2018-07-25T00:00:00.000").getTime())), "DATE");
		toSqlValueString(stmt -> stmt.setTime(1, new Time(df.parse("1970-01-01T18:28:38.000").getTime())), "TIME");

		toSqlValueString(stmt -> stmt.setBytes(1, new byte[] { (byte) 0xCA, (byte) 0xFE }), "BINARY");
	}

	@FunctionalInterface
	private interface SetStatementParameter {

		void setParameter(PreparedStatement stmt) throws SQLException, ParseException;
	}

	private void toSqlValueString(SetStatementParameter setParameterValue, String convertTo) throws SQLException, ParseException {

		try (Connection con = dbSupport.getConnection();
				PreparedStatement prepStmt = con.prepareStatement("SELECT CAST(? AS " + convertTo + ") AS VALUE");
				Statement stmt = con.createStatement()) {
			
			setParameterValue.setParameter(prepStmt);
			try (ResultSet rsExpected = prepStmt.executeQuery()) {
				while (rsExpected.next()) {
					Object value = rsExpected.getObject(1);

					ResultSet rs = stmt.executeQuery("SELECT " + dbSupport.toSqlValueString(value) + " AS VALUE");
					while (rs.next()) {
						if (value instanceof byte[]) {
							assertArrayEquals((byte[]) value, (byte[]) rs.getObject(1));
						}
						else {
							assertEquals(value, rs.getObject(1));
						}
					}
				}
			}
		}
	}

	@Test
	public void scriptData() throws SQLException {

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("Item").addWhereSql("InvoiceID = 0"));
		DataOutput output = new OutputSqlScript(dbSupport);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);

		String generateScript = output.toString();

		assertThat(generateScript, containsString("-- SELECT * FROM Customer WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO Customer (ID, FirstName, LastName, Street, City) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');"));

		assertThat(generateScript, containsString("-- SELECT * FROM Product WHERE ID IN (7, 14, 47) ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO Product (ID, Name, Price) VALUES (7, 'Telephone Shoe', 84.00);"));
		assertThat(generateScript, containsString("INSERT INTO Product (ID, Name, Price) VALUES (14, 'Telephone Iron', 124.00);"));
		assertThat(generateScript, containsString("INSERT INTO Product (ID, Name, Price) VALUES (47, 'Ice Tea Iron', 178.00);"));

		assertThat(generateScript, containsString("-- SELECT * FROM Invoice WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO Invoice (ID, CustomerID, Total) VALUES (0, 0, 5847.00);"));

		assertThat(generateScript, containsString("-- SELECT * FROM Item WHERE InvoiceID = 0 ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO Item (ID, InvoiceID, Item, ProductID, Quantity, Cost) VALUES (0, 0, 2, 47, 3, 267.00);"));
		assertThat(generateScript, containsString("INSERT INTO Item (ID, InvoiceID, Item, ProductID, Quantity, Cost) VALUES (1, 0, 1, 14, 19, 186.00);"));
		assertThat(generateScript, containsString("INSERT INTO Item (ID, InvoiceID, Item, ProductID, Quantity, Cost) VALUES (2, 0, 0, 7, 12, 126.00);"));
	}

	@Test
	public void scriptDataAndRun() throws SQLException {

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("Item").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutput output = new OutputSqlScript(dbSupport, out);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);
	
		try (Connection con = dataSource.getConnection();
				Statement stmt = con.createStatement()) {

			runSqlScript(con, DbSupportMySqlIT.class.getResourceAsStream("DbSupportMySqlIT.drop.sql"));
			runSqlScript(con, DbSupportMySqlIT.class.getResourceAsStream("DbSupportMySqlIT.create.sql"));
			assertEquals(8, runSqlScript(con, new ByteArrayInputStream(out.toByteArray())));
		}
	}

	private static int runSqlScript(Connection con, InputStream stream) throws SQLException {

		Pattern pattern = Pattern.compile("(?:;(?:\\r|\\n)+)|(?:--.*(?:\\r|\\n)+)");
		try (Statement stmt = con.createStatement()) {

			int affectedRowCount = 0;
			for (Scanner s = new Scanner(stream).useDelimiter(pattern); s.hasNext();) {

				String sql = s.next().trim();
				if (sql.length() > 0) {
					affectedRowCount += stmt.executeUpdate(sql);
				}
			}
			return affectedRowCount;
		}
	}
}
