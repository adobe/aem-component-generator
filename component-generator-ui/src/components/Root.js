import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { toast, ToastContainer } from 'react-toastify';
import { Provider } from 'react-redux';
import { FETCH_CONFIGS, API_ROOT } from '../actions';
import Routes from '../routes/Routes';
import wretch from '../utils/wretch';

function Root({ store }) {
    const [showLoader, setShowLoader] = useState(false);

    useEffect(() => {
        setShowLoader(true);
        const initFormData = async () => {
            try {
                const result = await wretch.url(`${API_ROOT}/global`).get().json();
                store.dispatch({ type: FETCH_CONFIGS, payload: result });
                setTimeout(() => {
                    setShowLoader(false);
                }, 700);
            } catch (err) {
                let msg = '';
                if (err && err.message) {
                    msg = err.message;
                }
                toast(`Uh oh! The backend is having problems. ${msg}`, { type: toast.TYPE.ERROR });
            }
        };

        initFormData();
    }, [store]);

    return (
        <Provider store={store}>
            <div className={`loader-overlay ${showLoader ? 'd-visible' : ''}`}>
                <div className={`spinner ${showLoader ? 'd-visible' : ''}`} />
            </div>
            <ToastContainer position="top-center" autoClose={4000} closeOnClick draggable pauseOnHover />
            <div className="container-scroller">
                <Routes />
            </div>
        </Provider>
    );
}

Root.propTypes = {
    // eslint-disable-next-line react/forbid-prop-types
    store: PropTypes.object.isRequired,
};

export default Root;
