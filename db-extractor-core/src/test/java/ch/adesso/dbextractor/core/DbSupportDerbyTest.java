package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

public class DbSupportDerbyTest {

	private DbSupport dbSupport = new DbSupportDerby(null);

	@Test
	public void toSqlValueStringTimestamp() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		assertEquals("TIMESTAMP ('2018-07-25 18:28:38')", dbSupport.toSqlValueString(df.parse("2018-07-25T18:28:38.000")));
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.048')", dbSupport.toSqlValueString(df.parse("2018-07-25T18:28:38.048")));

		Timestamp timestamp = new Timestamp(df.parse("2018-07-25T18:28:38.000").getTime());
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38')", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(1);
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.000000001')", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_999_999);
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.999999999')", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_999_000);
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.999999')", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_900_000);
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.9999')", dbSupport.toSqlValueString(timestamp));
		timestamp.setNanos(999_000_000);
		assertEquals("TIMESTAMP ('2018-07-25 18:28:38.999')", dbSupport.toSqlValueString(timestamp));

		assertEquals("TIMESTAMP ('2018-07-25 00:00:00')", dbSupport.toSqlValueString(new Timestamp(df.parse("2018-07-25T00:00:00.000").getTime())));
	}

	@Test
	public void toSqlValueStringDate() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		assertEquals("DATE ('2018-07-25')", dbSupport.toSqlValueString(df.parse("2018-07-25")));
		assertEquals("DATE ('2018-07-25')", dbSupport.toSqlValueString(new java.sql.Date(df.parse("2018-07-25").getTime())));
	}

	@Test
	public void toSqlValueStringTime() throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		assertEquals("TIME ('18:28:38')", dbSupport.toSqlValueString(new java.sql.Time(df.parse("18:28:38").getTime())));
	}
}
