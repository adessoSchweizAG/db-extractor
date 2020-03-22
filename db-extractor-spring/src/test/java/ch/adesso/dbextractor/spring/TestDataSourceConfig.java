package ch.adesso.dbextractor.spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class TestDataSourceConfig {

	@Bean(destroyMethod = "shutdown")
	@Profile({ "default", "hsqldb" })
	public DataSource dataSourceHsqlDb() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.generateUniqueName(true)
				.addDefaultScripts();
		return builder.build();
	}

	@Bean(destroyMethod = "shutdown")
	@Profile("h2")
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addDefaultScripts();
		return builder.build();
	}
}
