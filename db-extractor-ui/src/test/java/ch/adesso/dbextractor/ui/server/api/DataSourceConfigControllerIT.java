package ch.adesso.dbextractor.ui.server.api;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import ch.adesso.dbextractor.core.DbSupportPostgres;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQLDB)
public class DataSourceConfigControllerIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void getDriverClassNames() {
		List<String> driverClassNames = restTemplate.getForObject("/rest/driverClassNames", List.class);
		assertThat(driverClassNames, hasItems(DbSupportHsqlDb.DRIVER_CLASS_NAME, DbSupportPostgres.DRIVER_CLASS_NAME));
	}

	@Test
	public void dataSourceConfigTest() {

		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		dataSourceConfig.setDriverClassName(DbSupportHsqlDb.DRIVER_CLASS_NAME);
		dataSourceConfig.setUrl("jdbc:hsqldb:mem:memdb");
		dataSourceConfig.setUsername("SA");

		DataSourceConfigTestResult result = restTemplate.postForObject("/rest/dataSourceConfig/dummy/test",
				dataSourceConfig, DataSourceConfigTestResult.class);
		
		assertNotNull(result);
		assertTrue(result.isSuccess());
	}

	@Test
	public void dataSourceConfigTest_Fail() {

		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		dataSourceConfig.setDriverClassName(DbSupportHsqlDb.DRIVER_CLASS_NAME);
		dataSourceConfig.setUrl("jdbc:postgresql:blubber");
		dataSourceConfig.setUsername("SA");

		DataSourceConfigTestResult result = restTemplate.postForObject("/rest/dataSourceConfig/dummy/test",
				dataSourceConfig, DataSourceConfigTestResult.class);

		assertNotNull(result);
		assertFalse(result.isSuccess());
		assertThat(result.getMessage(), startsWith("No suitable driver found for "));
	}
}
