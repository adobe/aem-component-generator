/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint max-len: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0 */
import React, { useState } from 'react';
import forEach from 'lodash/forEach';
import { faGripLines } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FORM_ERROR } from 'final-form';
import PropTypes from 'prop-types';
import { Field, Form } from 'react-final-form';
import { useDispatch } from 'react-redux';
import Select from 'react-select';
import { toast } from 'react-toastify';
import { useUIDSeed } from 'react-uid';
import { SortableHandle, SortableElement } from 'react-sortable-hoc';
import { FETCH_CONFIGS, REMOVE_PROPERTY, API_ROOT } from '../../../actions';
import { FORM_TYPES } from '../../../utils/Constants';
import wretch from '../../../utils/wretch';
import AttributesModal from '../../modals/AttributesModal';

function SortableProperty({ index, propValues }) {
    const [outProp, setOutProp] = useState(true);
    const [attrModalShow, setAttrModalShow] = useState(false);
    const dispatch = useDispatch();
    const seed = useUIDSeed();
    const COMP_PROP_FIELDS = {
        field: 'field',
        description: 'description',
        javadoc: 'javadoc',
        type: 'type',
        label: 'label',
        jsonExpose: 'jsonExpose',
        modelName: 'modelName',
        useExistingModel: 'useExistingModel',
        jsonProperty: 'jsonProperty',
        attributes: 'attributes',
        items: 'items',
        id: 'id',
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.field) {
            errors.field = 'Required';
        }
        if (!values.type) {
            errors.type = 'Required';
        }
        return errors;
    };

    const onSubmit = async (values) => {
        const toastId = 'attrSubmit';
        try {
            const response = await wretch.url(`${API_ROOT}`).post({ ...values, updateProp: true }).json();
            const result = await wretch.url(`${API_ROOT}`).get().json();
            dispatch({ type: FETCH_CONFIGS, payload: result });
            return toast(`Success!: ${response.message}`, {
                toastId,
                type: toast.TYPE.SUCCESS,
            });
        } catch (err) {
            return { [FORM_ERROR]: err.message };
        }
    };

    const Condition = ({ when, is, children }) => (
        <Field name={when} subscription={{ value: true }}>
            {({ input: { value } }) => (value === is ? children : null)}
        </Field>
    );

    const handleSubmitAttrModal = async (attribs) => {
        setAttrModalShow(false);
        if (attribs && attribs.length > 0) {
            // attribs is an array of attributes and values,
            // convert it back to an object with keys/values
            const attributes = {};
            forEach(attribs, (val, key) => {
                if (val.value && val.value !== '') {
                    attributes[`${val.attribute}`] = val.value;
                }
            });
            try {
                // console.log('Submitting attributes: ', { ...propValues, attributes: { ...attributes }, updateProp: true });
                await wretch
                    .url(`${API_ROOT}`)
                    .post({ ...propValues, attributes: { ...attributes }, updateProp: true })
                    .json();
                const result = await wretch.url(`${API_ROOT}`).get().json();
                dispatch({ type: FETCH_CONFIGS, payload: result });
                toast('Your property attributes have been saved.', { type: toast.TYPE.SUCCESS });
            } catch (err) {
                let msg = '';
                if (err && err.message) {
                    msg = err.message;
                }
                toast(`Uh oh! The backend is having problems processing your update. ${msg}`, { type: toast.TYPE.ERROR, position: 'top-center' });
            }
        }
    };

    const handleAttrModalShow = (event) => {
        event.preventDefault();
        setAttrModalShow(true);
    };

    const removePropAction = async () => {
        // animate a fade out on deletion of property
        setOutProp(false);
        await wretch
            .url(`${API_ROOT}`)
            .post({ ...propValues, removeProp: true })
            .json();
        // update global state to update properties
        setTimeout(() => {
            dispatch({ type: REMOVE_PROPERTY, payload: propValues });
        }, 550);
    };

    const DragHandle = SortableHandle(() => (
        <i title="Click/hold to drag and re-order" className="dragIcon">
            <FontAwesomeIcon icon={faGripLines} />
        </i>
    ));
    const SortableItem = SortableElement(() => (
        <div className={`card rounded mb-2 ${outProp ? '' : 'animated fadeOut'}`}>
            <AttributesModal
                onConfirm={handleSubmitAttrModal}
                attributes={propValues.attributes || {}}
                show={attrModalShow}
                onHide={() => setAttrModalShow(false)}
            />
            <div className="card-body p-3">
                <div className="media align-items-center">
                    <DragHandle />
                    <div className="media-body">
                        <Form
                            initialValues={{ ...propValues }}
                            onSubmit={onSubmit}
                            validate={onValidate}
                            render={({
                                submitError, handleSubmit, reset, submitting, pristine,
                            }) => (
                                <form className="small-margin-form" onSubmit={handleSubmit}>
                                    {submitError && (
                                        <div className="alert alert-danger">
                                            <strong>Error!</strong>
                                            {' '}
                                            {submitError}
                                        </div>
                                    )}
                                    <Field name={`${COMP_PROP_FIELDS.field}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.field)}`}>Field node name: </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_PROP_FIELDS.field)}`} type="text" placeholder="e.g. textfieldTest" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${COMP_PROP_FIELDS.field}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_PROP_FIELDS.label}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.label)}`}>Field Label (for Dialog): </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_PROP_FIELDS.label)}`} type="text" placeholder="e.g. Textfield Test" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${COMP_PROP_FIELDS.label}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_PROP_FIELDS.description}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.description)}`}>Field Description (for Dialog): </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_PROP_FIELDS.description)}`} type="text" placeholder="e.g. Adds a fieldDescription tooltip" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${COMP_PROP_FIELDS.description}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_PROP_FIELDS.javadoc}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.javadoc)}`}>Java Doc Comment: </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_PROP_FIELDS.javadoc)}`} type="text" placeholder="e.g. returns a text value" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${seed(COMP_PROP_FIELDS.javadoc)}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_PROP_FIELDS.type}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.type)}`}>Form type: </label>
                                                <div className="col-sm-9">
                                                    <Select {...input} options={FORM_TYPES} searchable />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${seed(COMP_PROP_FIELDS.type)}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <div className="form-group row">
                                        <div className="form-check offset-26">
                                            <label className="form-check-label">
                                                <Field
                                                    name={`${COMP_PROP_FIELDS.jsonExpose}`}
                                                    component="input"
                                                    type="checkbox"
                                                />
                                                Expose JSON?
                                                <i className="input-helper" />
                                            </label>
                                        </div>
                                        <Condition when={`${COMP_PROP_FIELDS.jsonExpose}`} is={true}>
                                            <div className="form-check ml-1">
                                                <Field
                                                    name={`${COMP_PROP_FIELDS.jsonProperty}`}
                                                    className="form-control form-control-sm"
                                                    component="input"
                                                    placeholder="JSON prop name"
                                                    type="text"
                                                />
                                            </div>
                                        </Condition>
                                        <div className="form-check offset-1">
                                            <label className="form-check-label">
                                                <Field
                                                    name={`${COMP_PROP_FIELDS.useExistingModel}`}
                                                    component="input"
                                                    type="checkbox"
                                                />
                                                Use existing model?
                                                <i className="input-helper" />
                                            </label>
                                        </div>
                                        <div className="form-check ml-1">
                                            <Field
                                                name={`${COMP_PROP_FIELDS.modelName}`}
                                                className="form-control form-control-sm"
                                                component="input"
                                                placeholder="Model name"
                                                type="text"
                                            />
                                        </div>
                                    </div>
                                    <div className="row offset-4 mb-4 mt-4">
                                        <Field
                                            name={`${COMP_PROP_FIELDS.id}`}
                                            className="form-control form-control-sm d-none"
                                            component="input"
                                            type="text"
                                        />
                                        <button type="submit" className="btn btn-primary" disabled={submitting || pristine}>
                                            <i className="mdi mdi-floppy menu-icon" />
                                            <span className="pl-1">Save changes</span>
                                        </button>
                                    </div>
                                    <ul className="nav nav-pills nav-fill">
                                        <li className="nav-item">
                                            <button title="Remove this property" type="button" onClick={removePropAction} className="nav-link active bg-danger">
                                                <i className="mdi mdi-delete pr-1" />
                                                Remove
                                            </button>
                                        </li>
                                        <li className="nav-item">
                                            <button type="button" onClick={handleAttrModalShow} className="nav-link btn btn-light" href="#">
                                                Add/Remove Attributes
                                                <div className="badge badge-pill badge-primary ml-2">{Object.keys(propValues.attributes).length}</div>
                                            </button>
                                        </li>
                                        <li className="nav-item">
                                            <button type="button" className="nav-link btn btn-light">
                                                Add/Remove Child Properties
                                                <div className="badge badge-pill badge-primary ml-2">{propValues.items.length}</div>
                                            </button>
                                        </li>
                                    </ul>
                                </form>
                            )}
                        />
                    </div>
                </div>
            </div>
        </div>
    ));

    return (
        <SortableItem index={index} propValues={propValues} />
    );
}

SortableProperty.propTypes = {
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    propValues: PropTypes.object.isRequired,
};

export default SortableProperty;
