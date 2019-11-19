import React from 'react';
import { Navbar, Container, Row, Col, Accordion, Card } from 'react-bootstrap';

import 'bootstrap/dist/css/bootstrap.min.css';

import DataSourceConfig from './DataSourceConfig';
import TableDataFilters from './TableDataFilters';

function App() {
	return <React.Fragment>
		<Navbar bg="dark" variant="dark">
			<Navbar.Brand>DB Extractor - React UI</Navbar.Brand>
		</Navbar>
		<Container fluid="true">
			<Row>
				<Col as="nav" className="sidebar bg-light" md="2">
					<div className="sidebar-sticky">
						<Accordion defaultActiveKey="datasourceConfig">
							<Card>
								<Accordion.Toggle as={Card.Header} eventKey="datasourceConfig">
									Datasource
								</Accordion.Toggle>
								<Accordion.Collapse eventKey="datasourceConfig">
									<Card.Body>
										<DataSourceConfig />
									</Card.Body>
								</Accordion.Collapse>
							</Card>
						</Accordion>
					</div>
				</Col>
				<Col md="10">
					<TableDataFilters />
				</Col>
			</Row>
		</Container>
		
	</React.Fragment>;
}

export default App;