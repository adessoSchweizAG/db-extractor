package ch.adesso.dbextractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TableDataFilter {

	private final DatabaseObject table;
	private String orderBy;
	private Map<String, Set<Object>> mapFilters = new LinkedHashMap<>();
	private Map<String, Set<Object>> mapFiltersModified = new LinkedHashMap<>();
	private Set<DatabaseObject> dependsOn = new HashSet<>();

	public TableDataFilter(DatabaseObject table) {
		this.table = table;
	}

	public TableDataFilter(String tableName) {
		this(new DatabaseObject(tableName));
	}

	public TableDataFilter(String tableName, String orderBy) {
		this(tableName);
		this.orderBy = orderBy;
	}

	public DatabaseObject getTable() {
		return table;
	}

	public TableDataFilter addWhereSql(String sql) {

		Set<Object> set = mapFilters.computeIfAbsent(null, key -> new LinkedHashSet<>());
		if (set.add(sql)) {
			mapFiltersModified.computeIfAbsent(null, key -> new LinkedHashSet<>()).add(sql);
		}
		return this;
	}

	public TableDataFilter addWhereInValue(String columnName, Object... values) {

		Set<Object> set = mapFilters.computeIfAbsent(columnName, key -> new HashSet<>());
		for (Object value : values) {
			if (set.add(value)) {
				mapFiltersModified.computeIfAbsent(columnName, key -> new LinkedHashSet<>()).add(value);
			}
		}
		return this;
	}

	public String toSelectSql(DbSupport dbSupport) {
		return toSelectSql(dbSupport, mapFilters);
	}

	public String toSelectSqlModified(DbSupport dbSupport) {
		return toSelectSql(dbSupport, mapFiltersModified);
	}

	private String toSelectSql(DbSupport dbSupport, Map<String, Set<Object>> mapFilters) {

		StringBuilder sb = new StringBuilder();

		if (!mapFilters.isEmpty()) {
			for (Entry<String, Set<Object>> entry : mapFilters.entrySet()) {
				if (entry.getKey() == null) {
					for (Object value : entry.getValue()) {
						sb.append(" OR ").append(value);
					}
				}
				else {
					List<Object> sortedValues = new ArrayList<>(entry.getValue());
					Collections.sort(sortedValues, this::naturalSortComparator);

					sb.append(" OR ").append(entry.getKey()).append(" IN (");
					boolean isFirstValue = true;
					for (Object value : sortedValues) {
						if (!isFirstValue) {
							sb.append(", ");
						}
						isFirstValue = false;

						sb.append(dbSupport.toSqlValueString(value));
					}
					sb.append(")");
				}
			}
			sb.replace(0, 4, " WHERE ");
		}

		sb.insert(0, table.toString())
				.insert(0, "SELECT * FROM ");

		if (orderBy != null) {
			sb.append(" ORDER BY ").append(orderBy);
		}
		return sb.toString();
	}

	private int naturalSortComparator(Object o1, Object o2) {

		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return -1;
		} else if (o2 == null) {
			return 1;
		}

		else if (o1 instanceof Comparable && o1.getClass().equals(o2.getClass())) {
			return ((Comparable) o1).compareTo(o2);
		}
		return o1.getClass().getName().compareTo(o2.getClass().getName());
	}

	public boolean hasFilter() {
		return !mapFilters.isEmpty();
	}

	public boolean hasFilterModified() {
		return !mapFiltersModified.isEmpty();
	}

	public void resetFilterModified() {
		this.mapFiltersModified.clear();
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Set<DatabaseObject> getDependsOn() {
		return dependsOn;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + table + ")";
	}
}
