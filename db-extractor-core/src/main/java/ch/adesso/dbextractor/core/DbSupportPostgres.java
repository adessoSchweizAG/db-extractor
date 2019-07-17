package ch.adesso.dbextractor.core;

import javax.sql.DataSource;

public class DbSupportPostgres extends AbstractDbSupportSql92 implements DbSupport {

	public DbSupportPostgres(DataSource dataSource) {
		super(dataSource);
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
