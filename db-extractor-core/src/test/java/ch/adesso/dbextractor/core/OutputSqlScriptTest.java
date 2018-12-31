package ch.adesso.dbextractor.core;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class OutputSqlScriptTest {

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
	public void stringWriter() {
		OutputSqlScript output = new OutputSqlScript(dbSupport);

		generateOutput(output);
		assertThat(output.toString(), containsString("-- Input:"));
		assertThat(output.toString(), containsString("-- SELECT * FROM ITEM WHERE ID IN (1, 2)"));
	}

	@Test
	public void printWriter() {
		OutputStream out = new ByteArrayOutputStream();
		OutputSqlScript output = new OutputSqlScript(dbSupport, out);

		generateOutput(output);
		assertThat(out.toString(), containsString("-- Input:"));
		assertThat(out.toString(), containsString("-- SELECT * FROM ITEM WHERE ID IN (1, 2)"));
	}

	private void generateOutput(OutputSqlScript output) {
		List<TableDataFilter> filters = Collections.singletonList(
				new TableDataFilter("ITEM").addWhereInValue("ID", 1, 2));
		output.initialize(filters);
	}
}
