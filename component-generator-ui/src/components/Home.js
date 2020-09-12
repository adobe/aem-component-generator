import React, { useState } from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import { ROOT_URL } from '../actions';
import routes from '../routes';
import wretch from '../utils/wretch';

function Home() {
    const [data, setData] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const loadData = async () => {
        try {
            setIsLoading(true);
            const result = await wretch
                .url(`${ROOT_URL}`)
                .get()
                .json();
            setIsLoading(false);
            setData(result.message);
        } catch (err) {
            setIsLoading(false);
            // eslint-disable-next-line no-console
            console.error('Something went wrong', err);
        }
    };

    return (
        <div className="row">
            <div className="col-lg-3">
                <h1>Welcome!</h1>
                <LinkContainer to={`${routes.config}`}>
                    <button className="m-2 btn btn-primary" type="button">Config</button>
                </LinkContainer>
                <button onClick={loadData} className="m-2 btn btn-primary" type="button">Load api data</button>
                {(isLoading && !data) ? (
                    <p>Loading...</p>
                ) : (
                    <div className={data !== '' ? '' : 'text-hide'}>
                        <p>Success</p>
                        <p>{data}</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Home;
