import { ofType, combineEpics, Epic } from 'redux-observable';
import { map, flatMap } from 'rxjs/operators';
import DbExtractorRestClient from '../DbExtractorRestClient';

import {
	FETCH_DRIVER_CLASS_NAMES,
	fetchDriverClassNamesSuccess,
} from './actions';

const fetchDriverClassNamesEpic: Epic = action$ =>
	action$.pipe(
		ofType(FETCH_DRIVER_CLASS_NAMES),
		flatMap(() => DbExtractorRestClient.fetchDriverClassNames()),
		map(driverClassNames => fetchDriverClassNamesSuccess(driverClassNames))
	);

export const rootEpic = combineEpics(
	fetchDriverClassNamesEpic
);