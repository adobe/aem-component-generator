import React from 'react';

function Footer() {
    const CURRENT_YEAR = new Date().getFullYear();
    return (
        <footer className="footer">
            <div>
                <span className="text-muted text-center text-sm-left d-block d-sm-inline-block">
                    Copyright &copy;&nbsp;
                    {CURRENT_YEAR}
                    &nbsp;
                    <a rel="noreferrer noopener" target="_blank" href="https://github.com/adobe/aem-component-generator">AEM Component Generator</a>
                    . All rights reserved.
                </span>
                <a rel="noreferrer noopener" target="_blank" href="https://www.adobe.com/" className="float-right d-block mr-3">
                    Adobe
                    {' '}
                    <i className="mdi mdi-adobe text-danger" />
                </a>
                <div className="clearfix" />
            </div>
        </footer>
    );
}

export default Footer;
