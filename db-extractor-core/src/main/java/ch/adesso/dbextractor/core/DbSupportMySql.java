package ch.adesso.dbextractor.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DbSupportMySql extends AbstractDbSupportSql92 {

	public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	private static final String SQL_SELECT_PRIMARY_KEY = "SELECT SCHEMA() AS CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  a.TABLE_CATALOG, a.TABLE_SCHEMA, a.TABLE_NAME, a.COLUMN_NAME, a.ORDINAL_POSITION \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS  c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE  a ON a.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                   AND a.TABLE_SCHEMA = c.TABLE_SCHEMA AND a.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'PRIMARY KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, a.ORDINAL_POSITION";

	private static final String SQL_SELECT_FOREIGN_KEY = "SELECT SCHEMA() AS CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  fc.TABLE_CATALOG AS FKTABLE_CATALOG, fc.TABLE_SCHEMA AS FKTABLE_SCHEMA, fc.TABLE_NAME AS FKTABLE_NAME, fc.COLUMN_NAME AS FKCOLUMN_NAME, \r\n"
			+ "  a.TABLE_CATALOG AS PKTABLE_CATALOG, a.TABLE_SCHEMA AS PKTABLE_SCHEMA, a.TABLE_NAME AS PKTABLE_NAME, a.COLUMN_NAME AS PKCOLUMN_NAME \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS                c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE        fc ON fc.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND fc.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND fc.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                          AND fc.TABLE_SCHEMA = c.TABLE_SCHEMA AND fc.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS  r ON r.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND r.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                         AND r.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE         a ON a.CONSTRAINT_CATALOG = r.UNIQUE_CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = r.UNIQUE_CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = r.UNIQUE_CONSTRAINT_NAME AND a.ORDINAL_POSITION = fc.POSITION_IN_UNIQUE_CONSTRAINT \r\n"
			+ "                                                          AND a.TABLE_NAME = r.REFERENCED_TABLE_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'FOREIGN KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, fc.POSITION_IN_UNIQUE_CONSTRAINT";

	public DbSupportMySql(DataSource dataSource) {
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
	protected DatabaseObject databaseObject(ResultSet rs, String prefix) throws SQLException {
		String schema = nullIf(rs.getString("CURRENT_SCHEMA"), rs.getString(prefix + "_SCHEMA"));
		String name = rs.getString(prefix + "_NAME");
		return new DatabaseObject(schema, name);
	}

	@Override
	protected String toSqlValueString(byte[] value) {
		StringBuilder sb = new StringBuilder(value.length * 2 + 12);
		sb.append("UNHEX('");
		for (byte b : value) {
			sb.append(String.format("%02x", b));
		}
		sb.append("')");
		return sb.toString();
	}

}
