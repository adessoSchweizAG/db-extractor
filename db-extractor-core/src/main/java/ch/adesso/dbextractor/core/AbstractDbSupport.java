package ch.adesso.dbextractor.core;

import java.util.Date;

public abstract class AbstractDbSupport implements DbSupport {

	@Override
	public String toSqlValueString(Object value) {
		if (value == null) {
			return "NULL";
		} else if (value instanceof Boolean) {
			return toSqlValueString((boolean) value);
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

	protected abstract String toSqlValueString(Date value);

	protected String toSqlValueString(String value) {
		return "'" + value.replace("'", "''") + "'";
	}

	protected abstract String toSqlValueString(byte[] value);

}
