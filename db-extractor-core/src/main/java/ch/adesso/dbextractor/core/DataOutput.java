package ch.adesso.dbextractor.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface DataOutput {

	void initialize(Collection<TableDataFilter> filters);

	void resultSet(TableDataFilter table, List<ForeignKey> foreignKeys, ResultSet rs) throws SQLException;

	void finish();

}
