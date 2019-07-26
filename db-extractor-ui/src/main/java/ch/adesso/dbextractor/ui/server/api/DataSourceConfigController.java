package ch.adesso.dbextractor.ui.server.api;

import java.sql.Connection;
import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.adesso.dbextractor.core.DbSupportFactory;

@RestController
public class DataSourceConfigController extends BaseContoller {

	@GetMapping(path = "driverClassNames", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Collection<String> driverClassNames() {
		return DbSupportFactory.getDriverClassNames();
	}

	@PostMapping(path = "dataSourceConfig/{name}/test", produces = { MediaType.APPLICATION_JSON_VALUE })
	public DataSourceConfigTestResult test(@RequestBody DataSourceConfig dataSourceConfig) {

		try {
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(dataSourceConfig.getDriverClassName());
			dataSource.setUrl(dataSourceConfig.getUrl());
			dataSource.setUsername(dataSourceConfig.getUsername());
			dataSource.setPassword(dataSourceConfig.getPassword());

			try (Connection con = dataSource.getConnection()) {
				con.commit();
			}
			return new DataSourceConfigTestResult(true, null);
		}
		catch (Exception exception) {
			return new DataSourceConfigTestResult(false, exception.getMessage());
		}
	}
}
