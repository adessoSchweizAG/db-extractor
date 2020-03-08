import { enableFetchMocks } from 'jest-fetch-mock';

import DbExtractorRestClient from './DbExtractorRestClient';

enableFetchMocks();

beforeEach(() => {
	fetchMock.resetMocks();
})

test('get driverClassNames', () => {
	fetchMock.mockResponseOnce(JSON.stringify(["org.hsqldb.jdbc.JDBCDriver","org.postgresql.Driver"]));

	return DbExtractorRestClient.fetchDriverClassNames().then(result => {
		expect(result).toEqual(['org.hsqldb.jdbc.JDBCDriver', 'org.postgresql.Driver']);
	});
})