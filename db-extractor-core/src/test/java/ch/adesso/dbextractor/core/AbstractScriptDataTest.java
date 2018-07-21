package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractScriptDataTest {

	@Spy
	private AbstractScriptData scriptData = new AbstractScriptData(null, null, null, null) {
		
		@Override
		String toSqlValueString(Object value) {
			if (value == null) {
				return "NULL";
			}
			else if (value instanceof Boolean) {
				return (boolean) value ? "1" : "0";
			}
			else if (value instanceof Date) {
				return toSqlValueString((Date) value);
			}
			else if (value instanceof String) {
				return "'" + ((String) value).replace("'", "''") + "'";
			}
			return value.toString();
		}
		
		@Override
		List<TableDataFilter> postProcess(List<TableDataFilter> tables) {
			return tables;
		}
		
		@Override
		Map<String, String> loadPrimaryKey() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		List<ForeignKey> loadForeignKey() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		void handleSpecialConstraints(TableDataFilter table, ResultSet rs) throws SQLException {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Test
	public void handleForeignKeyConstraints() throws SQLException {
		
		TableDataFilter tableDataFilter = new TableDataFilter("test");
		doReturn(tableDataFilter).when(scriptData).getTableDataFilter(anyString());
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		
		ForeignKey fk = new ForeignKey(null, null, "fkTableName", "pkTableName");
		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");
		
		scriptData.handleForeignKeyConstraints(null, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM test WHERE pkColumnName1 IN ('fkColumnValue1')", tableDataFilter.toSelectSql());
	}
	
	@Test
	public void handleForeignKeyConstraintsNullValue() throws SQLException {
		
		ResultSet rs = mock(ResultSet.class);
		
		ForeignKey fk = new ForeignKey(null, null, "fkTableName", "pkTableName");
		fk.getFkColumnNames().add("fkColumnName1");
		fk.getPkColumnNames().add("pkColumnName1");
		
		scriptData.handleForeignKeyConstraints(null, Collections.singletonList(fk), rs);
		
		verify(scriptData, Mockito.never()).getTableDataFilter(anyString());
	}
	
	@Test
	public void handleForeignKeyConstraintsCombined() throws SQLException {
		
		TableDataFilter tableDataFilter = new TableDataFilter("test");
		doReturn(tableDataFilter).when(scriptData).getTableDataFilter(anyString());
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		doReturn("fkColumnValue2").when(rs).getObject(eq("fkColumnName2"));
		
		ForeignKey fk = new ForeignKey(null, null, "fkTableName", "pkTableName");
		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));
		
		scriptData.handleForeignKeyConstraints(null, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM test WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 = 'fkColumnValue2')",
				tableDataFilter.toSelectSql());
	}
	
	@Test
	public void handleForeignKeyConstraintsCombinedNullValue() throws SQLException {
		
		TableDataFilter tableDataFilter = new TableDataFilter("test");
		doReturn(tableDataFilter).when(scriptData).getTableDataFilter(anyString());
		
		ResultSet rs = mock(ResultSet.class);
		doReturn("fkColumnValue1").when(rs).getObject(eq("fkColumnName1"));
		
		ForeignKey fk = new ForeignKey(null, null, "fkTableName", "pkTableName");
		fk.getFkColumnNames().addAll(Arrays.asList("fkColumnName1", "fkColumnName2"));
		fk.getPkColumnNames().addAll(Arrays.asList("pkColumnName1", "pkColumnName2"));
		
		scriptData.handleForeignKeyConstraints(null, Collections.singletonList(fk), rs);
		
		assertEquals("SELECT * FROM test WHERE (pkColumnName1 = 'fkColumnValue1' AND pkColumnName2 IS NULL)",
				tableDataFilter.toSelectSql());
	}
	
}
