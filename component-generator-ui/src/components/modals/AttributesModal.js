/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint react/forbid-prop-types: 0 */
import React, { Fragment, useEffect, useState } from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import { func, bool, object } from 'prop-types';
import { useUIDSeed } from 'react-uid';

function AttributesModal(props) {
    const seed = useUIDSeed();
    const [inputFields, setInputFields] = useState([
        { attribute: '', value: '' },
    ]);
    const {
        onHide, onConfirm, show, attributes,
    } = props;

    useEffect(() => {
        const initAttrData = () => {
            if (attributes && Object.keys(attributes).length > 0) {
                const attributesArr = [];
                Object.keys(attributes).map((keyName, keyIndex) => {
                    attributesArr.push({ attribute: keyName, value: attributes[keyName] });
                });
                setInputFields(attributesArr);
            }
        };

        initAttrData();
    }, [attributes]);

    const handleAddFields = () => {
        const values = [...inputFields];
        values.push({ attribute: '', value: '' });
        setInputFields(values);
    };

    const handleRemoveFields = (index) => {
        const values = [...inputFields];
        if (values.length === 1) {
            values.splice(index, 1);
            values.push({ attribute: '', value: '' });
            setInputFields(values);
        } else {
            values.splice(index, 1);
            setInputFields(values);
        }
    };

    const handleInputChange = (index, event) => {
        const values = [...inputFields];
        if (event.target.name === 'attribute') {
            values[index].attribute = event.target.value;
        } else {
            values[index].value = event.target.value;
        }

        setInputFields(values);
    };

    const saveEdit = () => {
        onConfirm(inputFields);
    };

    return (
        <Modal
            show={show}
            onHide={onHide}
            size="lg"
            aria-labelledby={seed('attributeModal')}
        >
            <Modal.Header closeButton>
                <Modal.Title id={seed('attributeModal')}>
                    Add or Remove Property Attributes
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <form onSubmit={saveEdit}>
                    <div className="form-row">
                        {inputFields.map((inputField, index) => (
                            <Fragment key={`${seed(inputField)}`}>
                                <div className="form-group col-sm-6">
                                    <label htmlFor={`${seed('attribute')}`}>Attribute key: </label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id={`${seed('attribute')}`}
                                        name="attribute"
                                        value={inputField.attribute}
                                        onChange={(event) => handleInputChange(index, event)}
                                    />
                                </div>
                                <div className="form-group col-sm-4">
                                    <label htmlFor={`${seed('value')}`}>Value: </label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id={`${seed('value')}`}
                                        name="value"
                                        value={inputField.value}
                                        onChange={(event) => handleInputChange(index, event)}
                                    />
                                </div>
                                <div className="form-group col-sm-2">
                                    <button
                                        className="btn btn-link mt-4"
                                        type="button"
                                        title="Remove Attribute"
                                        onClick={() => handleRemoveFields(index)}
                                    >
                                        <i className="mdi mdi-minus-circle" />
                                    </button>
                                    <button
                                        className="btn btn-link mt-4"
                                        type="button"
                                        title="Add new attribute"
                                        onClick={() => handleAddFields()}
                                    >
                                        <i className="mdi mdi-plus-circle" />
                                    </button>
                                </div>
                            </Fragment>
                        ))}
                    </div>
                </form>
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={onHide} variant="secondary">Cancel</Button>
                <Button onClick={saveEdit} variant="primary">Save Attributes!</Button>
            </Modal.Footer>
        </Modal>
    );
}

AttributesModal.propTypes = {
    onHide: func.isRequired,
    onConfirm: func.isRequired,
    show: bool.isRequired,
    attributes: object.isRequired,
};

export default AttributesModal;
