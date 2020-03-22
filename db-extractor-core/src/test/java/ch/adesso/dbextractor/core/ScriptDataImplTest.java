package ch.adesso.dbextractor.core;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptDataImplTest {

	@Mock
	private DbSupport dbSupport;

	@Mock
	private ResultSet rs;

	@InjectMocks
	private ScriptDataImpl scriptData;

	private Map<DatabaseObject, TableDataFilter> mapTables = new HashMap<>();

	private DatabaseObject pkTable = new DatabaseObject("pkTableName");
	private DatabaseObject fkTable = new DatabaseObject("pkTableName");
	private ForeignKey fk = new ForeignKey(null, fkTable, pkTable);

	private TableDataFilter fkTableDataFilter = new TableDataFilter(fkTable);

	@Before
	public void setup() {

		doAnswer(invocation -> {
			Object value = invocation.getArgument(0);
			if (value == null) {
				return "NULL";
			} else if (value instanceof Boolean) {
				return (boolean) value ? "1" : "0";
			} else if (value instanceof String) {
				return "'" + ((String) value).replace("'", "''") + "'";
			}
			return value.toString();
		}).when(dbSupport).toSqlValueString(any());
	}

	@Test
	public void handleForeignKeyConstraints() throws SQLException {

		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");

		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));

		scriptData.handleForeignKeyConstraints(mapTables, fkTableDataFilter, Collections.singletonList(fk), rs);

		TableDataFilter pkTableDataFilter = mapTables.get(pkTable);
		assertNotNull(pkTableDataFilter);
		assertEquals("SELECT * FROM pkTableName WHERE pkColumnName1 IN ('fkColumnValue1')", pkTableDataFilter.toSelectSql(dbSupport));
		assertEquals("SELECT * FROM pkTableName WHERE pkColumnName1 IN ('fkColumnValue1')", pkTableDataFilter.toSelectSqlModified(dbSupport));

		assertThat("depends on", fkTableDataFilter.getDependsOn(), contains(pkTable));
	}

	@Test
	public void handleForeignKeyConstraintsNullValue() throws SQLException {

		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");

		scriptData.handleForeignKeyConstraints(mapTables, fkTableDataFilter, Collections.singletonList(fk), rs);

		assertThat(mapTables, not(hasKey(pkTable)));

		assertThat("depends on", fkTableDataFilter.getDependsOn(), contains(pkTable));
	}

	@Test
	public void handleForeignKeyConstraintsCombined() throws SQLException {

		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));

		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		doReturn("fkColumnValue2").when(rs).getObject(eq("fkColumnName2"));

		scriptData.handleForeignKeyConstraints(mapTables, fkTableDataFilter, Collections.singletonList(fk), rs);

		TableDataFilter pkTableDataFilter = mapTables.get(pkTable);
		assertNotNull(pkTableDataFilter);
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 = 'fkColumnValue2')",
				pkTableDataFilter.toSelectSql(dbSupport));
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 = 'fkColumnValue2')",
				pkTableDataFilter.toSelectSqlModified(dbSupport));
	}

	@Test
	public void handleForeignKeyConstraintsCombinedNullValue() throws SQLException {

		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));

		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));

		scriptData.handleForeignKeyConstraints(mapTables, fkTableDataFilter, Collections.singletonList(fk), rs);

		TableDataFilter pkTableDataFilter = mapTables.get(pkTable);
		assertNotNull(pkTableDataFilter);
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 IS NULL)",
				pkTableDataFilter.toSelectSql(dbSupport));
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 IS NULL)",
				pkTableDataFilter.toSelectSqlModified(dbSupport));
	}

}
