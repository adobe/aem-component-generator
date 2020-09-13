/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint jsx-a11y/label-has-for: 0 */
/* eslint react/jsx-props-no-spreading: 0 */
/* eslint max-len: 0 */
import React from 'react';
import { Field, Form } from 'react-final-form';
import Select from 'react-select';

function ComponentAttributesForm() {
    const COMP_ATTR_FIELDS = {
        componentTitle: 'componentTitle',
        componentNodeName: 'componentNodeName',
        group: 'group',
        type: 'type',
        modelAdapters: 'modelAdapters',
    };

    const slingAdaptables = [
        { value: 'resource', label: 'Resource' },
        { value: 'request', label: 'SlingHttpServletRequest' },
    ];

    const onValidate = (values) => {
        const errors = {};
        if (!values.componentTitle) {
            errors.componentTitle = 'Component title is required';
        }
        if (!values.componentNodeName) {
            errors.componentNodeName = 'Component node name is required';
        }
        if (!values.group) {
            errors.group = 'Component group is required';
        }
        if (!values.type) {
            errors.type = 'Component type is required';
        }
        if (!values.modelAdapters) {
            errors.modelAdapters = 'Model adapter is required';
        } else if (values.modelAdapters && values.modelAdapters.length < 1) {
            errors.modelAdapters = 'Model adapter is required';
        }
        return errors;
    };

    const onSubmit = async (values) => {
        console.info('Submitting', values);
    };

    return (
        <div className="col-md-6 grid-margin stretch-card">
            <div className="card">
                <div className="card-body">
                    <h4 className="card-title">Component attributes</h4>
                    <Form
                        onSubmit={onSubmit}
                        validate={onValidate}
                        render={({
                            submitError, handleSubmit, reset, submitting, pristine,
                        }) => (
                            <form className="form-comp-attributes" onSubmit={handleSubmit}>
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
                                <Field name={`${COMP_ATTR_FIELDS.group}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.group}`}>Component Group: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.group}`} type="text" placeholder="e.g. NewCo.Content" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.group}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${COMP_ATTR_FIELDS.type}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${COMP_ATTR_FIELDS.type}`}>Component Type: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${COMP_ATTR_FIELDS.type}`} type="text" placeholder="e.g. content" className={`form-control form-control-sm ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.type}`} className="error mt-2 text-danger">{meta.error}</label>
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
                                                <Select {...input} options={slingAdaptables} searchable isMulti />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${COMP_ATTR_FIELDS.modelAdapters}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <button type="submit" className="btn btn-primary btn-md" disabled={submitting}>
                                    <i className="mdi mdi-floppy menu-icon" />
                                    <span className="pl-1">Save changes</span>
                                </button>
                            </form>
                        )}
                    />
                </div>
            </div>
        </div>
    );
}

export default ComponentAttributesForm;
