/* eslint max-len: 0 */
import React from 'react';
import PropTypes from 'prop-types';
import { SortableElement, SortableHandle } from 'react-sortable-hoc';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGripLines } from '@fortawesome/free-solid-svg-icons';
import { useUIDSeed } from 'react-uid';
import { Field, Form } from 'react-final-form';
import Select from 'react-select';
import arrayMutators from 'final-form-arrays';
import { FieldArray } from 'react-final-form-arrays';
import { FORM_TYPES } from '../../../utils/Constants';

function SortableChildProperty({
    index, propValues, onSave, onRemove,
}) {
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

    const DragHandle = SortableHandle(() => (
        <i aria-label="Click and drag icon" title="Click/hold to drag and re-order" className="dragIcon">
            <FontAwesomeIcon icon={faGripLines} />
        </i>
    ));

    const onValidate = (values) => {
        const errors = {};
        if (!values.field) {
            errors.field = 'Required';
        }
        return errors;
    };

    const saveEdit = (values) => {
        onSave(values);
    };

    const onRemoveItem = () => {
        onRemove(index);
    };

    const SortableItem = SortableElement(() => (
        <div className="is-sortable-modal card rounded mb-2">
            <div className="card-body p-3">
                <div className="media align-items-center">
                    <DragHandle />
                    <div className="media-body">
                        <Form
                            initialValues={{ ...propValues }}
                            mutators={{
                                ...arrayMutators,
                            }}
                            onSubmit={saveEdit}
                            validate={onValidate}
                            render={({
                                submitError,
                                handleSubmit,
                                form: {
                                    mutators: { push, pop },
                                },
                                reset,
                                submitting,
                                pristine,
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
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.field)}`}>Field name: </label>
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
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.label)}`}>Field Label: </label>
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
                                    <Field name={`${COMP_PROP_FIELDS.type}`}>
                                        {({ input, meta }) => (
                                            <div className={`form-group row ${(meta.error) && meta.touched ? 'has-danger' : ''}`}>
                                                <label className="col-sm-3" htmlFor={`${seed(COMP_PROP_FIELDS.type)}`}>Field type: </label>
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
                                            <label htmlFor={`${seed(COMP_PROP_FIELDS.jsonExpose)}`} className="form-check-label">
                                                <Field
                                                    id={`${seed(COMP_PROP_FIELDS.jsonExpose)}`}
                                                    name={`${COMP_PROP_FIELDS.jsonExpose}`}
                                                    component="input"
                                                    type="checkbox"
                                                />
                                                Expose JSON?
                                                <i className="input-helper" />
                                            </label>
                                        </div>
                                    </div>
                                    <div className="row offset-3 mb-4 mt-4">
                                        <div className="buttons">
                                            <button
                                                className="mr-3"
                                                type="button"
                                                onClick={() => { push('attributes', { key: '', value: '' }); }}
                                            >
                                                <i className="mdi mdi-plus pr-1" />
                                                Add Attribute
                                            </button>
                                            <button type="button" onClick={() => pop('attributes')} disabled={pristine}>
                                                <i className="mdi mdi-minus pr-1" />
                                                Remove Attribute
                                            </button>
                                        </div>
                                    </div>
                                    <FieldArray name="attributes">
                                        {({ fields }) => fields.map((name, index) => (
                                            <div className="form-group row offset-3" key={name}>
                                                <label htmlFor={seed(`child-attribute${index}-key`)} className="col-sm-2 pt-2">
                                                    Attribute #
                                                    {index + 1}
                                                    :
                                                </label>
                                                <div className="col-sm-10 form-inline">
                                                    <Field
                                                        id={seed(`child-attribute${index}-key`)}
                                                        className="mr-3 form-control form-control-sm "
                                                        name={`${name}.key`}
                                                        component="input"
                                                        placeholder="Attribute key"
                                                    />
                                                    <Field
                                                        className="form-control form-control-sm mr-3"
                                                        name={`${name}.value`}
                                                        component="input"
                                                        placeholder="Attribute value"
                                                    />
                                                    <span title="remove attribute" tabIndex="0" role="button" className="attr-delete-btn" onKeyPress={(event) => event.key === 'Enter' && fields.remove(index)} onClick={() => fields.remove(index)}>
                                                        <i className="mdi mdi-delete-circle" />
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                    </FieldArray>
                                    <div className="row offset-3 mb-4 mt-4">
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
                                            <button title="Remove this property" type="button" onClick={onRemoveItem} className="nav-link active bg-danger">
                                                <i className="mdi mdi-delete pr-1" />
                                                Remove
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

SortableChildProperty.propTypes = {
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    propValues: PropTypes.object.isRequired,
    onSave: PropTypes.func.isRequired,
    onRemove: PropTypes.func.isRequired,
};

export default SortableChildProperty;
