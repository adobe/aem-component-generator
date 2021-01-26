/* eslint max-len: 0 */
import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { SortableElement, SortableHandle } from 'react-sortable-hoc';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGripLines } from '@fortawesome/free-solid-svg-icons';
import { Field, Form } from 'react-final-form';
import { toast } from 'react-toastify';
import { FORM_ERROR } from 'final-form';
import { useDispatch, useSelector } from 'react-redux';
import { useUIDSeed } from 'react-uid';
import AsyncSelect from 'react-select/async';
import wretch from '../../../utils/wretch';
import {
    API_ROOT, FETCH_CONFIGS, REMOVE_TAB, UPDATE_TAB,
} from '../../../actions';
import { GLOBAL, MAIN, SHARED } from '../../../utils/Constants';

function SortableTab({ index, propValues, type }) {
    const [removedTab, setRemovedTab] = useState(false);
    const global = useSelector((state) => state.compData);
    const seed = useUIDSeed();
    const dispatch = useDispatch();
    const COMP_TAB_FIELDS = {
        label: 'label',
        fields: 'fields',
        id: 'id',
    };

    const onPropSelectChange = async (options) => {
        const updatedTab = { ...propValues, fields: options };
        dispatch({ type: UPDATE_TAB, payload: { ...updatedTab, type } });
        await wretch.url(`${API_ROOT}/tabs`).post({ ...updatedTab, tabType: type }).json();
        return options;
    };

    const onBlurUpdate = async (event) => {
        const updatedTab = { ...propValues, label: event.target.value };
        dispatch({ type: UPDATE_TAB, payload: { ...updatedTab, type } });
        await wretch.url(`${API_ROOT}/tabs`).post({ ...updatedTab, tabType: type }).json();
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.label) {
            errors.label = 'Required';
        }
        return errors;
    };

    const onSubmit = async (values) => {
        const toastId = 'tabSubmit';
        try {
            const response = await wretch.url(`${API_ROOT}/tabs`).post({ ...values, tabType: type }).json();
            const result = await wretch.url(`${API_ROOT}/global`).get().json();
            dispatch({ type: FETCH_CONFIGS, payload: result });
            return toast(`Success!: ${response.message}`, {
                toastId,
                type: toast.TYPE.SUCCESS,
            });
        } catch (err) {
            return { [FORM_ERROR]: err.message };
        }
    };

    const fetchFieldOptions = async (inputValue) => {
        const result = await wretch.url(`${API_ROOT}/global`).get().json();
        const fieldOps = [];
        const assignedProps = new Set();
        switch (type) {
        case MAIN: {
            if (result && result.options.properties) {
                if (global.options.propertiesTabs) {
                    global.options.propertiesTabs.forEach((tab) => {
                        if (tab.fields) {
                            tab.fields.forEach((p) => {
                                assignedProps.add(p.value);
                            });
                        }
                    });
                }
                result.options.properties.forEach((prop) => {
                    if (!assignedProps.has(prop.field)) {
                        fieldOps.push({ value: prop.field, label: prop.field });
                    }
                });
            }
            return fieldOps.filter((i) => i.label.toLowerCase().includes(inputValue.toLowerCase()));
        }
        case SHARED: {
            if (result && result.options['properties-shared']) {
                if (global.options.propertiesSharedTabs) {
                    global.options.propertiesSharedTabs.forEach((tab) => {
                        if (tab.fields) {
                            tab.fields.forEach((p) => {
                                assignedProps.add(p.value);
                            });
                        }
                    });
                }
                result.options['properties-shared'].forEach((prop) => {
                    if (!assignedProps.has(prop.field)) {
                        fieldOps.push({ value: prop.field, label: prop.field });
                    }
                });
            }
            return fieldOps.filter((i) => i.label.toLowerCase().includes(inputValue.toLowerCase()));
        }
        case GLOBAL: {
            if (result && result.options['properties-global']) {
                if (global.options.propertiesGlobalTabs) {
                    global.options.propertiesGlobalTabs.forEach((tab) => {
                        if (tab.fields) {
                            tab.fields.forEach((p) => {
                                assignedProps.add(p.value);
                            });
                        }
                    });
                }
                result.options['properties-global'].forEach((prop) => {
                    if (!assignedProps.has(prop.field)) {
                        fieldOps.push({ value: prop.field, label: prop.field });
                    }
                });
            }
            return fieldOps.filter((i) => i.label.toLowerCase().includes(inputValue.toLowerCase()));
        }
        default:
            return {
                ...fieldOps,
            };
        }
    };

    const promiseOptions = (inputValue) => {
        return new Promise((resolve) => {
            resolve(fetchFieldOptions(inputValue));
        });
    };

    const DragHandle = SortableHandle(() => (
        <i title="Click/hold to drag and re-order" className="dragIcon">
            <FontAwesomeIcon icon={faGripLines} />
        </i>
    ));

    const removeTabAction = async () => {
        // animate a fade out on deletion of property
        setRemovedTab(true);
        try {
            await wretch.url(`${API_ROOT}/tabs`).post({ ...propValues, removeTab: true, tabType: type }).json();
        } catch (err) {
            let msg = '';
            if (err && err.message) {
                msg = err.message;
            }
            toast(`Uh oh! The backend is having problems. ${msg}`, { type: toast.TYPE.ERROR });
        }
        // update global state to update properties
        setTimeout(() => {
            dispatch({ type: REMOVE_TAB, payload: { ...propValues, type } });
        }, 550);
    };

    const SortableItem = SortableElement(() => (
        <div className={`card rounded mb-2 ${!removedTab ? '' : 'animated fadeOut'}`}>
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
                                    <Field name={`${COMP_TAB_FIELDS.label}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_TAB_FIELDS.label)}`}>Tab label: </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_TAB_FIELDS.label)}`} onBlur={onBlurUpdate} type="text" placeholder="" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${COMP_TAB_FIELDS.label}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_TAB_FIELDS.id}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_TAB_FIELDS.id)}`}>Tab ID: </label>
                                                <div className="col-sm-9">
                                                    <input {...input} id={`${seed(COMP_TAB_FIELDS.id)}`} type="text" placeholder="" disabled className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${COMP_TAB_FIELDS.id}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <Field name={`${COMP_TAB_FIELDS.fields}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_TAB_FIELDS.fields)}`}>Included properties: </label>
                                                <div className="col-sm-9">
                                                    <AsyncSelect {...input} onChange={(options) => { onPropSelectChange(options); input.onChange(options); }} loadOptions={promiseOptions} defaultOptions isMulti searchable />
                                                    {(meta.error)
                                                    && meta.touched && (
                                                        <label htmlFor={`${seed(COMP_TAB_FIELDS.fields)}`} className="error mt-2 text-danger">{meta.error}</label>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </Field>
                                    <ul className="nav nav-pills nav-fill mt-5">
                                        <li className="nav-item d-none">
                                            <button type="submit" className="btn btn-primary" disabled={submitting || pristine}>
                                                <i className="mdi mdi-floppy menu-icon" />
                                                <span className="pl-1">Save changes</span>
                                            </button>
                                        </li>
                                        <li className="nav-item">
                                            <button title="Remove this tab" type="button" onClick={removeTabAction} className="nav-link active bg-danger">
                                                <i className="mdi mdi-delete pr-1" />
                                                Remove tab
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

SortableTab.propTypes = {
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    propValues: PropTypes.object.isRequired,
    type: PropTypes.string.isRequired,
};

export default SortableTab;
