import React, { useState } from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import {
    func, bool, array, string,
} from 'prop-types';
import { useUIDSeed } from 'react-uid';
import { SortableContainer } from 'react-sortable-hoc';
import { v4 as uuidv4 } from 'uuid';
import arrayMove from 'array-move';
import { toast } from 'react-toastify';
import { FORM_ERROR } from 'final-form';
import SortableChildProperty from '../forms/helper/SortableChildProperty';
import wretch from '../../utils/wretch';
import { API_ROOT } from '../../actions';

function ItemsModal(props) {
    const seed = useUIDSeed();
    const {
        onHide, onConfirm, show, items, propertyId, type,
    } = props;
    const [inputFields, setInputFields] = useState(items);

    const onSaveEditItems = () => {
        onConfirm(inputFields, propertyId);
    };

    const onSaveChild = async (values) => {
        const toastId = 'childPropSave';
        try {
            const response = await wretch.url(`${API_ROOT}/child-properties`).post({ ...values, propertyId, propType: type }).json();
            return toast(`Success!: ${response.message}`, {
                toastId,
                type: toast.TYPE.SUCCESS,
            });
        } catch (err) {
            return { [FORM_ERROR]: err.message };
        }
    };

    const onSortEnd = async ({ oldIndex, newIndex }) => {
        await wretch
            .url(`${API_ROOT}/child-properties`)
            .post({
                oldIndex, newIndex, propertyId, moveProp: true, propType: type,
            })
            .json();
        setInputFields(arrayMove(inputFields, oldIndex, newIndex));
    };

    function addChildPropAction() {
        const values = {
            field: '',
            type: '',
            label: '',
            jsonExpose: false,
            id: uuidv4(),
            attributes: [],
        };
        setInputFields(inputFields.concat(values));
    }

    const onRemoveChild = async (index) => {
        await wretch
            .url(`${API_ROOT}/child-properties`)
            .post({
                id: inputFields[index].id, propertyId, removeProp: true, propType: type,
            })
            .json();
        if (inputFields.length > 1) {
            setInputFields(inputFields.splice(index, 1));
        } else {
            setInputFields([]);
        }

    };

    const SortableTabsContainer = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    return (
        <Modal
            show={show}
            onHide={onHide}
            className="scrollable-modal"
            size="lg"
            aria-labelledby={seed('itemsModal')}
        >
            <Modal.Header closeButton>
                <Modal.Title id={seed('itemsModal')}>
                    Add or Remove Child Properties
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <SortableTabsContainer onSortEnd={onSortEnd} useDragHandle>
                    {inputFields.map((value, index) => (
                        <SortableChildProperty key={`item-${value.id}`} propValues={value} index={index} onSave={onSaveChild} onRemove={onRemoveChild} />
                    ))}
                </SortableTabsContainer>
                <div className="row">
                    <div className="col">
                        <button onClick={addChildPropAction} type="button" className="btn btn-primary">
                            <i className="mdi mdi-plus pr-1" />
                            Add Child Property
                        </button>
                    </div>
                </div>
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={onHide} variant="secondary">Cancel</Button>
                <Button onClick={onSaveEditItems} variant="primary">
                    <i className="mdi mdi-floppy menu-icon pr-2" />
                    Save child props
                </Button>
            </Modal.Footer>
        </Modal>
    );
}

ItemsModal.propTypes = {
    onHide: func.isRequired,
    onConfirm: func.isRequired,
    show: bool.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    items: array.isRequired,
    propertyId: string.isRequired,
    type: string.isRequired,
};

export default ItemsModal;
