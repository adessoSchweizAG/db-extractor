package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

public class DbSupportPostgres extends AbstractDbSupportSql92 implements DbSupport {

	private DataSource dataSource;

	public DbSupportPostgres(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	protected String toSqlValueString(Date value) {

		final SimpleDateFormat sdf;
		if (value instanceof java.sql.Time) {
			sdf = new SimpleDateFormat("'TIME '''HH:mm:ss''");
		} else if (value instanceof java.sql.Date) {
			sdf = new SimpleDateFormat("'DATE '''yyyy-MM-dd''");
		} else {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(value);
			if (cal.get(Calendar.MILLISECOND) == 0) {
				sdf = new SimpleDateFormat("'TIMESTAMP '''yyyy-MM-dd HH:mm:ss''");
			} else {
				sdf = new SimpleDateFormat("'TIMESTAMP '''yyyy-MM-dd HH:mm:ss.SSS''");
			}
		}
		return sdf.format(value);
	}

	@Override
	protected String toSqlValueString(byte[] value) {
		StringBuilder sb = new StringBuilder(value.length * 2 + 17);
		sb.append("DECODE('");
		for (byte b : value) {
			sb.append(String.format("%02x", b));
		}
		sb.append("', 'hex')");
		return sb.toString();
	}

}
