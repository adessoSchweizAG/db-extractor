package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.ResultSet;
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

public class ScriptDataImpl implements ScriptData {

	private final DbSupport inputDbSupport;

	private Map<DatabaseObject, String> primaryKey;
	private List<ForeignKey> foreignKey;
	private Map<DatabaseObject, TableDataFilter> mapTables = new HashMap<>();

	public ScriptDataImpl(DbSupport dbSupport) {
		this.inputDbSupport = dbSupport;
	}

	@Override
	public void script(List<TableDataFilter> filters, DataOutput output) {

		for (TableDataFilter table : filters) {
			mapTables.put(table.getTable(), table);
		}

		output.initialize(filters);

		List<TableDataFilter> tables = collectData();
		for (TableDataFilter table : tables) {
			List<ForeignKey> foreignKeys = getForeignKeys(table.getTable());

			try (Connection con = inputDbSupport.getConnection();
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(table.toSelectSql())) {

				output.resultSet(table, foreignKeys, rs);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		output.finish();
	}

	private List<TableDataFilter> collectData() {
		while (!isFilterDone()) {
			for (TableDataFilter table : new ArrayList<>(mapTables.values())) {
				if (table.isFilterModified()) {
					table.setFilterModified(false);

					List<ForeignKey> foreignKeys = getForeignKeys(table.getTable());
					try (Connection con = inputDbSupport.getConnection();
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery(table.toSelectSql());) {

						while (rs.next()) {
							handleForeignKeyConstraints(table, foreignKeys, rs);
						}

					} catch (SQLException e) {
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

		return tables;
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
						sb.append(fk.getPkColumnNames().get(i)).append(" = ").append(inputDbSupport.toSqlValueString(columnValue));
					}
					sb.append(" AND ");
				}
				sb.replace(sb.length() - 5, sb.length(), ")");
				getTableDataFilter(fk.getPkTable()).addWhereSql(sb.toString());
			}

			table.getDependsOn().add(fk.getPkTable());
		}
	}

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

	String getPrimaryKeyColumns(DatabaseObject table) {

		if (primaryKey == null) {
			primaryKey = inputDbSupport.loadPrimaryKey();
		}
		return primaryKey.get(table);
	}

	List<ForeignKey> getForeignKeys(DatabaseObject table) {

		if (foreignKey == null) {
			foreignKey = inputDbSupport.loadForeignKey();
		}
		return foreignKey.stream().filter(c -> table.equals(c.getFkTable())).collect(Collectors.toList());
	}

}
