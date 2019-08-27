import React from 'react';
import './DataSourceConfig.css';

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
		const name = target.name;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		this.setState({ [name]: value });
	}
	
	handleTestConnection() {
		this.setState({ testResult: { style: { color: 'black' }, message: "testing ..." }});
		DbExtractorRestClient.dataSourceConfigTest(this.state)
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
		<div className="dataSourceConfig">
			<h3>Datasource Configuration</h3>
			<label>Driver Class Name
				<select name="driverClassName" value={driverClassName} onChange={handleChange} >
					<option key="blank" />
					{optionDriverClassNames}
				</select>
			</label>
			<label>Url
				<input name="url" value={url} onChange={handleChange} />
			</label>
			<label>Username
				<input name="username" value={username} onChange={handleChange} />
			</label>
			<label>Password
				<input name="password" value={password} type="password" onChange={handleChange} />
			</label>
			<button onClick={handleTestConnection}>test</button>
			<p style={testResult.style}>{testResult.message}</p>
		</div>);
}

export default DataSourceConfig;