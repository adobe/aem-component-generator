import React from 'react';
import { Route, Switch } from 'react-router';
import { BrowserRouter as Router } from 'react-router-dom';
import ComponentBuilder from '../components/ComponentBuilder';
import Header from '../components/layout/Header';
import Home from '../components/Home';
import GlobalConfiguration from '../components/GlobalConfiguration';
import Sidebar from '../components/layout/Sidebar';
import PageNotFound from '../components/PageNotFound';
import routes from '.';

function Routes() {
    return (
        <Router>
            <Header />
            <div className="container-fluid page-body-wrapper">
                <Sidebar />
                <div className="main-panel">
                    <div className="content-wrapper">
                        <Switch>
                            <Route exact path={routes.home} component={Home} />
                            <Route path={routes.config} component={GlobalConfiguration} />
                            <Route path={routes.builder} component={ComponentBuilder} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </div>
                </div>
            </div>
        </Router>
    );
}

export default Routes;
