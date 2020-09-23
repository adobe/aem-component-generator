import React, { useState } from 'react';
import join from 'lodash/join';
import { Dropdown, NavLink } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { LinkContainer } from 'react-router-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagic } from '@fortawesome/free-solid-svg-icons';
import { FETCH_CONFIGS, MOBILE_MENU_CLICK, API_ROOT } from '../../actions';
import routes from '../../routes';
import { validateGlobalData } from '../../utils/Utils';
import wretch from '../../utils/wretch';
import ConfirmModal from '../modals/ConfirmModal';
import FeedbackModal from '../modals/FeedbackModal';

function Header() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);
    const [confirmShow, setConfirmShow] = useState(false);
    const [modalShow, setModalShow] = useState(false);
    const [modalTitle, setModalTitle] = useState('Validation failed');
    const [modalDesc, setModalDesc] = useState('');

    const mobileMenuAction = () => {
        dispatch({ type: MOBILE_MENU_CLICK });
    };

    const saveAndGenerateAction = async () => {
        const result = validateGlobalData(global);
        if (result.valid) {
            try {
                await wretch.url(`${API_ROOT}/global`).put().json();
                setModalTitle('Success!');
                setModalDesc('Your component code was successfully generated.');
                setModalShow(true);
            } catch (err) {
                let msg = 'Server error occurred. Please check the logs.';
                if (err && err.message) {
                    msg = err.message;
                }
                setModalTitle('Uh-oh something went wrong');
                setModalDesc(msg);
                setModalShow(true);
            }
        } else {
            setModalDesc(`${result.message} ${join(result.invalidFields, ', ')}`);
            setModalShow(true);
        }
    };

    const resetAllFieldsAction = async () => {
        setConfirmShow(false);
        try {
            const result = await wretch.url(`${API_ROOT}/global`).delete().json();
            dispatch({ type: FETCH_CONFIGS, payload: result });
        } catch (err) {
            let msg = '';
            if (err && err.message) {
                msg = err.message;
            }
            setModalTitle('Uh-oh something went wrong');
            setModalDesc(msg);
            setModalShow(true);
        }
    };

    const handleResetClick = (event) => {
        event.preventDefault();
        setConfirmShow(true);
    };

    return (
        <nav className="navbar col-lg-12 col-12 p-0 fixed-top d-flex flex-row navbar-dark">
            <ConfirmModal
                title="Are you Sure?"
                description="This action will reset / remove the data-config.json file. If you want to save your current config, make a copy of the data-config.json file before proceeding."
                onHide={() => setConfirmShow(false)}
                onConfirm={resetAllFieldsAction}
                show={confirmShow}
            />
            <FeedbackModal
                title={modalTitle}
                description={modalDesc}
                show={modalShow}
                onHide={() => setModalShow(false)}
            />
            <div className="navbar-brand-wrapper d-flex justify-content-center">
                <div className="navbar-brand-inner-wrapper d-flex justify-content-between align-items-center w-100">
                    <LinkContainer to={`${routes.home}`}>
                        <a href="#home" className="navbar-brand brand-logo-white">
                            <FontAwesomeIcon icon={faMagic} />
                            {' '}
                            AEM Component Generator
                        </a>
                    </LinkContainer>
                    <LinkContainer to={`${routes.home}`}>
                        <a href="#home" className="navbar-brand brand-logo-mini">AEM Generator</a>
                    </LinkContainer>
                </div>
            </div>
            <div className="navbar-menu-wrapper d-flex align-items-center justify-content-end">
                <ul className="navbar-nav mr-lg-4 w-100">
                    <li className="nav-item mr-1">
                        <button onClick={saveAndGenerateAction} className="btn btn-warning btn-md mt-xl-0" type="button">
                            <i className="mdi mdi-content-save-all menu-icon" />
                            <span className="pl-1">Save &amp; Generate Component</span>
                        </button>
                    </li>
                    <li className="nav-item mr-1">
                        <button onClick={handleResetClick} className="btn btn-danger btn-md mt-xl-0" type="button">
                            <i className="mdi mdi-refresh menu-icon" />
                            <span className="pl-1">Reset all fields</span>
                        </button>
                    </li>
                </ul>
                <ul className="navbar-nav navbar-nav-right">
                    <li className="nav-item help mr-2">
                        <Dropdown>
                            <Dropdown.Toggle as={NavLink}>
                                <i className="mdi mdi-help-circle" />
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                                <LinkContainer to={`${routes.about}`}>
                                    <Dropdown.Item>About</Dropdown.Item>
                                </LinkContainer>
                                <Dropdown.Divider />
                                <LinkContainer to={`${routes.help}`}>
                                    <Dropdown.Item>Help</Dropdown.Item>
                                </LinkContainer>
                            </Dropdown.Menu>
                        </Dropdown>
                    </li>
                </ul>
                <button
                    className="navbar-toggler navbar-toggler-right d-lg-none align-self-center"
                    type="button"
                    onClick={mobileMenuAction}
                    data-toggle="offcanvas"
                >
                    <span className="mdi mdi-menu" />
                </button>
            </div>
        </nav>
    );
}

export default Header;
