import { enableFetchMocks } from 'jest-fetch-mock';

import React from 'react';
import { Provider } from 'react-redux';
import { store } from './store/store';

import { render, screen, waitForElement } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom/extend-expect';

import TableDataFilters from './TableDataFilters';

window.baseUrl = 'http://localhost/';

enableFetchMocks();

function renderWithProvider(component) {
	return render(<Provider store={store}>{component}</Provider>);
}

beforeEach(() => {
	fetchMock.resetMocks();
});

it('render with provider', () => {
	renderWithProvider(<TableDataFilters />);
});

it('add, modify and remove', async () => {
	const { container } = renderWithProvider(<TableDataFilters />);
	
	userEvent.type(screen.getByPlaceholderText('catalog'), 'PUBLIC');
	userEvent.type(screen.getByPlaceholderText('schema'), 'PUBLIC');
	userEvent.type(screen.getByPlaceholderText('name'), 'PRODUCT');
	
	const addButton = container.querySelector('button > svg[data-icon="plus-square"]');
	expect(addButton).toBeInTheDocument();
	userEvent.click(addButton!);
	const removeButton = await waitForElement(() => container.querySelector('button > svg[data-icon="minus-square"]'), { container });
	
	userEvent.type(screen.getByDisplayValue('PRODUCT'), 'CUSTOMER');
	
	expect(removeButton).toBeInTheDocument();
	userEvent.click(removeButton!);
});

it('generate SQL', async () => {
	const { container } = renderWithProvider(<TableDataFilters />);
	
	userEvent.type(screen.getByPlaceholderText("name"), "PRODUCT");
	userEvent.click(container.querySelector('button > svg[data-icon="plus-square"]'));
	
	fetchMock.mockResponseOnce("INSERT INTO PRODUCT VALUES ('val')");
	
	const scriptDataButton = screen.getByText("generate SQL");
	expect(scriptDataButton).toBeInTheDocument();
	userEvent.click(scriptDataButton);
	
	await expect(screen.findByDisplayValue("INSERT INTO PRODUCT VALUES ('val')")).resolves.toBeInTheDocument();
});

it('download SQL', async () => {
	
	let promiseResolve;
	const promise = new Promise((resolve) => {
		promiseResolve = resolve;
	});
	
	URL.createObjectURL = jest.fn();
	URL.revokeObjectURL = promiseResolve;
	
	const { container } = renderWithProvider(<TableDataFilters />);
	
	userEvent.type(screen.getByPlaceholderText("name"), "PRODUCT");
	userEvent.click(container.querySelector('button > svg[data-icon="plus-square"]'));
	
	fetchMock.mockResponseOnce("INSERT INTO PRODUCT VALUES ('val')");
	
	const scriptDataButton = screen.getByText("download SQL");
	expect(scriptDataButton).toBeInTheDocument();
	userEvent.click(scriptDataButton);
	
	await promise;
});