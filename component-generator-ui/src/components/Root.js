import React from 'react';
import PropTypes from 'prop-types';
import { Provider } from 'react-redux';
import Routes from '../routes/Routes';

const Root = ({ store }) => (
    <Provider store={store}>
        <Routes />
    </Provider>
);

Root.propTypes = {
    // eslint-disable-next-line react/forbid-prop-types
    store: PropTypes.object.isRequired,
};

export default Root;
