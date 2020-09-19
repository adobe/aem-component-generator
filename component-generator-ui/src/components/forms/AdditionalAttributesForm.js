/* eslint jsx-a11y/label-has-associated-control: 0 */
/* eslint jsx-a11y/label-has-for: 0 */
import { FORM_ERROR } from 'final-form';
import React from 'react';
import { Field, Form } from 'react-final-form';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { FETCH_CONFIGS, ROOT_URL } from '../../actions';
import wretch from '../../utils/wretch';

function AdditionalAttributesForm() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);
    const ADD_ATTR_FIELDS = {
        js: 'js',
        jsTxt: 'jsTxt',
        css: 'css',
        cssTxt: 'cssTxt',
        html: 'html',
        htmlContent: 'htmlContent',
        slingModel: 'slingModel',
        testClass: 'testClass',
        junitMajorVersion: 'junitMajorVersion',
        contentExporter: 'contentExporter',
        genericJavadoc: 'genericJavadoc',
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.junitMajorVersion) {
            errors.junitMajorVersion = 'Required';
        }
        return errors;
    };

    const onSubmit = async (values) => {
        console.info('Submitting', values);
        const toastId = 'addAttrSubmit';
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
                    <h4 className="card-title">Additional Options</h4>
                    <Form
                        onSubmit={onSubmit}
                        initialValues={{ ...global.options }}
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
                                                        name={`${ADD_ATTR_FIELDS.jsTxt}`}
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
                                                        name={`${ADD_ATTR_FIELDS.cssTxt}`}
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
                                                        name={`${ADD_ATTR_FIELDS.slingModel}`}
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
                                                        name={`${ADD_ATTR_FIELDS.testClass}`}
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
                                                        name={`${ADD_ATTR_FIELDS.genericJavadoc}`}
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
                                                    add content exporter annotation
                                                    <i className="input-helper" />
                                                    <a className="whatThisLink" title="What is this?" target="_blank" rel="noreferrer noopener" href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/json-exporter.html">
                                                        <i className="mdi mdi-comment-question" />
                                                    </a>
                                                </label>
                                            </div>
                                            <Field name={`${ADD_ATTR_FIELDS.junitMajorVersion}`}>
                                                {({ input, meta }) => (
                                                    <div className={`form-group ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                        <label className="d-inline pr-2" htmlFor={`${ADD_ATTR_FIELDS.junitMajorVersion}`}>junit version: </label>
                                                        <div className="d-inline">
                                                            <input {...input} id={`${ADD_ATTR_FIELDS.junitMajorVersion}`} style={{ maxWidth: '70px' }} type="number" placeholder="5" className={`form-control form-control-sm d-inline pr-2 ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                            {(meta.error)
                                                            && meta.touched && (
                                                                <label htmlFor={`${ADD_ATTR_FIELDS.junitMajorVersion}`} className="error mt-2 text-danger">{meta.error}</label>
                                                            )}
                                                        </div>
                                                    </div>
                                                )}
                                            </Field>
                                        </div>
                                    </div>
                                </div>
                                <div className="row offset-30 mt-2">
                                    <button type="submit" className="btn btn-primary btn-md" disabled={submitting || pristine}>
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

export default AdditionalAttributesForm;
