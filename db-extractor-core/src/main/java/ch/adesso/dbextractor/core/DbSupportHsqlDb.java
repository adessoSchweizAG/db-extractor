package ch.adesso.dbextractor.core;

import javax.sql.DataSource;

public class DbSupportHsqlDb extends AbstractDbSupportSql92 implements DbSupport {

	public DbSupportHsqlDb(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	protected String toSqlValueString(byte[] value) {
		StringBuilder sb = new StringBuilder(value.length * 2 + 12);
		sb.append("HEXTORAW('");
		for (byte b : value) {
			sb.append(String.format("%02x", b));
		}
		sb.append("')");
		return sb.toString();
	}

}
