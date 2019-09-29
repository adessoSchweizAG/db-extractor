import React from 'react';
import { Button, Form } from 'react-bootstrap';

import DbExtractorRestClient from './DbExtractorRestClient';

class DataSourceConfig extends React.Component {
	
	constructor(props) {
		super(props);
		this.componentDidMount = this.componentDidMount.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleTestConnection = this.handleTestConnection.bind(this);
	}
	
	componentDidMount() {
		DbExtractorRestClient.fetchDriverClassNames()
		.then(data => {
			this.setState({ driverClassNames: data });
		})
		.catch(console.log);
	}
	
	handleChange({ target }) {
		const name = target.name === "" ? target.id : target.name;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		// console.log("handleChange: " + name + ": " + value);
		
		if (name === "driverClassName") {
			let urlPrefix = "";
			let url = "";
			if (value === "org.hsqldb.jdbc.JDBCDriver" || value === "org.hsqldb.jdbcDriver") {
				urlPrefix = "jdbc:hsqldb:";
				url = "jdbc:hsqldb:mem:memdb";
			}
			else if (value === "org.postgresql.Driver") {
				urlPrefix = "jdbc:postgresql:";
				url = "jdbc:postgresql:";
			}
			else {
				urlPrefix = "jdbc:";
				url = "jdbc:<driver>:";
			}
			
			this.setState((state, props) => {
				if (typeof state.url === "string" && state.url.indexOf(urlPrefix) === 0) {
					return { driverClassName: value };
				}
				return { driverClassName: value, url };
			});
		} else {
			this.setState({ [name]: value });
		}
	}
	
	handleTestConnection() {
		
		const dataSourceConfig = ({ id, name, driverClassName, url, username, password }) => {
			return { id, name, driverClassName, url, username, password };
		};
		
		this.setState({ testResult: { style: { color: 'black' }, message: "testing ..." }});
		DbExtractorRestClient.dataSourceConfigTest(dataSourceConfig(this.state))
		.then(data => {
			if (data.success === true) {
				return { testResult: { style: { color: 'green' }, message: "success" }};
			}
			return { testResult: { style: { color: 'red' }, message: data.message }};
		})
		.then(state => this.setState(state))
		.catch(console.log);
	}
	
	render() {
		return <DataSourceConfigView {...this.state}
			handleChange={this.handleChange}
			handleTestConnection={this.handleTestConnection} />;
	}
}

function DataSourceConfigView({ driverClassNames = [],
		driverClassName = "", url = "jdbc:<driver>:", username = "", password = "",
		testResult = { style: {}, message: "" },
		handleChange, handleTestConnection }) {
	
	const optionDriverClassNames = driverClassNames.map(function (item) {
		if (driverClassName === item)
			return <option key={item} value={item} selected>{item}</option>;
		else
			return <option key={item} value={item}>{item}</option>;
	});
	
	return (
		<React.Fragment>
			<Form>
				<Form.Group controlId="driverClassName">
					<Form.Label>Driver Class Name</Form.Label>
					<Form.Control as="select" value={driverClassName} onChange={handleChange} >
						<option key="blank" />
						{optionDriverClassNames}
					</Form.Control>
				</Form.Group>
				
				<Form.Group controlId="url">
					<Form.Label>Url</Form.Label>
					<Form.Control value={url} onChange={handleChange} />
				</Form.Group>
				
				<Form.Group controlId="username">
					<Form.Label>Username</Form.Label>
					<Form.Control value={username} onChange={handleChange} />
				</Form.Group>
				
				<Form.Group controlId="password">
					<Form.Label>Password</Form.Label>
					<Form.Control value={password} type="password" onChange={handleChange} />
				</Form.Group>
			</Form>
			<Button onClick={handleTestConnection}>test</Button>
			<p style={testResult.style}>{testResult.message}</p>
		</React.Fragment>);
}

export default DataSourceConfig;