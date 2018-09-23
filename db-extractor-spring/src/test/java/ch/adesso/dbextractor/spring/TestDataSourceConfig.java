package ch.adesso.dbextractor.spring;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class TestDataSourceConfig {

	private EmbeddedDatabase database;

	@Bean
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.setName("test")
				.addDefaultScripts();
		this.database = builder.build();
		return this.database;
	}

	@PreDestroy
	public void close() {
		if (this.database != null) {
			this.database.shutdown();
		}
	}

}
