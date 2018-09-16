package ch.adesso.dbextractor.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DataOutput {

	void initialize(List<TableDataFilter> filters);

	void resultSet(TableDataFilter table, List<ForeignKey> foreignKeys, ResultSet rs) throws SQLException;

	void finish();

}
