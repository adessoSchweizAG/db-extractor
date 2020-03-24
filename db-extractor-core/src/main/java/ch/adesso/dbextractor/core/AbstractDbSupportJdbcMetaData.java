package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public abstract class AbstractDbSupportJdbcMetaData extends AbstractDbSupport {

	private static final String[] TYPES = new String[] { "TABLE" };

	private final DataSource dataSource;

	protected AbstractDbSupportJdbcMetaData(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public final Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public Map<DatabaseObject, String> loadPrimaryKey() {

		try (Connection con = getConnection();
				ResultSet rsTables = con.getMetaData().getTables(null, null, null, TYPES)) {

			Map<DatabaseObject, String> result = new HashMap<>();
			while (rsTables.next()) {
				DatabaseObject table = databaseObject(con, rsTables, "TABLE");
				try (ResultSet rs = con.getMetaData().getPrimaryKeys(
						rsTables.getString("TABLE_CAT"), rsTables.getString("TABLE_SCHEM"), rsTables.getString("TABLE_NAME"))) {

					while (rs.next()) {
						String columnName = rs.getString("COLUMN_NAME");
						result.merge(table, columnName,
								(oldValue, value) -> oldValue + ", " + value);
					}
				}
			}
			return result;
		}
		catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<ForeignKey> loadForeignKey() {

		try (Connection con = getConnection();
				ResultSet rsTables = con.getMetaData().getTables(null, null, null, TYPES)) {
			
			Map<DatabaseObject, ForeignKey> mapForeignKey = new HashMap<>();
			while (rsTables.next()) {
				try (ResultSet rs = con.getMetaData().getExportedKeys(
						rsTables.getString("TABLE_CAT"), rsTables.getString("TABLE_SCHEM"), rsTables.getString("TABLE_NAME"))) {

					while (rs.next()) {
						DatabaseObject constraint = databaseObject(con, rs, "FKTABLE", rs.getString("FK_NAME"));
						DatabaseObject fkTable = databaseObject(con, rs, "FKTABLE");
						DatabaseObject pkTable = databaseObject(con, rs, "PKTABLE");

						ForeignKey foreignKey = mapForeignKey.computeIfAbsent(constraint,
								k -> new ForeignKey(constraint, fkTable, pkTable));

						foreignKey.getFkColumnNames().add(rs.getString("FKCOLUMN_NAME"));
						foreignKey.getPkColumnNames().add(rs.getString("PKCOLUMN_NAME"));
					}
				}
			}
			
			return new ArrayList<>(mapForeignKey.values());
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private DatabaseObject databaseObject(Connection con, ResultSet rs, String prefix) throws SQLException {

		return databaseObject(con, rs, prefix, rs.getString(prefix + "_NAME"));
	}

	protected DatabaseObject databaseObject(Connection con, ResultSet rs, String prefix, String name) throws SQLException {

		String catalog = nullIf(con.getCatalog(), rs.getString(prefix + "_CAT"));
		String schema = nullIf(con.getSchema(), rs.getString(prefix + "_SCHEM"));
		return new DatabaseObject(catalog, schema, name);
	}

	protected String nullIf(String current, String value) {
		if (current != null && current.equalsIgnoreCase(value)) {
			return null;
		}
		if (value == null || value.length() == 0) {
			return null;
		}
		return value;
	}
}
