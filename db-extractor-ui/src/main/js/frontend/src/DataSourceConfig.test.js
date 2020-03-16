import { enableFetchMocks } from 'jest-fetch-mock';

import React from 'react';
import { Provider } from 'react-redux';
import { store } from './store/store';

import { getByText, render, screen, waitForElement } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom/extend-expect';

import DataSourceConfig from './DataSourceConfig';

enableFetchMocks();

function renderWithProvider(component) {
	return { ...render(<Provider store={store}>{component}</Provider>) }
}

beforeEach(() => {
	fetchMock.resetMocks();
});

it('render with provider', () => {
	fetchMock.mockResponseOnce(JSON.stringify([]));
	
	renderWithProvider(<DataSourceConfig />);
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:<driver>:');
});

it('fetch driver classes', async () => {
	fetchMock.mockResponseOnce(JSON.stringify(["org.hsqldb.jdbc.JDBCDriver","org.postgresql.Driver","oracle.jdbc.driver.OracleDriver"]));
	
	renderWithProvider(<DataSourceConfig />);
	const driverClassName = screen.getByLabelText('Driver Class Name');
	
	await waitForElement(() => getByText(driverClassName, 'org.hsqldb.jdbc.JDBCDriver'), { driverClassName });
	
	userEvent.selectOptions(driverClassName, 'org.postgresql.Driver');
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:postgresql:');
	
	userEvent.selectOptions(driverClassName, 'oracle.jdbc.driver.OracleDriver');
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:postgresql:');
	
	expect(fetchMock.mock.calls.length).toEqual(1);
	expect(fetchMock.mock.calls[0]).toEqual(['/rest/driverClassNames', {
			headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
			method: 'GET' } ]);
});

it('test connection', async () => {
	fetchMock.mockResponseOnce(JSON.stringify(["org.hsqldb.jdbc.JDBCDriver"]));
	
	renderWithProvider(<DataSourceConfig />);
	const driverClassName = screen.getByLabelText('Driver Class Name');
	
	await waitForElement(() => getByText(driverClassName, 'org.hsqldb.jdbc.JDBCDriver'), { driverClassName });
	
	userEvent.selectOptions(driverClassName, 'org.hsqldb.jdbc.JDBCDriver');
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:hsqldb:mem:memdb');
	userEvent.type(screen.getByLabelText('Username'), 'SA');
	userEvent.type(screen.getByLabelText('Password'), 'secret');
	
	fetchMock.mockResponseOnce(() => new Promise(resolve => setTimeout(() => resolve({ body: JSON.stringify({ "success": true }) }), 100)));
	userEvent.click(screen.getByText('test'));
	expect(screen.getByText('testing ...')).toHaveStyle('color: black');
	
	await expect(waitForElement(() => screen.getByText('success'))).resolves.toHaveStyle('color: green');
	
	expect(fetch.mock.calls.length).toEqual(2);
	expect(fetch.mock.calls[1]).toEqual(['/rest/dataSourceConfig/dummy/test', {
			headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
			method: 'POST',
			body: '{"driverClassName":"org.hsqldb.jdbc.JDBCDriver","url":"jdbc:hsqldb:mem:memdb","username":"SA","password":"secret"}' } ]);
});

it('test connection fail', async () => {
	fetchMock.mockResponseOnce(JSON.stringify(["org.hsqldb.jdbc.JDBCDriver"]));
	
	renderWithProvider(<DataSourceConfig />);
	const driverClassName = screen.getByLabelText('Driver Class Name');
	await waitForElement(() => getByText(driverClassName, 'org.hsqldb.jdbc.JDBCDriver'), { driverClassName });
	
	userEvent.selectOptions(driverClassName, 'org.hsqldb.jdbc.JDBCDriver');
	
	fetchMock.mockResponseOnce(JSON.stringify({ "success": false, "message": "No suitable driver found for " }));
	
	userEvent.click(screen.getByText("test"));
	
	await expect(waitForElement(() => screen.getByText('No suitable driver found for'))).resolves.toHaveStyle('color: red');
	expect(fetch.mock.calls.length).toEqual(2);
	expect(fetch.mock.calls[1]).toEqual(['/rest/dataSourceConfig/dummy/test', {
			headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
			method: 'POST',
			body: '{"driverClassName":"org.hsqldb.jdbc.JDBCDriver","url":"jdbc:hsqldb:mem:memdb","username":""}' } ]);
});
