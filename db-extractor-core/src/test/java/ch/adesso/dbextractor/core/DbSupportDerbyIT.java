package ch.adesso.dbextractor.core;

import static ch.adesso.dbextractor.core.TestHelper.runSqlScript;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DbSupportDerbyIT {

	private DataSource dataSource;
	private DbSupport dbSupport;

	@Before
	public void setupDb() throws Exception {

		// disable log file
		System.setProperty("derby.stream.error.method", DbSupportDerbyIT.class.getName() + ".getNoopOutputStream");

		Properties properties = dataSourceProperties("jdbc:derby:memory:memDb;create=true");
		dataSource = BasicDataSourceFactory.createDataSource(properties);

		try (Connection con = dataSource.getConnection()) {
			runSqlScript(con, DbSupportDerbyIT.class.getResourceAsStream("DbSupportDerbyIT.create.sql"));
			runSqlScript(con, DbSupportDerbyIT.class.getResourceAsStream("DbSupportDerbyIT.data.sql"));
		}

		dbSupport = DbSupportFactory.createInstance(dataSource);
	}

	@After
	public void shutdownDb() throws Exception {

		String urlDropDatabase = "jdbc:derby:memory:memDb;drop=true";

		try (Connection con = DriverManager.getConnection(urlDropDatabase)) {
			// drop database
		}
		catch (SQLException ex) {
			assertEquals("08006", ex.getSQLState());
		}
	}

	private Properties dataSourceProperties(String url) {

		Properties properties = new Properties();
		properties.setProperty("driverClassName", DbSupportDerby.DRIVER_CLASS_NAME);
		properties.setProperty("url", url);
		properties.setProperty("username", "SA");
		return properties;
	}

	@Test
	public void loadPrimaryKey() {
		Map<DatabaseObject, String> loadPrimaryKey = dbSupport.loadPrimaryKey();
		assertNotNull(loadPrimaryKey);
		assertThat(loadPrimaryKey, Matchers.hasEntry(new DatabaseObject("customer"), "ID"));
	}

	@Test
	public void loadForeignKey() {
		List<ForeignKey> loadForeignKey = dbSupport.loadForeignKey();
		assertNotNull(loadForeignKey);

		DatabaseObject customer = new DatabaseObject("customer");
		DatabaseObject invoice = new DatabaseObject("invoice");

		for (ForeignKey foreignKey : loadForeignKey) {
			if (invoice.equals(foreignKey.getFkTable()) && foreignKey.getFkColumnNames().contains("CUSTOMERID")) {
				assertEquals(customer, foreignKey.getPkTable());
				assertThat(foreignKey.getPkColumnNames(), CoreMatchers.hasItem("ID"));
				return;
			}
		}
		fail();
	}

	@Test
	public void toSqlValueString() throws SQLException, ParseException {

		toSqlValueString(stmt -> stmt.setString(1, "string with '"), "VARCHAR(100)");
		toSqlValueString(stmt -> stmt.setLong(1, Long.MAX_VALUE), "BIGINT");
		toSqlValueString(stmt -> stmt.setInt(1, Integer.MAX_VALUE), "INTEGER");
		toSqlValueString(stmt -> stmt.setInt(1, -42), "INTEGER");
		toSqlValueString(stmt -> stmt.setBoolean(1, true), "BOOLEAN");
		toSqlValueString(stmt -> stmt.setBoolean(1, false), "BOOLEAN");

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Timestamp timestamp = new Timestamp(df.parse("2018-07-25T18:28:38").getTime());

		toSqlValueString(stmt -> stmt.setTimestamp(1, timestamp), "TIMESTAMP");

		timestamp.setNanos(0);
		toSqlValueString(stmt -> stmt.setTimestamp(1, timestamp), "TIMESTAMP");

		timestamp.setNanos(1_000);
		toSqlValueString(stmt -> stmt.setTimestamp(1, timestamp), "TIMESTAMP");

		timestamp.setNanos(48_900_000);
		toSqlValueString(stmt -> stmt.setTimestamp(1, timestamp), "TIMESTAMP");

		toSqlValueString(stmt -> stmt.setDate(1, new Date(df.parse("2018-07-25T00:00:00").getTime())), "DATE");
		toSqlValueString(stmt -> stmt.setTime(1, new Time(df.parse("1970-01-01T18:28:38").getTime())), "TIME");

		toSqlValueString(stmt -> stmt.setBytes(1, new byte[] { (byte) 0xCA, (byte) 0xFE }), "VARCHAR(100) FOR BIT DATA");
	}

	@FunctionalInterface
	private interface SetStatementParameter {

		void setParameter(PreparedStatement stmt) throws SQLException, ParseException;
	}

	private void toSqlValueString(SetStatementParameter setParameterValue, String convertTo) throws SQLException, ParseException {

		try (Connection con = dbSupport.getConnection();
				PreparedStatement prepStmt = con.prepareStatement("SELECT CAST(? AS " + convertTo + ") AS VALUE FROM SYSIBM.SYSDUMMY1");
				Statement stmt = con.createStatement()) {

			setParameterValue.setParameter(prepStmt);
			try (ResultSet rsExpected = prepStmt.executeQuery()) {
				while (rsExpected.next()) {
					Object value = rsExpected.getObject(1);

					ResultSet rs = stmt.executeQuery("SELECT " + dbSupport.toSqlValueString(value) + " AS VALUE FROM SYSIBM.SYSDUMMY1");
					while (rs.next()) {
						if (value instanceof byte[]) {
							assertArrayEquals((byte[]) value, (byte[]) rs.getObject(1));
						} else {
							assertEquals(value, rs.getObject(1));
						}
					}
				}
			}
		}
	}

	@Test
	public void scriptData() throws SQLException {

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		DataOutput output = new OutputSqlScript(dbSupport);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);

		String generateScript = output.toString();

		assertThat(generateScript, containsString("-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript,
				containsString("INSERT INTO CUSTOMER (ID, FIRSTNAME, LASTNAME, STREET, CITY) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');"));

		assertThat(generateScript, containsString("-- SELECT * FROM PRODUCT WHERE ID IN (7, 14, 47) ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (7, 'Telephone Shoe', 84.00);"));
		assertThat(generateScript, containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (14, 'Telephone Iron', 124.00);"));
		assertThat(generateScript, containsString("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES (47, 'Ice Tea Iron', 178.00);"));

		assertThat(generateScript, containsString("-- SELECT * FROM INVOICE WHERE ID IN (0) ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO INVOICE (ID, CUSTOMERID, TOTAL) VALUES (0, 0, 5847.00);"));

		assertThat(generateScript, containsString("-- SELECT * FROM ITEM WHERE InvoiceID = 0 ORDER BY ID;"));
		assertThat(generateScript, containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (0, 0, 2, 47, 3, 267.00);"));
		assertThat(generateScript, containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (1, 0, 1, 14, 19, 186.00);"));
		assertThat(generateScript, containsString("INSERT INTO ITEM (ID, INVOICEID, ITEM, PRODUCTID, QUANTITY, COST) VALUES (2, 0, 0, 7, 12, 126.00);"));
	}

	@Test
	public void scriptDataAndRun() throws SQLException {

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutput output = new OutputSqlScript(dbSupport, out);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);

		try (Connection con = DriverManager.getConnection("jdbc:derby:memory:memdbTest;create=true", "SA", null);
				Statement stmt = con.createStatement()) {

			runSqlScript(con, DbSupportDerbyIT.class.getResourceAsStream("DbSupportDerbyIT.create.sql"));
			assertEquals(8, runSqlScript(con, new ByteArrayInputStream(out.toByteArray())));
		}
	}

	/**
	 * Returns an {@link java.io.OutputStream} that ignores all data given to it.
	 */
	public static OutputStream getNoopOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// ignore the output
			}
		};
	}
}
