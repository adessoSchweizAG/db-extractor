package ch.adesso.dbextractor.core;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractScriptData implements ScriptData {

	private final String jdbcDriver;
	private final String jdbcUrl;
	private final String jdbcUser;
	private final String jdbcPassword;

	private Map<DatabaseObject, String> primaryKey;
	private List<ForeignKey> foreignKey;
	private Map<DatabaseObject, TableDataFilter> mapTables = new HashMap<>();

	public AbstractScriptData(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPassword) {
		this.jdbcDriver = jdbcDriver;
		this.jdbcUrl = jdbcUrl;
		this.jdbcUser = jdbcUser;
		this.jdbcPassword = jdbcPassword;
	}

	@Override
	public void script(List<TableDataFilter> list, PrintStream outStream) {

		outStream.println("-- Input:");
		for (TableDataFilter table : list) {
			mapTables.put(table.getTable(), table);
			if (table.hasFilter()) {
				outStream.println("-- " + table.toSelectSql());
			}
		}
		outStream.println();

		List<TableDataFilter> tables = collectData();
		for (TableDataFilter table : tables) {

			outStream.println();
			outStream.println("-- " + table.toSelectSql() + ";");
			printInsertSql(table, outStream);
		}
	}

	private List<TableDataFilter> collectData() {
		while (!isFilterDone()) {
			for (TableDataFilter table : new ArrayList<>(mapTables.values())) {
				if (table.isFilterModified()) {
					table.setFilterModified(false);

					List<ForeignKey> foreignKeys = getForeignKey(table.getTable());
					try (Connection con = getConnection();
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery(table.toSelectSql());) {

						while (rs.next()) {
							handleForeignKeyConstraints(table, foreignKeys, rs);
							handleSpecialConstraints(table, rs);
						}

					} catch (ClassNotFoundException | SQLException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}

		List<TableDataFilter> tables = sortTables();

		for (Iterator<TableDataFilter> it = tables.iterator(); it.hasNext();) {
			TableDataFilter table = it.next();

			if (!table.hasFilter()) {
				it.remove();
				continue;
			}
			if (table.getOrderBy() == null) {
				table.setOrderBy(getPrimaryKeyColumns(table.getTable()));
			}
		}

		return postProcess(tables);
	}

	void handleForeignKeyConstraints(TableDataFilter table, List<ForeignKey> foreignKeys, ResultSet rs)
			throws SQLException {
		
		for (ForeignKey fk : foreignKeys) {
			
			if (fk.getFkColumnNames().size() == 1) {
				Object columnValue = rs.getObject(fk.getFkColumnNames().get(0));
				if (columnValue != null) {
					getTableDataFilter(fk.getPkTable()).addWhereInValue(fk.getPkColumnNames().get(0), columnValue);
				}
			}
			else {
				StringBuilder sb = new StringBuilder("(");
				for (int i = 0; i < fk.getFkColumnNames().size(); i++) {
					Object columnValue = rs.getObject(fk.getFkColumnNames().get(i));
					if (columnValue == null) {
						sb.append(fk.getPkColumnNames().get(i)).append(" IS NULL");
					}
					else {
						sb.append(fk.getPkColumnNames().get(i)).append(" = ").append(toSqlValueString(columnValue));
					}
					sb.append(" AND ");
				}
				sb.replace(sb.length() - 5, sb.length(), ")");
				getTableDataFilter(fk.getPkTable()).addWhereSql(sb.toString());
			}
		}
	}

	abstract void handleSpecialConstraints(TableDataFilter table, ResultSet rs) throws SQLException;

	abstract List<TableDataFilter> postProcess(List<TableDataFilter> tables);

	void printInsertSql(TableDataFilter table, PrintStream outStream) {
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(table.toSelectSql());) {

			ResultSetMetaData metaData = rs.getMetaData();

			while (rs.next()) {
				StringBuilder sbSqlInsert = new StringBuilder();
				sbSqlInsert.append("INSERT INTO ").append(table.getTable().toString()).append(" (");

				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					if (i > 1) {
						sbSqlInsert.append(", ");
					}
					sbSqlInsert.append(metaData.getColumnName(i));
				}
				sbSqlInsert.append(") VALUES (");
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					if (i > 1) {
						sbSqlInsert.append(", ");
					}
					sbSqlInsert.append(toSqlValueString(rs.getObject(i)));
				}
				sbSqlInsert.append(");");

				outStream.println(sbSqlInsert.toString());
			}

		} catch (ClassNotFoundException | SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	abstract String toSqlValueString(Object value);

	List<TableDataFilter> sortTables() {
		List<TableDataFilter> tables = new ArrayList<>(mapTables.values());
		Collections.sort(tables, new Comparator<TableDataFilter>() {

			@Override
			public int compare(TableDataFilter o1, TableDataFilter o2) {
				int depth1 = getDepth(o1);
				int depth2 = getDepth(o2);

				if (depth1 == depth2) {
					return o1.getTable().getName().compareTo(o2.getTable().getName());
				}
				return Integer.compare(depth1, depth2);
			}

			private int getDepth(TableDataFilter table) {
				int result = 0;
				for (DatabaseObject dependTable : table.getDependsOn()) {
					if (dependTable.equals(table.getTable())) {
						continue;
					}

					int depth = getDepth(mapTables.get(dependTable)) + 1;
					if (result < depth) {
						result = depth;
					}
				}
				return result;
			}
		});
		return tables;
	}

	boolean isFilterDone() {

		for (TableDataFilter table : mapTables.values()) {
			if (table.isFilterModified()) {
				return false;
			}
		}
		return true;
	}

	protected TableDataFilter getTableDataFilter(DatabaseObject table) {
		return mapTables.computeIfAbsent(table, TableDataFilter::new);
	}

	Connection getConnection() throws SQLException, ClassNotFoundException {

		Class.forName(jdbcDriver);
		return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
	}

	String getPrimaryKeyColumns(DatabaseObject table) {

		if (primaryKey == null) {
			primaryKey = loadPrimaryKey();
		}
		return primaryKey.get(table);
	}

	List<ForeignKey> getForeignKey(DatabaseObject table) {

		if (foreignKey == null) {
			foreignKey = loadForeignKey();
		}
		return foreignKey.stream().filter(c -> table.equals(c.getFkTable())).collect(Collectors.toList());
	}

	abstract Map<DatabaseObject, String> loadPrimaryKey();

	abstract List<ForeignKey> loadForeignKey();

}
