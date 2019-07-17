package ch.adesso.dbextractor.spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class TestDataSourceConfig {

	private EmbeddedDatabase database;

	@Bean(destroyMethod = "shutdown")
	@Profile({ "default", "hsqldb" })
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.setName("test")
				.addDefaultScripts();
		this.database = builder.build();
		return this.database;
	}
}
