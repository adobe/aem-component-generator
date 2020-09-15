/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint jsx-a11y/label-has-for: 0 */
import React from 'react';
import { Field, Form } from 'react-final-form';

function AdditionalAttributesForm() {
    const ADD_ATTR_FIELDS = {
        js: 'js',
        jstxt: 'jstxt',
        css: 'css',
        csstxt: 'csstxt',
        html: 'html',
        htmlContent: 'htmlContent',
        slingmodel: 'slingmodel',
        testclass: 'testclass',
        jUnitVersion: 'jUnitVersion',
        contentExporter: 'contentExporter',
        javaDoc: 'javaDoc',
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.jUnitVersion) {
            errors.jUnitVersion = 'Required';
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
                    <h4 className="card-title">Additional Options</h4>
                    <Form
                        onSubmit={onSubmit}
                        initialValues={{ js: true }}
                        validate={onValidate}
                        render={({
                            submitError, handleSubmit, reset, submitting, pristine,
                        }) => (
                            <form className="form-add-attributes" onSubmit={handleSubmit}>
                                <div className="row">
                                    <div className="col-md-6">
                                        <div className="form-group">
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.js}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    Add JS?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.jstxt}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    Add JS text file?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.css}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    Add CSS?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.csstxt}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    Add CSS text file?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.html}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    Add HTML file?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.htmlContent}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    include stubbed HTML content?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-6">
                                        <div className="form-group">
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.slingmodel}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    create Sling model?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.testclass}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    create test class?
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.javaDoc}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    add generic java docs
                                                    <i className="input-helper" />
                                                </label>
                                            </div>
                                            <div className="form-check">
                                                <label className="form-check-label">
                                                    <Field
                                                        name={`${ADD_ATTR_FIELDS.contentExporter}`}
                                                        component="input"
                                                        type="checkbox"
                                                    />
                                                    content exporter annotation
                                                    <i className="input-helper" />
                                                    <a title="What is this?" target="_blank" rel="noreferrer noopener" href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/json-exporter.html">
                                                        <i className="mdi mdi-comment-question" />
                                                    </a>
                                                </label>
                                            </div>
                                            <Field name={`${ADD_ATTR_FIELDS.jUnitVersion}`}>
                                                {({ input, meta }) => (
                                                    <div className={`form-group ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                        <label className="d-inline pr-2" htmlFor={`${ADD_ATTR_FIELDS.jUnitVersion}`}>junit version: </label>
                                                        <div className="d-inline">
                                                            <input {...input} id={`${ADD_ATTR_FIELDS.jUnitVersion}`} style={{ maxWidth: '70px' }} type="number" placeholder="5" className={`form-control form-control-sm d-inline pr-2 ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                            {(meta.error)
                                                            && meta.touched && (
                                                                <label htmlFor={`${ADD_ATTR_FIELDS.jUnitVersion}`} className="error mt-2 text-danger">{meta.error}</label>
                                                            )}
                                                        </div>
                                                    </div>
                                                )}
                                            </Field>
                                        </div>
                                    </div>
                                </div>
                                <button type="submit" className="btn btn-primary btn-md" disabled={submitting || pristine}>
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

export default AdditionalAttributesForm;
