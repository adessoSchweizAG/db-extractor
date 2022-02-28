import { DataSourceConfig } from './types/DataSourceConfig';
import { DataSourceConfigTestResult } from './types/DataSourceConfigTestResult'
import { TableDataFilter } from './types/TableDataFilter'

function expandUrl(relativeUrl: string) {
	const url = new URL(relativeUrl, window.baseUrl);
	return url.toString();
}

const DbExtractorRestClient = {
	
	fetchDriverClassNames: function(): Promise<string[]> {
		return fetch(expandUrl('rest/driverClassNames'), {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'GET'
		})
		.then(response => response.json());
	},
	
	dataSourceConfigTest: function(dataSourceConfig: DataSourceConfig): Promise<DataSourceConfigTestResult> {
		return fetch(expandUrl('rest/dataSourceConfig/dummy/test'), {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'POST',
			body: JSON.stringify(dataSourceConfig)
		})
		.then(response => response.json());
	},
	
	scriptData: function(dataSourceConfig: DataSourceConfig, tableDataFilters: TableDataFilter[]): Promise<string> {
		return fetch(expandUrl('rest/scriptData'), {
			headers: { "Accept": "text/plain", "Content-Type": "application/json" },
			method: 'POST',
			body: JSON.stringify({ dataSourceConfig: dataSourceConfig, filters: tableDataFilters })
		})
		.then(response => response.text());
	}
}

export default DbExtractorRestClient;