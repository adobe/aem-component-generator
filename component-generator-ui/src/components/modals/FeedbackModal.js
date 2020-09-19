import React from 'react';
import { Button, Modal } from 'react-bootstrap';
import { string, func, bool } from 'prop-types';

function FeedbackModal(props) {
    const {
        title, description, onHide, show,
    } = props;

    return (
        <Modal
            show={show}
            onHide={onHide}
            size="lg"
            aria-labelledby="contained-modal-title-vcenter"
            centered
        >
            <Modal.Header closeButton>
                <Modal.Title id="contained-modal-title-vcenter">
                    {title}
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <p>{description}</p>
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={onHide}>Ok</Button>
            </Modal.Footer>
        </Modal>
    );
}

FeedbackModal.propTypes = {
    title: string.isRequired,
    description: string.isRequired,
    onHide: func.isRequired,
    show: bool.isRequired,
};

export default FeedbackModal;
