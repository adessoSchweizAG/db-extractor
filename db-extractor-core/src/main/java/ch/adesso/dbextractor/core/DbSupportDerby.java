package ch.adesso.dbextractor.core;

import java.util.Date;

import javax.sql.DataSource;

public class DbSupportDerby extends AbstractDbSupportJdbcMetaData {

	public static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.AutoloadedDriver";

	public DbSupportDerby(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String toSqlValueString(Object value) {
		
		String sqlValueString = super.toSqlValueString(value);

		if (value instanceof Date) {
			StringBuilder sbSqlValueString = new StringBuilder(sqlValueString);
			sbSqlValueString.insert(sqlValueString.indexOf('\''), "(");
			sbSqlValueString.append(")");
			sqlValueString = sbSqlValueString.toString();
		}
		
		return sqlValueString;
	}

	@Override
	protected String toSqlValueString(byte[] value) {
		StringBuilder sb = new StringBuilder(value.length * 2 + 3);
		sb.append("X'");
		for (byte b : value) {
			sb.append(String.format("%02x", b));
		}
		sb.append("'");
		return sb.toString();
	}
}
