package ch.adesso.dbextractor.spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.DbSupportHsqlDb;
import ch.adesso.dbextractor.core.DbSupportPostgres;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.ScriptDataImpl;

@Configuration
public class DbExtractorConfig {

	@Bean
	@Profile({ "default", "hsqldb" })
	public DbSupport dbSupportHsqlDb(DataSource dataSource) {
		return new DbSupportHsqlDb(dataSource);
	}

	@Bean
	@Profile("postgres")
	public DbSupport dbSupportPostgres(DataSource dataSource) {
		return new DbSupportPostgres(dataSource);
	}

	@Bean
	public ScriptData scriptData(DbSupport dbSupport) {
		return new ScriptDataImpl(dbSupport);
	}
}
