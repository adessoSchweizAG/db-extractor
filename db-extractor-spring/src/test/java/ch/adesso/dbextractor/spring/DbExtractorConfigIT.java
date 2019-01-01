package ch.adesso.dbextractor.spring;

import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.OutputSqlScript;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.TableDataFilter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestDataSourceConfig.class, DbExtractorConfig.class })
public class DbExtractorConfigIT {

	@Autowired
	private ScriptData scriptData;

	@Autowired
	private DbSupport dbSupport;

	@Test
	public void scriptData() {
		List<TableDataFilter> list = Collections.singletonList(new TableDataFilter("ITEM").addWhereSql("InvoiceID = 0"));
		OutputSqlScript output = new OutputSqlScript(dbSupport);
		scriptData.script(list, output);

		String generateScript = output.toString();

		assertThat(generateScript, CoreMatchers.containsString("-- SELECT * FROM CUSTOMER WHERE ID IN (0) ORDER BY ID;"));
	}
}
