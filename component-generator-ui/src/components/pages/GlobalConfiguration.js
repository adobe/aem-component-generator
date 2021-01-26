import React from 'react';
import { FORM_ERROR } from 'final-form';
import { Form, Field } from 'react-final-form';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import createDecorator from 'final-form-focus';
import { FETCH_CONFIGS, API_ROOT } from '../../actions';
import wretch from '../../utils/wretch';

function GlobalConfiguration() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);

    const GLOBAL_CONF_FIELDS = {
        codeOwner: 'codeOwner',
        bundlePath: 'bundlePath',
        testPath: 'testPath',
        appsPath: 'appsPath',
        componentPath: 'componentPath',
        modelInterfacePackage: 'modelInterfacePackage',
        modelImplPackage: 'modelImplPackage',
        copyrightYear: 'copyrightYear',
    };

    const onValidate = (values) => {
        const errors = {};
        if (!values.codeOwner) {
            errors.codeOwner = 'Code owner is required';
        }
        if (!values.bundlePath) {
            errors.bundlePath = 'Bundle path is required';
        }
        if (!values.testPath) {
            errors.testPath = 'Test path is required';
        }
        if (!values.appsPath) {
            errors.appsPath = 'Apps path is required';
        }
        if (!values.componentPath) {
            errors.componentPath = 'Component path is required';
        }
        if (!values.modelInterfacePackage) {
            errors.modelInterfacePackage = 'Model interface package is required';
        }
        if (!values.modelImplPackage) {
            errors.modelImplPackage = 'Model impl package is required';
        }
        if (!values.copyrightYear) {
            errors.copyrightYear = 'Copyright year is required';
        }
        if (values.copyrightYear) {
            const regex = RegExp('^current|[0-9]{4}$');
            if (!regex.test(values.copyrightYear)) {
                errors.copyrightYear = 'Copyright year must be a 4 digit year or "current" for current year';
            }
        }
        return errors;
    };

    const onSubmit = async (values) => {
        console.info('Submitting', values);
        const toastId = 'globalConfigSubmit';
        try {
            const response = await wretch.url(`${API_ROOT}/global`).post({ ...values }).json();
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

    const focusOnError = createDecorator();

    return (
        <div className="col-12 grid-margin stretch-card">
            <div className="card">
                <div className="card-body">
                    <h4 className="card-title">
                        Global Project Configurations
                    </h4>
                    <Form
                        initialValues={{ ...global }} // init form with values from server
                        onSubmit={onSubmit}
                        validate={onValidate}
                        decorators={[focusOnError]}
                        render={({
                            submitError, handleSubmit, reset, submitting, pristine,
                        }) => (
                            <form className="form-globalconfig" onSubmit={handleSubmit}>
                                {submitError && (
                                    <div className="alert alert-danger">
                                        <strong>Error!</strong>
                                        {' '}
                                        {submitError}
                                    </div>
                                )}
                                <Field name={`${GLOBAL_CONF_FIELDS.codeOwner}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <div className="col-sm-3">
                                                <label htmlFor={`${GLOBAL_CONF_FIELDS.codeOwner}`}>Code Owner: </label>
                                                <p className="text-muted">client / project name - e.g. NewCo Inc</p>
                                            </div>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.codeOwner}`} type="text" placeholder="Code owner" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.codeOwner}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.bundlePath}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.bundlePath}`}>Bundle Path: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.bundlePath}`} type="text" placeholder="ex: core/src/main/java" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.bundlePath}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.testPath}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.testPath}`}>Test Path: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.testPath}`} type="text" placeholder="ex: core/src/test/java" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.testPath}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.appsPath}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.appsPath}`}>Apps Path: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.appsPath}`} type="text" placeholder="ex: ui.apps/src/main/content/jcr_root/apps" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.appsPath}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.componentPath}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.componentPath}`}>Component Path: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.componentPath}`} type="text" placeholder="ex: newco/components" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.componentPath}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.modelInterfacePackage}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.modelInterfacePackage}`}>Model interface package: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.modelInterfacePackage}`} type="text" placeholder="ex: com.newco.aem.base.models" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.modelInterfacePackage}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.modelImplPackage}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <label className="col-sm-3" htmlFor={`${GLOBAL_CONF_FIELDS.modelImplPackage}`}>Model implementation package: </label>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.modelImplPackage}`} type="text" placeholder="ex: com.newco.aem.base.models.impl" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.modelImplPackage}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <Field name={`${GLOBAL_CONF_FIELDS.copyrightYear}`}>
                                    {({ input, meta }) => (
                                        <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                            <div className="col-sm-3">
                                                <label htmlFor={`${GLOBAL_CONF_FIELDS.copyrightYear}`}>Copyright year: </label>
                                                <p className="text-muted">(enter &quot;current&quot; to default to current year)</p>
                                            </div>
                                            <div className="col-sm-9">
                                                <input {...input} id={`${GLOBAL_CONF_FIELDS.copyrightYear}`} type="text" placeholder="current" className={`form-control ${(meta.error) && meta.touched ? 'form-control-danger' : ''}`} />
                                                {(meta.error)
                                                && meta.touched && (
                                                    <label htmlFor={`${GLOBAL_CONF_FIELDS.copyrightYear}`} className="error mt-2 text-danger">{meta.error}</label>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Field>
                                <div className="row offset-4">
                                    <button type="submit" className="btn btn-primary btn-md" disabled={submitting || pristine}>
                                        <i className="mdi mdi-floppy menu-icon" />
                                        <span className="pl-1">Save changes</span>
                                    </button>
                                    <div className={` ${submitting ? '' : 'd-none'}`}>
                                        <div className="dot-opacity-loader">
                                            <span />
                                            <span />
                                            <span />
                                        </div>
                                    </div>
                                </div>
                            </form>
                        )}
                    />
                </div>
            </div>
        </div>
    );
}

export default GlobalConfiguration;
