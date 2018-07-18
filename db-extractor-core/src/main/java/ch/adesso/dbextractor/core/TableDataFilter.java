package ch.adesso.dbextractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class TableDataFilter  {

    private final String tableName;
    private String orderBy;
    private Map<String, Set<Object>> mapFilters = new LinkedHashMap<>();
    private Set<String> dependsOn = new HashSet<>();
    private boolean filterModified;


    public TableDataFilter(String tableName) {
        this.tableName = tableName;
    }

    public TableDataFilter(String tableName, String orderBy) {
        this.tableName = tableName;
        this.orderBy = orderBy;
    }

    public String getTableName() {
        return tableName;
    }

    public TableDataFilter addWhereSql(String sql) {
    	
    	Set<Object> set = mapFilters.computeIfAbsent(null, key -> new HashSet<>());
        filterModified = set.add(sql) || filterModified;
        return this;
    }

    public TableDataFilter addWhereInValue(String columnName, Object... values) {
    	
    	Set<Object> set = mapFilters.computeIfAbsent(columnName, key -> new HashSet<>());
        for (Object value : values) {
            filterModified = set.add(value) || filterModified;
        }
        return this;
    }

    public String toSelectSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(tableName);

        boolean isFirstKey = true;
        for (Entry<String, Set<Object>> entry : mapFilters.entrySet()) {
            sb.append(isFirstKey ? " WHERE " : " OR ");

            if (entry.getKey() == null) {
                boolean isFirstValue = true;
                for (Object value : entry.getValue()) {
                    if (!isFirstValue) {
                        sb.append(" OR ");
                    }
                    sb.append(value);
                    isFirstValue = false;
                }
            }
            else {
                sb.append(entry.getKey()).append(" IN (");
                
                List<Object> sortedValues = new ArrayList<>(entry.getValue());
                Collections.sort(sortedValues, (o1, o2) -> {

                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    else if (o1 == null) {
                        return -1;
                    }
                    else if (o2 == null) {
                        return 1;
                    }

                    if (o1 instanceof Long && o2 instanceof Long) {
                        return Long.compare((Long)o1, (Long)o2);
                    }
                    else if (o1 instanceof Comparable && o1.getClass().equals(o2.getClass())) {
                        return ((Comparable)o1).compareTo(o2);
                    }
                    return o1.getClass().getName().compareTo(o2.getClass().getName());
                });
                
                boolean isFirstValue = true;
                for (Object value : sortedValues) {
                    if (!isFirstValue) {
                        sb.append(", ");
                    }
                    isFirstValue = false;
                    
                    if (value == null) {
                        sb.append("NULL");
                        return "NULL";
                    }
                    else if (value instanceof Boolean) {
                        sb.append((boolean) value ? "1" : "0");
                    }
                    else if (value instanceof String) {
                        sb.append("'" + ((String) value).replace("'", "''") + "'");
                    }
                    else {
                        sb.append(value);
                    }
                }
                sb.append(")");
            }
            isFirstKey = false;
        }

        if (orderBy != null) {
            sb.append(" ORDER BY ").append(orderBy);
        }
        return sb.toString();
    }

    public boolean isFilterModified() {
        return filterModified;
    }

    public void setFilterModified(boolean filterModified) {
        this.filterModified = filterModified;
    }
    
    public boolean hasFilter() {
        return !mapFilters.isEmpty();
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
    }

}
