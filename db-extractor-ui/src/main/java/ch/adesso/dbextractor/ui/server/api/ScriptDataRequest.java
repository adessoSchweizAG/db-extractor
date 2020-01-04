package ch.adesso.dbextractor.ui.server.api;

import java.util.List;

public class ScriptDataRequest {

	private DataSourceConfig dataSourceConfig;
	private List<TableDataFilterDto> filters;

	public DataSourceConfig getDataSourceConfig() {
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
		this.dataSourceConfig = dataSourceConfig;
	}

	public List<TableDataFilterDto> getFilters() {
		return filters;
	}

	public void setFilters(List<TableDataFilterDto> filters) {
		this.filters = filters;
	}
}
