import './styles/majestic-admin/style.css';
import './styles/main.scss';
import 'react-toastify/dist/ReactToastify.css';
import React from 'react';
import { render } from 'react-dom';
import Root from './components/Root';
import configureStore from './utils/configureStore';
import * as serviceWorker from './serviceWorker';

const store = configureStore();

render(<Root store={store} />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
