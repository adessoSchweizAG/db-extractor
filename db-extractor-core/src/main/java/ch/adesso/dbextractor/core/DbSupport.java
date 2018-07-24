package ch.adesso.dbextractor.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DbSupport {

	Connection getConnection() throws SQLException;

	Map<DatabaseObject, String> loadPrimaryKey();

	List<ForeignKey> loadForeignKey();

	String toSqlValueString(Object value);

}
