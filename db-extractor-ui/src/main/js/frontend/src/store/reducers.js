import { combineReducers } from 'redux';

import {
	FETCH_DRIVER_CLASS_NAMES_SUCCESS,
	SET_DATA_SOURCE_CONFIG
} from './actions';

function driverClassNames(state = [], action) {
	switch (action.type) {
		case FETCH_DRIVER_CLASS_NAMES_SUCCESS:
			return action.payload;
		default:
			return state;
	}
}

function dataSourceConfig(state = {}, action) {
	switch (action.type) {
		case SET_DATA_SOURCE_CONFIG:
			return action.payload;
		default:
			return state;
	}
}

export const rootReducer = combineReducers({
	driverClassNames,
	dataSourceConfig
});