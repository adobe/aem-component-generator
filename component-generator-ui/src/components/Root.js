import React from 'react';
import PropTypes from 'prop-types';
import { ToastContainer } from 'react-toastify';
import { Provider } from 'react-redux';
import Routes from '../routes/Routes';

const Root = ({ store }) => (
    <Provider store={store}>
        <ToastContainer position="top-center" autoClose={4000} closeOnClick draggable pauseOnHover />
        <div className="container-scroller">
            <Routes />
        </div>
    </Provider>
);

Root.propTypes = {
    // eslint-disable-next-line react/forbid-prop-types
    store: PropTypes.object.isRequired,
};

export default Root;
