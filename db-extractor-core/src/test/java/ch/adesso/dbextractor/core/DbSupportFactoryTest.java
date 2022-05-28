package ch.adesso.dbextractor.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbSupportFactoryTest {

	@Mock
	private DataSource dataSource;

	@Test
	public void driverClassNames() {
		assertThat(DbSupportFactory.getDriverClassNames(), CoreMatchers.hasItems(
				DbSupportHsqlDb.DRIVER_CLASS_NAME, DbSupportPostgres.DRIVER_CLASS_NAME));
	}

	@Test
	public void createInstance() {
		
		for (String driverClassName : DbSupportFactory.getDriverClassNames()) {
			assertNotNull(DbSupportFactory.createInstance(driverClassName, dataSource));
		}
	}

}
