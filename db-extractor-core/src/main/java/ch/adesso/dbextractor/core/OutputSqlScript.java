package ch.adesso.dbextractor.core;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class OutputSqlScript implements DataOutput {

	private final DbSupport dbSupport;
	private final PrintWriter printWriter;
	private StringWriter stringWriter;

	public OutputSqlScript(DbSupport dbSupport) {
		stringWriter = new StringWriter();
		this.dbSupport = dbSupport;
		this.printWriter = new PrintWriter(stringWriter);
	}

	public OutputSqlScript(DbSupport dbSupport, PrintWriter printWriter) {
		this.dbSupport = dbSupport;
		this.printWriter = printWriter;
	}

	public OutputSqlScript(DbSupport dbSupport, OutputStream out) {
		this(dbSupport, new PrintWriter(out));
	}

	@Override
	public void initialize(List<TableDataFilter> filters) {

		printWriter.println("-- Input:");
		for (TableDataFilter table : filters) {
			if (table.hasFilter()) {
				printWriter.println("-- " + table.toSelectSql());
			}
		}
		printWriter.println();
		printWriter.flush();
	}

	@Override
	public void resultSet(TableDataFilter table, List<ForeignKey> foreignKeys, ResultSet rs) throws SQLException {

		printWriter.println();
		printWriter.println("-- " + table.toSelectSql() + ";");

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
				sbSqlInsert.append(dbSupport.toSqlValueString(rs.getObject(i)));
			}
			sbSqlInsert.append(");");

			printWriter.println(sbSqlInsert.toString());
		}
		printWriter.flush();
	}

	@Override
	public void finish() {
		printWriter.flush();
	}

	@Override
	public String toString() {
		if (stringWriter != null) {
			return stringWriter.toString();
		}
		return super.toString();
	}
}
