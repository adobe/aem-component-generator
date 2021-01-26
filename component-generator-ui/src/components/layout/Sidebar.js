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
                <li className={location.pathname === routes.home ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.home}`}>
                        <a className="nav-link" href="#config">
                            <i className="mdi mdi-home menu-icon" />
                            <span className="menu-title">Home</span>
                        </a>
                    </LinkContainer>
                </li>
                <li className={location.pathname === routes.config ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.config}`}>
                        <a className="nav-link" href="#config">
                            <i className="mdi mdi-cogs menu-icon" />
                            <span className="menu-title">Global Configs</span>
                        </a>
                    </LinkContainer>
                </li>
                <li className={location.pathname === routes.compConfig ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.compConfig}`}>
                        <a className="nav-link" href="#builder">
                            <i className="mdi mdi-pencil menu-icon" />
                            <span className="menu-title">Component Configs</span>
                        </a>
                    </LinkContainer>
                </li>
                <li className={location.pathname === routes.dialogProperties ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.dialogProperties}`}>
                        <a className="nav-link" href="#dialogProperties">
                            <i className="mdi mdi-database-settings menu-icon" />
                            <span className="menu-title">Dialog Properties</span>
                        </a>
                    </LinkContainer>
                </li>
                <li className={location.pathname === routes.dialogTabs ? 'nav-item active' : 'nav-item'}>
                    <LinkContainer to={`${routes.dialogTabs}`}>
                        <a className="nav-link" href="#dialogTabs">
                            <i className="mdi mdi-tab menu-icon" />
                            <span className="menu-title">Dialog Tab Builder</span>
                        </a>
                    </LinkContainer>
                </li>
            </ul>
        </div>
    );
}

export default Sidebar;
