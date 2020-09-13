import React from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import routes from '../../routes';

function PageNotFound() {
    const pageNotFoundMessage = 'Page Not Found';
    return (
        <div className="container">
            <div className="mt-3">
                <div className="card">
                    <div className="card-body">
                        <h4 className="card-title">{pageNotFoundMessage}</h4>
                        <p className="card-text">We couldn&apos;t find the page you&apos;re looking for.</p>
                        <LinkContainer to={`${routes.home}`}>
                            <button className="m-2 btn btn-primary" type="button">Go back home</button>
                        </LinkContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default PageNotFound;
