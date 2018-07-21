package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptDataHsqlDb extends AbstractScriptData {

	private static final String SQL_SELECT_PRIMARY_KEY = "SELECT c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  a.TABLE_CATALOG, a.TABLE_SCHEMA, a.TABLE_NAME, a.COLUMN_NAME, a.ORDINAL_POSITION \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS  c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE  a ON a.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'PRIMARY KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, a.ORDINAL_POSITION";

	private static final String SQL_SELECT_FOREIGN_KEY = "SELECT c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, \r\n"
			+ "  fc.TABLE_CATALOG AS FK_TABLE_CATALOG, fc.TABLE_SCHEMA AS FK_TABLE_SCHEMA, fc.TABLE_NAME AS FK_TABLE_NAME, fc.COLUMN_NAME AS FK_COLUMN_NAME, \r\n"
			+ "  a.TABLE_CATALOG AS PK_TABLE_CATALOG, a.TABLE_SCHEMA AS PK_TABLE_SCHEMA, a.TABLE_NAME AS PK_TABLE_NAME, a.COLUMN_NAME AS PK_COLUMN_NAME \r\n"
			+ "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS                c \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE        fc ON fc.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND fc.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND fc.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS  r ON r.CONSTRAINT_CATALOG = c.CONSTRAINT_CATALOG AND r.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME = c.CONSTRAINT_NAME \r\n"
			+ "  INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE         a ON a.CONSTRAINT_CATALOG = r.UNIQUE_CONSTRAINT_CATALOG AND a.CONSTRAINT_SCHEMA = r.UNIQUE_CONSTRAINT_SCHEMA AND a.CONSTRAINT_NAME = r.UNIQUE_CONSTRAINT_NAME AND a.ORDINAL_POSITION = fc.POSITION_IN_UNIQUE_CONSTRAINT \r\n"
			+ "WHERE c.CONSTRAINT_TYPE = 'FOREIGN KEY' \r\n"
			+ "ORDER BY c.CONSTRAINT_CATALOG, c.CONSTRAINT_SCHEMA, c.CONSTRAINT_NAME, fc.POSITION_IN_UNIQUE_CONSTRAINT";

	private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

	public ScriptDataHsqlDb(String jdbcUrl, String jdbcUser, String jdbcPassword) {
		super(JDBC_DRIVER, jdbcUrl, jdbcUser, jdbcPassword);
	}

	@Override
	List<TableDataFilter> postProcess(List<TableDataFilter> tables) {
		return tables;
	}

	@Override
	String toSqlValueString(Object value) {
		if (value == null) {
			return "NULL";
		}
		else if (value instanceof Boolean) {
			return (boolean) value ? "1" : "0";
		}
		else if (value instanceof Date) {
			return toSqlValueString((Date) value);
		}
		else if (value instanceof String) {
			return "'" + ((String) value).replace("'", "''") + "'";
		}
		return value.toString();
	}

	private String toSqlValueString(Date value) {

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(value);
		if (cal.get(Calendar.MILLISECOND) > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			return "TIMESTAMP '" + sdf.format(value) + "'";
		}
		else if (cal.get(Calendar.SECOND) > 0 || cal.get(Calendar.MINUTE) > 0 || cal.get(Calendar.HOUR_OF_DAY) > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return "TIMESTAMP '" + sdf.format(value) + "'";
		}
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return "TIMESTAMP ('" + sdf.format(value) + "')";
		}
	}

	@Override
	void handleSpecialConstraints(TableDataFilter table, ResultSet rs) throws SQLException {

	}

	@Override
	List<ForeignKey> loadForeignKey() {
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_SELECT_FOREIGN_KEY);) {

			Map<DatabaseObject, ForeignKey> mapForeignKey = new HashMap<>();
			while (rs.next()) {
				DatabaseObject constraint = databaseObject(
						rs.getString("CONSTRAINT_CATALOG"), rs.getString("CONSTRAINT_SCHEMA"), rs.getString("CONSTRAINT_NAME"));
				DatabaseObject fkTable = databaseObject(
						rs.getString("FK_TABLE_CATALOG"), rs.getString("FK_TABLE_SCHEMA"), rs.getString("FK_TABLE_NAME"));
				DatabaseObject pkTable = databaseObject(
						rs.getString("PK_TABLE_CATALOG"), rs.getString("PK_TABLE_SCHEMA"), rs.getString("PK_TABLE_NAME"));
				
				ForeignKey foreignKey = mapForeignKey.computeIfAbsent(constraint,
						k -> new ForeignKey(constraint, fkTable, pkTable));
				
				foreignKey.getFkColumnNames().add(rs.getString("FK_COLUMN_NAME"));
				foreignKey.getPkColumnNames().add(rs.getString("PK_COLUMN_NAME"));
			}
			return new ArrayList<>(mapForeignKey.values());
		} catch (ClassNotFoundException | SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	Map<DatabaseObject, String> loadPrimaryKey() {
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_SELECT_PRIMARY_KEY);) {

			Map<DatabaseObject, String> result = new HashMap<>();
			while (rs.next()) {
				DatabaseObject table = databaseObject(
						rs.getString("TABLE_CATALOG"), rs.getString("TABLE_SCHEMA"), rs.getString("TABLE_NAME"));
				String columnName = rs.getString("COLUMN_NAME");
				
				result.merge(table, columnName,
						(oldValue, value) -> oldValue + ", " + value);
			}
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private DatabaseObject databaseObject(String catalog, String schema, String name) {
		return new DatabaseObject(
				"PUBLIC".equalsIgnoreCase(catalog) ? null : catalog,
				"PUBLIC".equalsIgnoreCase(schema) ? null : schema,
				name);
	}
}
