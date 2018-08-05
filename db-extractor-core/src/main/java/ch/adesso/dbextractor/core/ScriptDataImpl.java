package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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

	public ScriptDataImpl(DbSupport dbSupport) {
		this.inputDbSupport = dbSupport;
	}

	@Override
	public void script(Collection<TableDataFilter> filters, DataOutput output) {

		output.initialize(filters);

		Map<DatabaseObject, TableDataFilter> mapTables = new HashMap<>();
		for (TableDataFilter table : filters) {
			mapTables.put(table.getTable(), table);
		}

		collectRowIdentifierRecursive(mapTables);
		List<TableDataFilter> tables = sortTables(mapTables);
		removeWithoutFilter(tables);
		setSortOrder(tables);

		for (TableDataFilter table : tables) {
			List<ForeignKey> foreignKeys = getForeignKeys(table.getTable());

			try (Connection con = inputDbSupport.getConnection();
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(table.toSelectSql(inputDbSupport))) {

				output.resultSet(table, foreignKeys, rs);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		output.finish();
	}

	private void collectRowIdentifierRecursive(Map<DatabaseObject, TableDataFilter> mapTables) {

		while (mapTables.values().stream().anyMatch(TableDataFilter::hasFilterModified)) {
			for (TableDataFilter table : new ArrayList<>(mapTables.values())) {
				if (table.hasFilterModified()) {
					List<ForeignKey> foreignKeys = getForeignKeys(table.getTable());
					try (Connection con = inputDbSupport.getConnection();
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery(table.toSelectSqlModified(inputDbSupport))) {

						table.resetFilterModified();
						while (rs.next()) {
							handleForeignKeyConstraints(mapTables, table, foreignKeys, rs);
						}

					} catch (SQLException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
	}

	void handleForeignKeyConstraints(Map<DatabaseObject, TableDataFilter> mapTables,
			TableDataFilter table, List<ForeignKey> foreignKeys, ResultSet rs) throws SQLException {
		
		for (ForeignKey fk : foreignKeys) {
			
			if (fk.getFkColumnNames().size() == 1) {
				Object columnValue = rs.getObject(fk.getFkColumnNames().get(0));
				if (columnValue != null) {
					mapTables.computeIfAbsent(fk.getPkTable(), TableDataFilter::new)
							.addWhereInValue(fk.getPkColumnNames().get(0), columnValue);
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
				mapTables.computeIfAbsent(fk.getPkTable(), TableDataFilter::new)
						.addWhereSql(sb.toString());
			}

			table.getDependsOn().add(fk.getPkTable());
		}
	}

	private List<TableDataFilter> sortTables(Map<DatabaseObject, TableDataFilter> mapTables) {
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

	private void removeWithoutFilter(Collection<TableDataFilter> tables) {

		for (Iterator<TableDataFilter> it = tables.iterator(); it.hasNext();) {
			TableDataFilter table = it.next();

			if (!table.hasFilter()) {
				it.remove();
			}
		}
	}

	private void setSortOrder(Collection<TableDataFilter> tables) {

		for (TableDataFilter table : tables) {
			if (table.getOrderBy() == null) {
				table.setOrderBy(getPrimaryKeyColumns(table.getTable()));
			}
		}
	}

	private String getPrimaryKeyColumns(DatabaseObject table) {

		if (primaryKey == null) {
			primaryKey = inputDbSupport.loadPrimaryKey();
		}
		return primaryKey.get(table);
	}

	private List<ForeignKey> getForeignKeys(DatabaseObject table) {

		if (foreignKey == null) {
			foreignKey = inputDbSupport.loadForeignKey();
		}
		return foreignKey.stream().filter(c -> table.equals(c.getFkTable())).collect(Collectors.toList());
	}

}
