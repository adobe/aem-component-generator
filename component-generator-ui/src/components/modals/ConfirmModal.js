import React from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import { string, func, bool } from 'prop-types';

function ConfirmModal(props) {
    const {
        title, description, onHide, onConfirm, show,
    } = props;

    return (
        <Modal
            show={show}
            onHide={onHide}
            size="lg"
            aria-labelledby="confirm-modal-title-vcenter"
        >
            <Modal.Header closeButton>
                <Modal.Title id="confirm-modal-title-vcenter">
                    {title}
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <p>{description}</p>
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={onHide}>Cancel</Button>
                <Button onClick={onConfirm} variant="danger">Reset it!</Button>
            </Modal.Footer>
        </Modal>
    );
}

ConfirmModal.propTypes = {
    title: string.isRequired,
    description: string.isRequired,
    onHide: func.isRequired,
    onConfirm: func.isRequired,
    show: bool.isRequired,
};

export default ConfirmModal;
