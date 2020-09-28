import React from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import routes from '../../routes';

function About() {
    return (
        <div className="container">
            <div className="mt-3">
                <div className="card">
                    <div className="card-body">
                        <h4 className="card-title">About</h4>
                        <p className="card-text">
                            The AEM component generator is an open source project&nbsp;
                            hosted on Github. See the&nbsp;
                            <a rel="noreferrer noopener" target="_blank" href="https://github.com/adobe/aem-component-generator">Github project page</a>
                            &nbsp;for more information.
                        </p>
                        <LinkContainer to={`${routes.home}`}>
                            <button className="mt-5 btn btn-primary" type="button">Go back home</button>
                        </LinkContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default About;
