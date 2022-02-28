import React from 'react';
import { connect, ConnectedProps } from 'react-redux';
import { Button, Form } from 'react-bootstrap';
import { fetchDriverClassNames, setDataSourceConfig } from './store/actions';
import { DataSourceConfig as DataSourceConfigType } from './types/DataSourceConfig';

import DbExtractorRestClient from './DbExtractorRestClient';
import { RootState, AppDispatch } from './store/store';

type DataSourceConfigProps = PropsFromRedux;

type DataSourceConfigState = {
	testResult?: {
		style: {
			color: string
		},
		message: string
	}
} & DataSourceConfigType;

class DataSourceConfig extends React.Component<DataSourceConfigProps, DataSourceConfigState> {
	
	constructor(props: DataSourceConfigProps) {
		super(props);
		this.componentDidMount = this.componentDidMount.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.handleTestConnection = this.handleTestConnection.bind(this);
		this.state = {};
	}
	
	componentDidMount() {
		this.props.fetchDriverClassNames();
	}
	
	handleChange({ target }: React.ChangeEvent<HTMLInputElement>): void {
		const name = target.name === "" ? target.id : target.name;
		const value = target.value;
		// console.log("handleChange: " + name + ": " + value);
		
		if (name === "driverClassName") {
			let urlPrefix = "";
			let url = "";
			if (value === "org.hsqldb.jdbc.JDBCDriver" || value === "org.hsqldb.jdbcDriver") {
				urlPrefix = "jdbc:hsqldb:";
				url = "jdbc:hsqldb:mem:memdb";
			}
			else if (value === "org.apache.derby.jdbc.AutoloadedDriver") {
				urlPrefix = "jdbc:derby:";
				url = "jdbc:derby:memory:memdb;create=true";
			}
			else if (value === "org.h2.Driver") {
				urlPrefix = "jdbc:h2:";
				url = "jdbc:h2:mem:";
			}
			else if (value === "org.postgresql.Driver") {
				urlPrefix = "jdbc:postgresql:";
				url = urlPrefix;
			}
			else if (value === "com.mysql.jdbc.Driver" || value === "com.mysql.cj.jdbc.Driver") {
				urlPrefix = "jdbc:mysql:";
				url = urlPrefix;
			}
			else {
				urlPrefix = "jdbc:";
				url = "jdbc:<driver>:";
			}
			
			this.setState((state) => {
				if (typeof state.url === "string" && state.url.indexOf(urlPrefix) === 0) {
					return { driverClassName: value };
				}
				return { driverClassName: value, url };
			});
		} else {
			this.setState({ [name]: value });
		}
	}
	
	handleTestConnection(): void {
		
		const dataSourceConfig = ({ id, name, driverClassName, url, username = "", password }: DataSourceConfigState): DataSourceConfigType => {
			return { id, name, driverClassName, url, username, password };
		};
		
		this.setState({ testResult: { style: { color: 'black' }, message: "testing ..." } });
		DbExtractorRestClient.dataSourceConfigTest(dataSourceConfig(this.state))
			.then(data => {
				if (data.success === true) {
					this.props.setDataSourceConfig(dataSourceConfig(this.state));
					return { testResult: { style: { color: 'green' }, message: "success" } };
				}
				return { testResult: { style: { color: 'red' }, message: data.message } };
			})
			.then(state => this.setState(state))
			.catch(console.log);
	}
	
	render() {
		return (
			<DataSourceConfigView {...this.state} {...this.props}
				handleChange={this.handleChange}
				handleTestConnection={this.handleTestConnection} />);
	}
}

function DataSourceConfigView({ driverClassNames = [],
	driverClassName = "", url = "jdbc:<driver>:", username = "", password = "",
	testResult = { style: {}, message: "" },
	handleChange, handleTestConnection
}: DataSourceConfigType & {
	driverClassNames?: string[],
	testResult?: { style: {}, message: string },
	handleChange: ((event: React.ChangeEvent<HTMLInputElement>) => void), handleTestConnection: (() => void)
}) {
	
	const optionDriverClassNames = driverClassNames.map(function(item) {
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

function mapStateToProps(state: RootState) {
	return {
		driverClassNames: state.driverClassNames
	};
}

function mapDispatchToProps(dispatch: AppDispatch) {
	return {
		fetchDriverClassNames: () => dispatch(fetchDriverClassNames()),
		setDataSourceConfig: (dataSourceConfig: DataSourceConfigType) => dispatch(setDataSourceConfig(dataSourceConfig))
	};
}

const connector = connect(mapStateToProps, mapDispatchToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

export default connector(DataSourceConfig);
