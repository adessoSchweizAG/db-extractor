import React from 'react';
import './DataSourceConfig.css';

class DataSourceConfig extends React.Component {
	
	constructor(props) {
		super(props);
		this.componentDidMount = this.componentDidMount.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleTestConnection = this.handleTestConnection.bind(this);
	}
	
	componentDidMount() {
		fetch('/rest/driverClassNames', {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'GET'
		})
		.then(response => response.json())
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
		fetch('/rest/dataSourceConfig/dummy/test', {
			headers: { "Accept": "application/json", "Content-Type": "application/json" },
			method: 'POST',
			body: JSON.stringify(this.state)
		})
		.then(response => response.json())
		.then(data => {
			if (data.success === true) {
				this.setState({ testResult: { style: { color: 'green' }, message: "success" }});
			}
			else {
				this.setState({ testResult: { style: { color: 'red' }, message: data.message }})
			}
		})
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