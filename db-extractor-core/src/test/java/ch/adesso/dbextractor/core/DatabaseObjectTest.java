package ch.adesso.dbextractor.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatabaseObjectTest {

	@Test
	public void toStringNameOnly() {
		assertEquals("name", new DatabaseObject("name").toString());
	}

	@Test
	public void toStringAll() {
		assertEquals("catalog.schema.name", new DatabaseObject("catalog", "schema", "name").toString());
	}

	@Test
	public void toStringNoCatalog() {
		assertEquals("schema.name", new DatabaseObject("schema", "name").toString());
	}

	@Test
	public void toStringMissingSchema() {
		assertEquals("catalog..name", new DatabaseObject("catalog", null, "name").toString());
	}

	@Test
	public void equals() {
		DatabaseObject dbObject = new DatabaseObject("name");

		assertEquals("same object", dbObject, dbObject);
		assertEquals("same name", dbObject, new DatabaseObject("name"));
		assertEquals("missing catalog, schema", dbObject, new DatabaseObject("catalog", "schema", "name"));

		assertEquals("same name different caseing", dbObject, new DatabaseObject("NAME"));
	}
}
