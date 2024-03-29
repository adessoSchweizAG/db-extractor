package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public abstract class AbstractDbSupportSql92 extends AbstractDbSupport {

	private static final String SQL_SELECT_PRIMARY_KEY = "SELECT CURRENT_CATALOG AS CURRENT_CATALOG, CURRENT_SCHEMA AS CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  a.TABLE_CATALOG, a.TABLE_SCHEMA, a.TABLE_NAME, a.COLUMN_NAME, a.ORDINAL_POSITION \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS  c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE  a ON a.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                   AND a.TABLE_CATALOG = c.TABLE_CATALOG AND a.TABLE_SCHEMA = c.TABLE_SCHEMA AND a.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'PRIMARY KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, a.ORDINAL_POSITION";

	private static final String SQL_SELECT_FOREIGN_KEY = "SELECT CURRENT_CATALOG AS CURRENT_CATALOG, CURRENT_SCHEMA AS CURRENT_SCHEMA, \r\n"
			+ "  c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  fc.TABLE_CATALOG AS FKTABLE_CATALOG, fc.TABLE_SCHEMA AS FKTABLE_SCHEMA, fc.TABLE_NAME AS FKTABLE_NAME, fc.COLUMN_NAME AS FKCOLUMN_NAME, \r\n"
			+ "  a.TABLE_CATALOG AS PKTABLE_CATALOG, a.TABLE_SCHEMA AS PKTABLE_SCHEMA, a.TABLE_NAME AS PKTABLE_NAME, a.COLUMN_NAME AS PKCOLUMN_NAME \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS                c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE        fc ON fc.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND fc.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND fc.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "                                                          AND fc.TABLE_CATALOG = c.TABLE_CATALOG AND fc.TABLE_SCHEMA = c.TABLE_SCHEMA AND fc.TABLE_NAME = c.TABLE_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS  r ON r.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND r.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE         a ON a.CONSTRAINT_CATALOG = r.UNIQUE_CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = r.UNIQUE_CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = r.UNIQUE_CONSTRAINT_NAME AND a.ORDINAL_POSITION = fc.POSITION_IN_UNIQUE_CONSTRAINT \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'FOREIGN KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, fc.POSITION_IN_UNIQUE_CONSTRAINT";

	private final DataSource dataSource;

	protected AbstractDbSupportSql92(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public final Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	protected String getSqlSelectPrimaryKey(Connection con) throws SQLException {
		return getSqlSelectPrimaryKey();
	}

	protected String getSqlSelectPrimaryKey() {
		return SQL_SELECT_PRIMARY_KEY;
	}

	@Override
	public final Map<DatabaseObject, String> loadPrimaryKey() {
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(getSqlSelectPrimaryKey(con))) {

			Map<DatabaseObject, String> result = new HashMap<>();
			while (rs.next()) {
				DatabaseObject table = databaseObject(rs, "TABLE");
				String columnName = rs.getString("COLUMN_NAME");

				result.merge(table, columnName,
						(oldValue, value) -> oldValue + ", " + value);
			}
			return result;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	protected String getSqlSelectForeignKey(Connection con) throws SQLException {
		return getSqlSelectForeignKey();
	}

	protected String getSqlSelectForeignKey() {
		return SQL_SELECT_FOREIGN_KEY;
	}

	@Override
	public final List<ForeignKey> loadForeignKey() {
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(getSqlSelectForeignKey(con))) {

			Map<DatabaseObject, ForeignKey> mapForeignKey = new HashMap<>();
			while (rs.next()) {
				DatabaseObject constraint = databaseObject(rs, "CONSTRAINT");
				DatabaseObject fkTable = databaseObject(rs, "FKTABLE");
				DatabaseObject pkTable = databaseObject(rs, "PKTABLE");

				ForeignKey foreignKey = mapForeignKey.computeIfAbsent(constraint,
						k -> new ForeignKey(constraint, fkTable, pkTable));

				foreignKey.getFkColumnNames().add(rs.getString("FKCOLUMN_NAME"));
				foreignKey.getPkColumnNames().add(rs.getString("PKCOLUMN_NAME"));
			}
			return new ArrayList<>(mapForeignKey.values());
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	protected DatabaseObject databaseObject(ResultSet rs, String prefix) throws SQLException {

		String catalog = nullIf(rs.getString("CURRENT_CATALOG"), rs.getString(prefix + "_CATALOG"));
		String schema = nullIf(rs.getString("CURRENT_SCHEMA"), rs.getString(prefix + "_SCHEMA"));
		String name = rs.getString(prefix + "_NAME");
		return new DatabaseObject(catalog, schema, name);
	}

	protected String nullIf(String current, String value) {
		if (current != null && current.equalsIgnoreCase(value)) {
			return null;
		}
		return value;
	}

}
