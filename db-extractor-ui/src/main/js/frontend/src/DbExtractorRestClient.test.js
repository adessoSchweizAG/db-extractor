import { enableFetchMocks } from 'jest-fetch-mock';

import DbExtractorRestClient from './DbExtractorRestClient';

window.baseUrl = 'http://localhost/';

enableFetchMocks();

beforeEach(() => {
	fetchMock.resetMocks();
})

test('get driverClassNames', () => {
	const expectedValue = ["org.hsqldb.jdbc.JDBCDriver", "org.postgresql.Driver"];
	fetchMock.mockResponseOnce(JSON.stringify(expectedValue));

	return DbExtractorRestClient.fetchDriverClassNames().then(result => {
		expect(result).toEqual(expectedValue);
	});
})

test('datasourceConfig test - success', () => {
	const expectedValue = { "success": true };
	fetchMock.mockResponseOnce(JSON.stringify(expectedValue));

	const dataSourceConfig = { id: 0, name: "test", driverClassName: "org.hsqldb.jdbc.JDBCDriver", url: "jdbc:hqsldb:mem:memdb", username: "sa", password: "sa" };
	return DbExtractorRestClient.dataSourceConfigTest(dataSourceConfig).then(result => {
		expect(result).toEqual(expectedValue);
	});
})

test('datasourceConfig test - error', () => {
	const expectedValue = { "success": false, "message": "No suitable driver found for " };
	fetchMock.mockResponseOnce(JSON.stringify(expectedValue));

	const dataSourceConfig = { id: 0, name: "test", driverClassName: "org.hsqldb.jdbc.JDBCDriver", url: "jdbc:hqsldb:mem:memdb", username: "sa", password: "sa" };
	return DbExtractorRestClient.dataSourceConfigTest(dataSourceConfig).then(result => {
		expect(result).toEqual(expectedValue);
	});
})