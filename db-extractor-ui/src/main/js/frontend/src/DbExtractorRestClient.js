function expandUrl(relativeUrl) {
	const url = new URL(relativeUrl, window.baseUrl);
	return url.toString();
}

const DbExtractorRestClient = {
	
	fetchDriverClassNames: function() {
		return fetch(expandUrl('rest/driverClassNames'), {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'GET'
		})
		.then(response => response.json());
	},
	
	dataSourceConfigTest: function(dataSourceConfig) {
		return fetch(expandUrl('rest/dataSourceConfig/dummy/test'), {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'POST',
			body: JSON.stringify(dataSourceConfig)
		})
		.then(response => response.json());
	}
}

export default DbExtractorRestClient;