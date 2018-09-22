package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class TableDataFilterTest {

	private DbSupport dbSupport;

	@Before
	public void setup() {

		dbSupport = mock(DbSupport.class);
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
	public void toSelectSql() {
		TableDataFilter filter = new TableDataFilter("ITEM");

		assertEquals("SELECT * FROM ITEM", filter.toSelectSql(dbSupport));
		assertFalse("hasFilter", filter.hasFilter());

		filter.addWhereSql("ID = 1");
		assertTrue("hasFilter", filter.hasFilter());
		assertEquals("SELECT * FROM ITEM WHERE ID = 1", filter.toSelectSql(dbSupport));

		filter.addWhereSql("ID = 2");
		assertEquals("SELECT * FROM ITEM WHERE ID = 1 OR ID = 2", filter.toSelectSql(dbSupport));

		filter.addWhereInValue("ID", 1);
		assertEquals("SELECT * FROM ITEM WHERE ID = 1 OR ID = 2 OR ID IN (1)", filter.toSelectSql(dbSupport));

		filter.addWhereInValue("ID", 3, 2);
		assertEquals("SELECT * FROM ITEM WHERE ID = 1 OR ID = 2 OR ID IN (1, 2, 3)", filter.toSelectSql(dbSupport));
	}

	@Test
	public void toSelectSqlModified() {
		TableDataFilter filter = new TableDataFilter("ITEM", "ID");

		assertEquals("SELECT * FROM ITEM ORDER BY ID", filter.toSelectSqlModified(dbSupport));
		assertFalse("hasFilterModified", filter.hasFilterModified());

		filter.addWhereSql("ID = 1");
		filter.addWhereSql("ID = 2");
		filter.addWhereInValue("ID", 1);
		assertTrue("hasFilterModified", filter.hasFilterModified());
		assertEquals("SELECT * FROM ITEM WHERE ID = 1 OR ID = 2 OR ID IN (1) ORDER BY ID", filter.toSelectSqlModified(dbSupport));

		filter.resetFilterModified();
		assertFalse("hasFilterModified", filter.hasFilterModified());

		filter.addWhereInValue("ID", 3, 2);
		assertTrue("hasFilterModified", filter.hasFilterModified());
		assertEquals("SELECT * FROM ITEM WHERE ID IN (2, 3) ORDER BY ID", filter.toSelectSqlModified(dbSupport));
	}

}
