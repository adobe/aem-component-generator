import { createBrowserHistory } from 'history';
import { applyMiddleware, compose, createStore } from 'redux';
import reduxThunk from 'redux-thunk';
import createRootReducer from '../reducers';

export const history = createBrowserHistory();

export default function configureStore(preloadedState) {
    const store = createStore(
        createRootReducer(history), // root reducer with router state
        preloadedState,
        compose(
            applyMiddleware(
                reduxThunk,
            ),
        ),
    );

    return store;
}
