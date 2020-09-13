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
                        <p className="card-text">More about this project.</p>
                        <LinkContainer to={`${routes.home}`}>
                            <button className="m-2 btn btn-primary" type="button">Go back home</button>
                        </LinkContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default About;
