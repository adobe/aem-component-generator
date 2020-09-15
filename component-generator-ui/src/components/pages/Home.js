import React, { useState } from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import { toast } from 'react-toastify';
import routes from '../../routes';

function Home() {
    const [data, setData] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const loadData = async () => {
        try {
            toast('Wow so easy !');
            setData('Test');
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
