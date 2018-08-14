package ch.adesso.dbextractor.spring;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.TableDataFilter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestDataSourceConfig.class, DbExtractorConfig.class })
public class DbExtractorConfigTest {

	@Autowired
	private ScriptData scriptData;

	@Test
	public void scriptData() {
		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		scriptData.script(list, new PrintStream(out));

		String generateScript = out.toString();

		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;"));
	}
}
