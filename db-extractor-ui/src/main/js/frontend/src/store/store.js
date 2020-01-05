import { createStore, applyMiddleware } from 'redux';
import { createEpicMiddleware } from 'redux-observable';
import { rootReducer } from './reducers';
import { rootEpic } from './epics';

const epicMiddleware = createEpicMiddleware();
export const store = createStore(rootReducer, applyMiddleware(epicMiddleware));
epicMiddleware.run(rootEpic);