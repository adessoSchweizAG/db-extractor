package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ScriptDataImplTest {

	private ScriptDataImpl scriptData;
	
	@Before
	public void setup() {

		DbSupport dbSupport = mock(DbSupport.class);
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

		scriptData = spy(new ScriptDataImpl(dbSupport));
	}

	@Test
	public void handleForeignKeyConstraints() throws SQLException {
		
		TableDataFilter pkTableDataFilter = new TableDataFilter("pkTableName");
		doReturn(pkTableDataFilter).when(scriptData).getTableDataFilter(any(DatabaseObject.class));
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		
		TableDataFilter fkTableDataFilter = new TableDataFilter("fkTableName");
		ForeignKey fk = new ForeignKey(null, fkTableDataFilter.getTable(), pkTableDataFilter.getTable());
		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");
		
		scriptData.handleForeignKeyConstraints(fkTableDataFilter, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM pkTableName WHERE pkColumnName1 IN ('fkColumnValue1')", pkTableDataFilter.toSelectSql());
	}
	
	@Test
	public void handleForeignKeyConstraintsNullValue() throws SQLException {
		
		ResultSet rs = mock(ResultSet.class);
		
		TableDataFilter fkTableDataFilter = new TableDataFilter("fkTableName");
		ForeignKey fk = new ForeignKey(null, fkTableDataFilter.getTable(), new DatabaseObject("pkTableName"));
		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");
		
		scriptData.handleForeignKeyConstraints(fkTableDataFilter, Collections.singletonList(fk), rs);
		
		verify(scriptData, Mockito.never()).getTableDataFilter(any(DatabaseObject.class));
	}
	
	@Test
	public void handleForeignKeyConstraintsCombined() throws SQLException {
		
		TableDataFilter pkTableDataFilter = new TableDataFilter("pkTableName");
		doReturn(pkTableDataFilter).when(scriptData).getTableDataFilter(any(DatabaseObject.class));
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		doReturn("fkColumnValue2").when(rs).getObject(eq("fkColumnName2"));
		
		TableDataFilter fkTableDataFilter = new TableDataFilter("fkTableName");
		ForeignKey fk = new ForeignKey(null, fkTableDataFilter.getTable(), pkTableDataFilter.getTable());
		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));
		
		scriptData.handleForeignKeyConstraints(fkTableDataFilter, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 = 'fkColumnValue2')",
				pkTableDataFilter.toSelectSql());
	}
	
	@Test
	public void handleForeignKeyConstraintsCombinedNullValue() throws SQLException {
		
		TableDataFilter pkTableDataFilter = new TableDataFilter("pkTableName");
		doReturn(pkTableDataFilter).when(scriptData).getTableDataFilter(any(DatabaseObject.class));
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		
		TableDataFilter fkTableDataFilter = new TableDataFilter("fkTableName");
		ForeignKey fk = new ForeignKey(null, fkTableDataFilter.getTable(), pkTableDataFilter.getTable());
		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));
		
		scriptData.handleForeignKeyConstraints(fkTableDataFilter, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM pkTableName WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 IS NULL)",
				pkTableDataFilter.toSelectSql());
	}
	
}
