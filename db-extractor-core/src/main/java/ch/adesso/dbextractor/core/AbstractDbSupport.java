package ch.adesso.dbextractor.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class AbstractDbSupport implements DbSupport {

	private static final String PATTERN_DATE = "'DATE '''yyyy-MM-dd''";
	private static final String PATTERN_TIME = "'TIME '''HH:mm:ss''";
	private static final String PATTERN_TIMESTAMP = "'TIMESTAMP '''yyyy-MM-dd HH:mm:ss''";
	private static final String PATTERN_TIMESTAMP_MS = "'TIMESTAMP '''yyyy-MM-dd HH:mm:ss.SSS''";

	@Override
	public String toSqlValueString(Object value) {
		if (value == null) {
			return "NULL";
		} else if (value instanceof Boolean) {
			return toSqlValueString((boolean) value);
		} else if (value instanceof java.sql.Date) {
			return toSqlValueString((java.sql.Date) value);
		} else if (value instanceof java.sql.Time) {
			return toSqlValueString((java.sql.Time) value);
		} else if (value instanceof Date) {
			return toSqlValueString((Date) value);
		} else if (value instanceof String) {
			return toSqlValueString((String) value);
		} else if (value instanceof byte[]) {
			return toSqlValueString((byte[]) value);
		}
		return value.toString();
	}

	protected String toSqlValueString(boolean value) {
		return value ? "TRUE" : "FALSE";
	}

	protected String toSqlValueString(java.sql.Date value) {

		SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE);
		return sdf.format(value);
	}

	protected String toSqlValueString(java.sql.Time value) {

		final SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME);
		return sdf.format(value);
	}

	protected String toSqlValueString(Date value) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(value);

		final SimpleDateFormat sdf;
		if (value instanceof java.sql.Timestamp
				&& ((java.sql.Timestamp) value).getNanos() % 1_000_000 > 0) {

			sdf = new SimpleDateFormat(PATTERN_TIMESTAMP_MS);

			String nanos = String.format("%06d", ((java.sql.Timestamp) value).getNanos() % 1_000_000);
			for (int i = nanos.length() - 1; i >= 0 && nanos.charAt(i) == '0'; i--) {
				nanos = nanos.substring(0, i);
			}
			
			StringBuilder sb = new StringBuilder(sdf.format(value));
			sb.insert(sb.length() - 1, nanos);
			return sb.toString();
		} else if (cal.get(Calendar.MILLISECOND) > 0) {
			sdf = new SimpleDateFormat(PATTERN_TIMESTAMP_MS);
		} else if (cal.get(Calendar.SECOND) > 0 || cal.get(Calendar.MINUTE) > 0 || cal.get(Calendar.HOUR) > 0
				|| value instanceof java.sql.Timestamp) {
			sdf = new SimpleDateFormat(PATTERN_TIMESTAMP);
		} else {
			sdf = new SimpleDateFormat(PATTERN_DATE);
		}
		return sdf.format(value);
	}

	protected String toSqlValueString(String value) {
		return "'" + value.replace("'", "''") + "'";
	}

	protected abstract String toSqlValueString(byte[] value);

}
