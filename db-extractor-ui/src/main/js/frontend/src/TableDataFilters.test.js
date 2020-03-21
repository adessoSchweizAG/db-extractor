import React from 'react';
import { Provider } from 'react-redux';
import { store } from './store/store';

import { render, screen, waitForElement } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom/extend-expect';

import TableDataFilters from './TableDataFilters';

function renderWithProvider(component) {
	return render(<Provider store={store}>{component}</Provider>);
}

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
	userEvent.click(addButton);
	const removeButton = await waitForElement(() => container.querySelector('button > svg[data-icon="minus-square"]'), { container });
	
	userEvent.type(screen.getByDisplayValue('PRODUCT'), 'CUSTOMER');
	
	expect(removeButton).toBeInTheDocument();
	userEvent.click(removeButton);
});