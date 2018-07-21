package ch.adesso.dbextractor.core;

import java.util.Objects;

public class DatabaseObject {

	private final String catalog;
	private final String schema;
	private final String name;

	public DatabaseObject(String name) {
		this(null, null, name);
	}

	public DatabaseObject(String catalog, String schema, String name) {
		this.catalog = catalog;
		this.schema = schema;
		this.name = name;
	}

	public String getCatalog() {
		return catalog;
	}

	public String getSchema() {
		return schema;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (catalog != null) {
			sb.append(catalog);
		}
		if (sb.length() > 0) {
			sb.append(".");
		}
		if (schema != null) {
			sb.append(schema);
		}
		if (sb.length() > 0) {
			sb.append(".");
		}
		sb.append(name);
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DatabaseObject other = (DatabaseObject) obj;

		return Objects.equals(name, other.name)
				&& (catalog == null || other.catalog == null || Objects.equals(catalog, other.catalog))
				&& (schema == null || other.schema == null || Objects.equals(schema, other.schema));
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
