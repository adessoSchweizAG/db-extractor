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
	fetchMock.mockResponseOnce(JSON.stringify(["org.hsqldb.jdbc.JDBCDriver","org.postgresql.Driver"]));
	
	renderWithProvider(<DataSourceConfig />);
	const driverClassName = screen.getByLabelText('Driver Class Name');
	
	await waitForElement(() => getByText(driverClassName, 'org.hsqldb.jdbc.JDBCDriver'), { driverClassName });
	
	userEvent.selectOptions(driverClassName, 'org.postgresql.Driver');
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:postgresql:');
});

it('test connection', async () => {
	fetchMock.mockResponse(async req => {
		if (req.url.endsWith("/rest/driverClassNames")) {
			return JSON.stringify(["org.hsqldb.jdbc.JDBCDriver","org.postgresql.Driver"]);
		}
		return JSON.stringify({ "success": true });
	});
	
	renderWithProvider(<DataSourceConfig />);
	const driverClassName = screen.getByLabelText('Driver Class Name');
	
	await waitForElement(() => getByText(driverClassName, 'org.hsqldb.jdbc.JDBCDriver'), { driverClassName });
	
	userEvent.selectOptions(driverClassName, 'org.hsqldb.jdbc.JDBCDriver');
	expect(screen.getByLabelText('Url')).toHaveAttribute('value', 'jdbc:hsqldb:mem:memdb');
	
	await userEvent.type(screen.getByLabelText('Username'), 'SA');
	await userEvent.click(screen.getByText('test'));
	
	await expect(waitForElement(() => screen.getByText('success'))).resolves.toHaveStyle('color: green');
});
