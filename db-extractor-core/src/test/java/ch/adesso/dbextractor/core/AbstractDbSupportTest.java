package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class AbstractDbSupportTest {

	private DbSupport dbSupport = new TestDbSupport();

	@Test
	public void toSqlValueStringNull() {
		assertEquals("NULL", dbSupport.toSqlValueString(null));
	}

	@Test
	public void toSqlValueStringBoolean() {
		assertEquals("TRUE", dbSupport.toSqlValueString(true));
		assertEquals("FALSE", dbSupport.toSqlValueString(false));
	}

	@Test
	public void toSqlValueStringTimestamp() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		assertEquals("TIMESTAMP '2018-07-25 18:28:38'", dbSupport.toSqlValueString(df.parse("2018-07-25T18:28:38.000")));
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.048'", dbSupport.toSqlValueString(df.parse("2018-07-25T18:28:38.048")));

		Timestamp timestamp = new Timestamp(df.parse("2018-07-25T18:28:38.000").getTime());
		assertEquals("TIMESTAMP '2018-07-25 18:28:38'", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(1);
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.000000001'", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_999_999);
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.999999999'", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_999_000);
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.999999'", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_900_000);
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.9999'", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_000_000);
		assertEquals("TIMESTAMP '2018-07-25 18:28:38.999'", dbSupport.toSqlValueString(timestamp));

		assertEquals("TIMESTAMP '2018-07-25 00:00:00'", dbSupport.toSqlValueString(new Timestamp(df.parse("2018-07-25T00:00:00.000").getTime())));
	}

	@Test
	public void toSqlValueStringDate() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		assertEquals("DATE '2018-07-25'", dbSupport.toSqlValueString(df.parse("2018-07-25")));
		assertEquals("DATE '2018-07-25'", dbSupport.toSqlValueString(new java.sql.Date(df.parse("2018-07-25").getTime())));
	}

	@Test
	public void toSqlValueStringTime() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		assertEquals("TIME '18:28:38'", dbSupport.toSqlValueString(new java.sql.Time(df.parse("18:28:38").getTime())));
	}

	@Test
	public void toSqlValueStringString() throws ParseException {

		assertEquals("'string value with '''", dbSupport.toSqlValueString("string value with '"));
	}

	private static class TestDbSupport extends AbstractDbSupport {

		@Override
		public Map<DatabaseObject, String> loadPrimaryKey() {
			return null;
		}

		@Override
		public List<ForeignKey> loadForeignKey() {
			return null;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return null;
		}

		@Override
		protected String toSqlValueString(byte[] value) {
			return null;
		}
	}
}
