import PropTypes from 'prop-types';
import React, { useState } from 'react';
import SortableProperty from './SortableProperty';

function SortableTab({ index, propValues }) {
    const [removedTab, setRemovedTab] = useState(true);

    const onValidate = (values) => {
        const errors = {};
        if (!values.label) {
            errors.label = 'Required';
        }
        return errors;
    };
}

SortableTab.propTypes = {
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    propValues: PropTypes.object.isRequired,
};

export default SortableTab;
