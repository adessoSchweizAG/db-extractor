package ch.adesso.dbextractor.core;

import java.util.LinkedList;
import java.util.List;

public class ForeignKey {

	private final DatabaseObject constraint;
	private final DatabaseObject fkTable;
	private final List<String> fkColumnNames = new LinkedList<>();
	private final DatabaseObject pkTable;
	private final List<String> pkColumnNames = new LinkedList<>();

	public ForeignKey(DatabaseObject constraint, DatabaseObject fkTable, DatabaseObject pkTable) {
		this.constraint = constraint;
		this.fkTable = fkTable;
		this.pkTable = pkTable;
	}

	public DatabaseObject getConstraint() {
		return constraint;
	}

	public DatabaseObject getFkTable() {
		return fkTable;
	}

	public List<String> getFkColumnNames() {
		return fkColumnNames;
	}

	public DatabaseObject getPkTable() {
		return pkTable;
	}

	public List<String> getPkColumnNames() {
		return pkColumnNames;
	}
}
