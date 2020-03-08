package ch.adesso.dbextractor.ui.server.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.adesso.dbextractor.core.DatabaseObject;
import ch.adesso.dbextractor.core.DbSupport;
import ch.adesso.dbextractor.core.DbSupportFactory;
import ch.adesso.dbextractor.core.OutputSqlScript;
import ch.adesso.dbextractor.core.ScriptData;
import ch.adesso.dbextractor.core.ScriptDataImpl;
import ch.adesso.dbextractor.core.TableDataFilter;

@RestController
public class ScriptDataController extends BaseContoller {

	@PostMapping(path = "scriptData", produces = { MediaType.TEXT_PLAIN_VALUE })
	public String scriptData(@RequestBody ScriptDataRequest request) {

		DriverManagerDataSource dataSource = driverManagerDataSource(request.getDataSourceConfig());

		DbSupport dbSupport = DbSupportFactory.createInstance(request.getDataSourceConfig().getDriverClassName(), dataSource);
		ScriptData scriptData = new ScriptDataImpl(dbSupport);

		OutputSqlScript output = new OutputSqlScript(dbSupport);
		scriptData.script(tableDataFilter(request.getFilters()), output);
		return output.toString();
	}

	private DriverManagerDataSource driverManagerDataSource(DataSourceConfig dataSourceConfig) {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(dataSourceConfig.getDriverClassName());
		dataSource.setUrl(dataSourceConfig.getUrl());
		dataSource.setUsername(dataSourceConfig.getUsername());
		dataSource.setPassword(dataSourceConfig.getPassword());
		return dataSource;
	}

	private List<TableDataFilter> tableDataFilter(List<TableDataFilterDto> filters) {

		if (filters == null || filters.isEmpty()) {
			return Collections.emptyList();
		}

		List<TableDataFilter> result = new ArrayList<>();
		for (TableDataFilterDto dto : filters) {
			DatabaseObject table = new DatabaseObject(dto.getCatalog(), dto.getSchema(), dto.getName());
			TableDataFilter filter = new TableDataFilter(table);
			filter.addWhereSql("1 = 1");
			result.add(filter);
		}
		return result;
	}
}
