import React from 'react';
import { useDispatch } from 'react-redux';
import { LinkContainer } from 'react-router-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagic, faQuestionCircle } from '@fortawesome/free-solid-svg-icons';
import { MOBILE_MENU_CLICK } from '../../actions';
import routes from '../../routes';

function Header() {
    const dispatch = useDispatch();

    function mobileMenuAction() {
        dispatch({ type: MOBILE_MENU_CLICK });
    }

    return (
        <nav className="navbar col-lg-12 col-12 p-0 fixed-top d-flex flex-row navbar-dark">
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
                        <button className="btn btn-primary btn-md mt-xl-0" type="button">
                            <i className="mdi mdi-floppy menu-icon" />
                            <span className="pl-1">Save changes</span>
                        </button>
                    </li>
                    <li className="nav-item mr-1">
                        <button className="btn btn-light btn-md mt-xl-0" type="button">
                            <i className="mdi mdi-refresh menu-icon" />
                            <span className="pl-1">Reset</span>
                        </button>
                    </li>
                    <li className="nav-item mr-1">
                        <button className="btn btn-warning btn-md mt-xl-0" type="button">
                            <i className="mdi mdi-content-save-all menu-icon" />
                            <span className="pl-1">Save &amp; Generate Component</span>
                        </button>
                    </li>
                </ul>
                <ul className="navbar-nav navbar-nav-right">
                    <li className="nav-item dropdown mr-2">
                        <a href="#home" className="navbar-brand brand-logo-mini">
                            <FontAwesomeIcon icon={faQuestionCircle} />
                        </a>
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
