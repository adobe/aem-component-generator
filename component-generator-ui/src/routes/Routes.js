import React from 'react';
import { Route, Switch } from 'react-router';
import { BrowserRouter as Router } from 'react-router-dom';
import ComponentBuilder from '../components/pages/ComponentBuilder';
import Footer from '../components/layout/Footer';
import Header from '../components/layout/Header';
import About from '../components/pages/About';
import DialogProperties from '../components/pages/DialogProperties';
import DialogTabs from '../components/pages/DialogTabs';
import Help from '../components/pages/Help';
import Home from '../components/pages/Home';
import GlobalConfiguration from '../components/pages/GlobalConfiguration';
import Sidebar from '../components/layout/Sidebar';
import PageNotFound from '../components/pages/PageNotFound';
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
                            <Route path={routes.compConfig} component={ComponentBuilder} />
                            <Route path={routes.dialogProperties} component={DialogProperties} />
                            <Route path={routes.dialogTabs} component={DialogTabs} />
                            <Route path={routes.about} component={About} />
                            <Route path={routes.help} component={Help} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </div>
                    <Footer />
                </div>
            </div>
        </Router>
    );
}

export default Routes;
