package ch.adesso.dbextractor.spring;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.DbSupportDerby;
import ch.adesso.dbextractor.core.DbSupportFactory;
import ch.adesso.dbextractor.core.DbSupportH2;
import ch.adesso.dbextractor.core.DbSupportHsqlDb;
import ch.adesso.dbextractor.core.DbSupportMySql;
import ch.adesso.dbextractor.core.DbSupportPostgres;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.ScriptDataImpl;

@Configuration
public class DbExtractorConfig {

	@Bean
	public DbSupport dbSupport(DataSource dataSource) throws SQLException {
		return DbSupportFactory.createInstance(dataSource);
	}

	@Bean
	@Profile("hsqldb")
	public DbSupport dbSupportHsqlDb(DataSource dataSource) {
		return new DbSupportHsqlDb(dataSource);
	}

	@Bean
	@Profile("postgres")
	public DbSupport dbSupportPostgres(DataSource dataSource) {
		return new DbSupportPostgres(dataSource);
	}

	@Bean
	@Profile("mysql")
	public DbSupport dbSupportMySql(DataSource dataSource) {
		return new DbSupportMySql(dataSource);
	}

	@Bean
	@Profile("h2")
	public DbSupport dbSupportH2(DataSource dataSource) {
		return new DbSupportH2(dataSource);
	}

	@Bean
	@Profile("derby")
	public DbSupport dbSupportDerby(DataSource dataSource) {
		return new DbSupportDerby(dataSource);
	}

	@Bean
	public ScriptData scriptData(DbSupport dbSupport) {
		return new ScriptDataImpl(dbSupport);
	}
}
