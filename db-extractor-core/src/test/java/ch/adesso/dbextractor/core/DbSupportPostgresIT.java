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

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DbSupportPostgresIT {

	private DataSource dataSource;
	private DbSupport dbSupport;

	@Before
	public void setupDb() throws Exception {

		Properties properties = dataSourceProperties("jdbc:postgresql:");
		dataSource = BasicDataSourceFactory.createDataSource(properties);

		try (Connection con = dataSource.getConnection()) {
			runSqlScript(con, DbSupportPostgresIT.class.getResourceAsStream("DbSupportPostgresIT.create.sql"));
			runSqlScript(con, DbSupportPostgresIT.class.getResourceAsStream("DbSupportPostgresIT.data.sql"));
		}

		dbSupport = DbSupportFactory.createInstance(dataSource);
	}

	private Properties dataSourceProperties(String url) {

		Properties properties = new Properties();
		properties.setProperty("driverClassName", DbSupportPostgres.DRIVER_CLASS_NAME);
		properties.setProperty("url", url);
		properties.setProperty("username", "postgres");
		properties.setProperty("password", "postgres");
		return properties;
	}

	@After
	public void shutdownDb() throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			runSqlScript(con, DbSupportPostgresIT.class.getResourceAsStream("DbSupportPostgresIT.drop.sql"));
		}
	}

	@Test
	public void loadPrimaryKey() {
		Map<DatabaseObject, String> loadPrimaryKey = dbSupport.loadPrimaryKey();
		assertNotNull(loadPrimaryKey);
		assertThat(loadPrimaryKey, Matchers.hasEntry(new DatabaseObject("customer"), "id"));
	}

	@Test
	public void loadForeignKey() {
		List<ForeignKey> loadForeignKey = dbSupport.loadForeignKey();
		assertNotNull(loadForeignKey);

		DatabaseObject customer = new DatabaseObject("customer");
		DatabaseObject invoice = new DatabaseObject("invoice");

		for (ForeignKey foreignKey : loadForeignKey) {
			if (invoice.equals(foreignKey.getFkTable()) && foreignKey.getFkColumnNames().contains("customerid")) {
				assertEquals(customer, foreignKey.getPkTable());
				assertThat(foreignKey.getPkColumnNames(), CoreMatchers.hasItem("id"));
				return;
			}
		}
		fail();
	}

	@Test
	public void toSqlValueString() throws SQLException, ParseException {

		toSqlValueString(stmt -> stmt.setNull(1, Types.VARCHAR), "VARCHAR");
		toSqlValueString(stmt -> stmt.setString(1, "string with '"), "VARCHAR");
		toSqlValueString(stmt -> stmt.setLong(1, Long.MAX_VALUE), "BIGINT");
		toSqlValueString(stmt -> stmt.setInt(1, Integer.MAX_VALUE), "INTEGER");
		toSqlValueString(stmt -> stmt.setByte(1, Byte.MAX_VALUE), "SMALLINT");
		toSqlValueString(stmt -> stmt.setBoolean(1, true), "BOOLEAN");
		toSqlValueString(stmt -> stmt.setBoolean(1, false), "BOOLEAN");

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		toSqlValueString(stmt -> stmt.setTimestamp(1, new Timestamp(df.parse("2018-07-25T18:28:38.048").getTime())), "TIMESTAMP");
		toSqlValueString(stmt -> stmt.setTimestamp(1, new Timestamp(df.parse("2018-07-25T18:28:38.000").getTime())), "TIMESTAMP");
		toSqlValueString(stmt -> stmt.setDate(1, new Date(df.parse("2018-07-25T00:00:00.000").getTime())), "DATE");
		toSqlValueString(stmt -> stmt.setTime(1, new Time(df.parse("1970-01-01T18:28:38.000").getTime())), "TIME");

		toSqlValueString(stmt -> stmt.setBytes(1, new byte[] { (byte) 0xCA, (byte) 0xFE }), "BYTEA");
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

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("item").addWhereSql("InvoiceID = 0"));
		DataOutput output = new OutputSqlScript(dbSupport);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);

		String generateScript = output.toString();

		assertThat(generateScript, containsString("-- SELECT * FROM customer WHERE id IN (0) ORDER BY id;"));
		assertThat(generateScript, containsString("INSERT INTO customer (id, firstname, lastname, street, city) VALUES (0, 'Laura', 'Steel', '429 Seventh Av.', 'Dallas');"));

		assertThat(generateScript, containsString("-- SELECT * FROM product WHERE id IN (7, 14, 47) ORDER BY id;"));
		assertThat(generateScript, containsString("INSERT INTO product (id, name, price) VALUES (7, 'Telephone Shoe', 84);"));
		assertThat(generateScript, containsString("INSERT INTO product (id, name, price) VALUES (14, 'Telephone Iron', 124);"));
		assertThat(generateScript, containsString("INSERT INTO product (id, name, price) VALUES (47, 'Ice Tea Iron', 178);"));

		assertThat(generateScript, containsString("-- SELECT * FROM invoice WHERE id IN (0) ORDER BY id;"));
		assertThat(generateScript, containsString("INSERT INTO invoice (id, customerid, total) VALUES (0, 0, 5847.0);"));

		assertThat(generateScript, containsString("-- SELECT * FROM item WHERE InvoiceID = 0 ORDER BY id;"));
		assertThat(generateScript, containsString("INSERT INTO item (id, invoiceid, item, productid, quantity, cost) VALUES (0, 0, 2, 47, 3, 267.0);"));
		assertThat(generateScript, containsString("INSERT INTO item (id, invoiceid, item, productid, quantity, cost) VALUES (1, 0, 1, 14, 19, 186.0);"));
		assertThat(generateScript, containsString("INSERT INTO item (id, invoiceid, item, productid, quantity, cost) VALUES (2, 0, 0, 7, 12, 126.0);"));
	}

	@Test
	public void scriptDataAndRun() throws SQLException {

		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("item").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutput output = new OutputSqlScript(dbSupport, out);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);
		scriptData.script(list, output);
	
		try (Connection con = dataSource.getConnection();
				Statement stmt = con.createStatement()) {

			runSqlScript(con, DbSupportPostgresIT.class.getResourceAsStream("DbSupportPostgresIT.drop.sql"));
			runSqlScript(con, DbSupportPostgresIT.class.getResourceAsStream("DbSupportPostgresIT.create.sql"));
			assertEquals(8, runSqlScript(con, new ByteArrayInputStream(out.toByteArray())));
		}
	}
}
