package ch.adesso.dbextractor.ui.server.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.adesso.dbextractor.core.DbSupportHsqlDb;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQLDB)
public class ScriptDataControllerIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void scriptData() {

		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		dataSourceConfig.setDriverClassName(DbSupportHsqlDb.DRIVER_CLASS_NAME);
		dataSourceConfig.setUrl("jdbc:hsqldb:res:ch/adesso/dbextractor/demodb");
		dataSourceConfig.setUsername("SA");

		TableDataFilterDto filter = new TableDataFilterDto();
		filter.setName("PRODUCT");

		ScriptDataRequest request = new ScriptDataRequest();
		request.setDataSourceConfig(dataSourceConfig);
		request.setFilters(Collections.singletonList(filter));

		String script = restTemplate.postForObject("/rest/scriptData", request, String.class);
		assertThat(script, startsWith("-- Input:"));
		assertThat(script, containsString("INSERT INTO PRODUCT ("));
	}

}
