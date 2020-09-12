import React from 'react';
import { useSelector } from 'react-redux';
import { useLocation } from 'react-router';
import { LinkContainer } from 'react-router-bootstrap';
import routes from '../../routes';

function Sidebar() {
    const location = useLocation();
    const mobileState = useSelector((state) => state.mobileAction);

    return (
        <div className={mobileState.menuOpen ? 'active sidebar sidebar-offcanvas' : 'sidebar sidebar-offcanvas' }>
            <ul className="nav">
                <li className={location.pathname === routes.config ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.config}`}>
                        <a className="nav-link" href="#config">
                            <i className="mdi mdi-wrench menu-icon" />
                            <span className="menu-title">Global Config</span>
                        </a>
                    </LinkContainer>
                </li>
                <li className={location.pathname === routes.builder ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.builder}`}>
                        <a className="nav-link" href="#builder">
                            <i className="mdi mdi-pencil menu-icon" />
                            <span className="menu-title">Component Builder</span>
                        </a>
                    </LinkContainer>
                </li>
            </ul>
        </div>
    );
}

export default Sidebar;
