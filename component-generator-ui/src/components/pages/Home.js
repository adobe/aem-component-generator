/* eslint max-len: 0 */
import React from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import routes from '../../routes';

function Home() {
    return (
        <div className="row">
            <div className="col-md-12 grid-margin">
                <h2>Welcome to the AEM Component Generator!</h2>
                <p className="mb-2">The no-code solution to generating new AEM components</p>
            </div>
            <div className="col-12 grid-margin">
                <div className="card">
                    <div className="card-body">
                        <h4 className="card-title">Let&apos;s get started</h4>
                        <div className="row">
                            <div className="col-md-6">
                                <h6>Steps to generating your component</h6>
                                <div className="py-2">
                                    <div className="card rounded border mb-2">
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-check-all text-success" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">Fill in global configurations</h6>
                                                        <p className="mb-0 text-muted">
                                                            Specify details about your codebase.
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.config}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">Global Config</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-check-all text-success" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">Fill in component details</h6>
                                                        <p className="mb-0 text-muted">
                                                            Specify the name and type of your component as well as other options.
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.compConfig}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">Component Config</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-check-all text-success" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">Add your components properties / fields</h6>
                                                        <p className="mb-0 text-muted">
                                                            Specify the fields your component will have in its edit dialog.
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.dialogProperties}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">Dialog properties</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-check-all text-success" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">Define your dialog tabs</h6>
                                                        <p className="mb-0 text-muted">
                                                            Specify which dialog tabs your properties belong to.
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.dialogTabs}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">Dialog Tabs</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="col-md-6">
                                <h6>Need help?</h6>
                                <div className="py-2">
                                    <div className="card rounded border mb-2">
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-help-circle" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">Help page</h6>
                                                        <p className="mb-0 text-muted">
                                                            Field input overload
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.help}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">Help!</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="card-body p-3">
                                            <div className="media">
                                                <i className="mdi icon-sm align-self-center mr-3 mdi-information-outline" />
                                                <div className="media-body row">
                                                    <div className="col">
                                                        <h6 className="mb-1">About page</h6>
                                                        <p className="mb-0 text-muted">
                                                            About this project
                                                        </p>
                                                    </div>
                                                    <div className="col">
                                                        <LinkContainer to={`${routes.about}`}>
                                                            <button className="m-2 btn btn-secondary" type="button">About</button>
                                                        </LinkContainer>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Home;
