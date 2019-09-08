import React from 'react';
import { Navbar } from 'react-bootstrap';

import 'bootstrap/dist/css/bootstrap.min.css';

import DataSourceConfig from './DataSourceConfig';

function App() {
	return <React.Fragment>
		<Navbar bg="dark" variant="dark">
			<Navbar.Brand>DB Extractor - React UI</Navbar.Brand>
		</Navbar>
		<DataSourceConfig />
	</React.Fragment>;
}

export default App;