import React from 'react';
import AdditionalAttributesForm from '../forms/AdditionalAttributesForm';
import ComponentAttributesForm from '../forms/ComponentAttributesForm';

function ComponentBuilder() {
    return (
        <div className="row">
            <ComponentAttributesForm />
            <AdditionalAttributesForm />
        </div>
    );
}

export default ComponentBuilder;
