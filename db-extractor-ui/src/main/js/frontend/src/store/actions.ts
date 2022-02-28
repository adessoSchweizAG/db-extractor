import { DataSourceConfig } from "../types/DataSourceConfig";

export const FETCH_DRIVER_CLASS_NAMES = 'FETCH_DRIVER_CLASS_NAMES';
export const FETCH_DRIVER_CLASS_NAMES_SUCCESS = 'FETCH_DRIVER_CLASS_NAMES_SUCCESS';

export const SET_DATA_SOURCE_CONFIG = 'SET_DATA_SOURCE_CONFIG';

export const fetchDriverClassNames = () => ({
	type: FETCH_DRIVER_CLASS_NAMES
});
export const fetchDriverClassNamesSuccess = (driverClassNames: string[]) => ({
	type: FETCH_DRIVER_CLASS_NAMES_SUCCESS,
	payload: driverClassNames
});

export const setDataSourceConfig = (dataSourceConfig: DataSourceConfig) => ({
	type: SET_DATA_SOURCE_CONFIG,
	payload: dataSourceConfig
});