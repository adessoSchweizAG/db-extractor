package ch.adesso.dbextractor.core;

import javax.sql.DataSource;

public class DbSupportH2 extends AbstractDbSupportSql92 implements DbSupport {

	public static final String DRIVER_CLASS_NAME = "org.h2.Driver";

	private static final String SQL_SELECT_PRIMARY_KEY = "SELECT CURRENT_CATALOG, CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  a.TABLE_CATALOG, a.TABLE_SCHEMA, a.TABLE_NAME, a.COLUMN_NAME, a.ORDINAL_POSITION \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS  c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE  a ON a.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                   AND a.TABLE_CATALOG = c.TABLE_CATALOG AND a.TABLE_SCHEMA = c.TABLE_SCHEMA AND a.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'PRIMARY KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, a.ORDINAL_POSITION";

	private static final String SQL_SELECT_FOREIGN_KEY = "SELECT CURRENT_CATALOG, CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  r.FKTABLE_CATALOG, r.FKTABLE_SCHEMA, r.FKTABLE_NAME, r.FKCOLUMN_NAME, \r\n"
			+ "  r.PKTABLE_CATALOG, r.PKTABLE_SCHEMA, r.PKTABLE_NAME, r.PKCOLUMN_NAME \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS         c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.CROSS_REFERENCES  r ON r.FKTABLE_CATALOG = c.CONSTRAINT_CATALOG AND r.FKTABLE_SCHEMA = c.CONSTRAINT_SCHEMA AND r.FK_NAME = c.CONSTRAINT_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'FOREIGN KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, r.ORDINAL_POSITION";

	public DbSupportH2(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	protected String getSqlSelectPrimaryKey() {
		return SQL_SELECT_PRIMARY_KEY;
	}

	@Override
	protected String getSqlSelectForeignKey() {
		return SQL_SELECT_FOREIGN_KEY;
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
