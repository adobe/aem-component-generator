/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint jsx-a11y/label-has-for: 0 */
/* eslint react/jsx-props-no-spreading: 0 */
/* eslint max-len: 0 */
import React from 'react';
import { FORM_ERROR } from 'final-form';
import { Field, Form } from 'react-final-form';
import { useDispatch, useSelector } from 'react-redux';
import Select from 'react-select';
import { toast } from 'react-toastify';
import { FETCH_CONFIGS, ROOT_URL } from '../../actions';
import { SLING_ADAPTABLES } from '../../utils/Constants';
import wretch from '../../utils/wretch';

function ComponentAttributesForm() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);
    const COMP_ATTR_FIELDS = {
        componentTitle: 'componentTitle',
        componentNodeName: 'componentNodeName',
        componentGroup: 'componentGroup',
        componentType: 'componentType',
        modelAdapters: 'modelAdapters',
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.componentTitle) {
            errors.componentTitle = 'Component title is required';
        }
        if (!values.componentNodeName) {
            errors.componentNodeName = 'Component node name is required';
        }
        if (!values.componentGroup) {
            errors.componentGroup = 'Component group is required';
        }
        if (!values.componentType) {
            errors.componentType = 'Component type is required';
        }
        if (!values.modelAdapters) {
            errors.modelAdapters = 'Model adapter is required';
        } else if (values.modelAdapters && values.modelAdapters.length < 1) {
            errors.modelAdapters = 'Model adapter is required';
        }
        return errors;
    };

    const onSubmit = async (values) => {
        const toastId = 'componentAttrSubmit';
        try {
            const response = await wretch.url(`${ROOT_URL}`).post({ ...values }).json();
            const result = await wretch.url(`${ROOT_URL}`).get().json();
            dispatch({ type: FETCH_CONFIGS, payload: result });
            return toast(`Success!: ${response.message}`, {
                toastId,
                type: toast.TYPE.SUCCESS,
            });
        } catch (err) {
            return { [FORM_ERROR]: err.message };
        }
    };

    return (
        <div className="col-md-6 grid-margin stretch-card">
            <div className="card">
                <div className="card-body">
                    <h4 className="card-title">Component attributes</h4>
                    <Form
                        initialValues={{ ...global, modelAdapters: global.options.modelAdaptables }}
                        onSubmit={onSubmit}
                        validate={onValidate}
                        render={({
                            submitError, handleSubmit, reset, submitting, pristine,
                        }) => (
                            <form className="form-comp-attributes" onSubmit={handleSubmit}>
                                {submitError && (
                                    <div className="alert alert-danger">
                                        <strong>Error!</strong>
                                        {' '}
                                        {submitError}
                                    </div>
                                )}
                                <Field name={`${COMP_ATTR_FIELDS.componentTitle}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.componentTitle}`}>Component Title: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.componentTitle}`} type="text" placeholder="e.g. Banner ad" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.componentTitle}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${COMP_ATTR_FIELDS.componentNodeName}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.componentNodeName}`}>Component Node name: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.componentNodeName}`} type="text" placeholder="e.g. banner" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.componentNodeName}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${COMP_ATTR_FIELDS.componentGroup}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.componentGroup}`}>Component Group: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.componentGroup}`} type="text" placeholder="e.g. NewCo.Content" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.componentGroup}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${COMP_ATTR_FIELDS.componentType}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.type}`}>Component Type: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.componentType}`} type="text" placeholder="e.g. content" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.componentType}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${COMP_ATTR_FIELDS.modelAdapters}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.modelAdapters}`}>Model Adapters: </label>
                                            <div className="col-sm-9">
                                                {/* pass in default value={{ value: 'resource', label: 'Resource' }} */}
                                                <Select {...input} options={SLING_ADAPTABLES} searchable isMulti />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.modelAdapters}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <div className="row offset-4">
                                    <button type="submit" className="btn btn-primary btn-sm" disabled={submitting || pristine}>
                                        <i className="mdi mdi-floppy menu-icon" />
                                        <span className="pl-1">Save changes</span>
                                    </button>
                                </div>
                            </form>
                        )}
                    />
                </div>
            </div>
        </div>
    );
}

export default ComponentAttributesForm;
