import React from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import routes from '../../routes';

function Help() {
    return (
        <div className="container">
            <div className="mt-3">
                <div className="card">
                    <div className="card-body">
                        <h4 className="card-title">Help topics</h4>
                        <p className="card-text">How to use this project.</p>
                        <LinkContainer to={`${routes.home}`}>
                            <button className="m-2 btn btn-primary" type="button">Go back home</button>
                        </LinkContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Help;
