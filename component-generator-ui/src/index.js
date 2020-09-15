import './styles/majestic-admin/style.css';
import './styles/main.scss';
import 'react-toastify/dist/ReactToastify.css';
import React from 'react';
import { render } from 'react-dom';
import { FETCH_CONFIGS, ROOT_URL } from './actions';
import Root from './components/Root';
import configureStore from './utils/configureStore';
import * as serviceWorker from './serviceWorker';
import wretch from './utils/wretch';

const store = configureStore();

const initFormData = async () => {
    try {
        const result = await wretch
            .url(`${ROOT_URL}`)
            .get()
            .json();
        store.dispatch({ type: FETCH_CONFIGS, payload: result });
    } catch (err) {
        // do something with err
    }
};

initFormData();

render(<Root store={store} />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
