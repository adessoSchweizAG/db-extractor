import React from 'react';
import { connect, ConnectedProps } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlusSquare, faMinusSquare } from '@fortawesome/free-solid-svg-icons';
import { Button, Form, Col } from 'react-bootstrap';

import DbExtractorRestClient from './DbExtractorRestClient';
import { RootState } from './store/store';
import { TableDataFilter as TableDataFilterType } from './types/TableDataFilter';

type TableDataFiltersProps = PropsFromRedux;

type TableDataFiltersState = {
	tableDataFilters: TableDataFilterType[],
	generatedScript: string
};

class TableDataFilters extends React.Component<TableDataFiltersProps, TableDataFiltersState> {

	constructor(props: TableDataFiltersProps) {
		super(props);
		this.handleAddTableDataFilter = this.handleAddTableDataFilter.bind(this);
		this.handleChangeTableDataFilter = this.handleChangeTableDataFilter.bind(this);
		this.handleRemoveTableDataFilter = this.handleRemoveTableDataFilter.bind(this);
		this.handleScriptData = this.handleScriptData.bind(this);
		this.handleScriptDataDownload = this.handleScriptDataDownload.bind(this);
		
		this.state = { tableDataFilters: [], generatedScript: "" };
	}
	
	handleAddTableDataFilter(tableDataFilter: TableDataFilterType): void {
		
		this.setState((state, props) => {
			return { tableDataFilters: state.tableDataFilters.concat(tableDataFilter)};
		});
	}
	
	handleChangeTableDataFilter(tableDataFilter: TableDataFilterType, { target }: React.ChangeEvent<HTMLInputElement>): void {
		
		const name = target.name === "" ? target.id : target.name;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		
		this.setState((state, props) => {
			return { tableDataFilters: state.tableDataFilters.map(item => { return item === tableDataFilter ? { ...item, [name]: value } : item }) };
		});
	}
	
	handleRemoveTableDataFilter(tableDataFilter: TableDataFilterType): void {
		
		this.setState((state, props) => {
			return { tableDataFilters: state.tableDataFilters.filter(item => { return item !== tableDataFilter }) };
		});
	}
	
	handleScriptData(): void {
		DbExtractorRestClient.scriptData(this.props.dataSourceConfig, this.state.tableDataFilters)
		.then(data => {
			return { generatedScript: data };
		})
		.then(state => this.setState(state))
		.catch(console.log);
	}
	
	handleScriptDataDownload(): void {
		DbExtractorRestClient.scriptData(this.props.dataSourceConfig, this.state.tableDataFilters)
		.then(data => {
			let blob = new Blob([data], { type: "text/plain" });
			let url = URL.createObjectURL(blob);
			let a = document.createElement('a');
			a.href = url;
			a.download = 'script.sql';
			const clickHandler = () => {
				setTimeout(() => {
						URL.revokeObjectURL(url);
						a.removeEventListener('click', clickHandler);
					}, 150);
			};
			a.addEventListener('click', clickHandler, false);
			a.click();
		})
		.catch(console.log);
	}
	
	render() {
		return (
			<React.Fragment>
				<Form>
					{(this.state.tableDataFilters || []).map((item, index) => {
						return <TableDataFilterView key={index}
								{...item}
								handleChange={(event) => this.handleChangeTableDataFilter(item, event) }
								handleRemoveTableDataFilter={() => this.handleRemoveTableDataFilter(item) } />
					})}
					<TableDataFilter handleAddTableDataFilter={this.handleAddTableDataFilter} />
					<Form.Row>
						<Col>
							<Button onClick={this.handleScriptData}>generate SQL</Button>
						</Col>
						<Col>
						<Button onClick={this.handleScriptDataDownload}>download SQL</Button>
					</Col>
					</Form.Row>
					<Form.Row>
						<Col>
							<Form.Control as="textarea" rows="3" readOnly={true} value={this.state.generatedScript} />
						</Col>
					</Form.Row>
				</Form>
			</React.Fragment>);
	}
}

type TableDataFilterProps = {
	handleAddTableDataFilter?: (state: TableDataFilterState) => void,
};

type TableDataFilterState = TableDataFilterType;

class TableDataFilter extends React.Component<TableDataFilterProps, TableDataFilterState> {
	
	constructor(props: TableDataFilterProps) {
		super(props);
		this.handleChange = this.handleChange.bind(this);
		this.handleAddTableDataFilter = this.handleAddTableDataFilter.bind(this);
	}
	
	handleChange({ target }: React.ChangeEvent<HTMLInputElement>): void {
		const name = target.name === "" ? target.id : target.name;
		const value = target.type === 'checkbox' ? target.checked : target.value;
		// console.log("handleChange: " + name + ": " + value);
		
		this.setState({ [name]: value });
	}
	
	handleAddTableDataFilter(): void {
		this.props.handleAddTableDataFilter?.(this.state);
		this.setState({ catalog: undefined, schema: undefined, name: undefined });
	}
	
	render() {
		return (
			<TableDataFilterView {...this.state}
				handleChange={this.handleChange}
				handleAddTableDataFilter={this.props.handleAddTableDataFilter && this.handleAddTableDataFilter} />);
	}
}

function TableDataFilterView({ catalog = "", schema = "", name = "",
		handleChange, handleAddTableDataFilter, handleRemoveTableDataFilter }:
		{
			handleChange: ((event: React.ChangeEvent<HTMLInputElement>) => void),
			handleAddTableDataFilter?: ((event: React.MouseEvent<HTMLButtonElement>) => void),
			handleRemoveTableDataFilter?: ((event: React.MouseEvent<HTMLButtonElement>) => void)
		} & TableDataFilterType) {
	return (
		<Form.Row>
			<Col><Form.Control id="catalog" placeholder="catalog" value={catalog} onChange={handleChange} /></Col>
			<Col><Form.Control id="schema" placeholder="schema" value={schema} onChange={handleChange} /></Col>
			<Col><Form.Control id="name" placeholder="name" value={name} onChange={handleChange} /></Col>
			<Col>{handleAddTableDataFilter && <Button onClick={handleAddTableDataFilter}><FontAwesomeIcon icon={faPlusSquare} /></Button>}
				{handleRemoveTableDataFilter && <Button onClick={handleRemoveTableDataFilter} variant="danger" ><FontAwesomeIcon icon={faMinusSquare} /></Button>}</Col>
		</Form.Row>);
}

function mapStateToProps(state: RootState) {
	return {
		dataSourceConfig: state.dataSourceConfig
	};
}

const connector = connect(mapStateToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

export default connector(TableDataFilters);
