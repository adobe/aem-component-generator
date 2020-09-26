import React from 'react';
import { Nav, Tab } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { v4 as uuidv4 } from 'uuid';
import { ADD_PROPERTY, API_ROOT, REORDER_PROPERTY } from '../../actions';
import SortableProperty from './helper/SortableProperty';
import wretch from '../../utils/wretch';

function PropertyBuilderForm() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);

    const SortableProperties = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const onSortEnd = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true }).json();
        dispatch({ type: REORDER_PROPERTY, payload: { oldIndex, newIndex } });
    };

    function addPropAction() {
        const values = {
            field: '',
            description: '',
            javadoc: '',
            type: '',
            label: '',
            jsonExpose: false,
            useExistingModel: false,
            id: uuidv4(),
            attributes: [],
            items: [],
        };
        dispatch({ type: ADD_PROPERTY, payload: values });
    }

    return (
        <div className="col-12 grid-margin stretch-card">
            <div className="card">
                <div className="card-body">
                    <h4 className="card-title">
                        Component Dialog Properties:
                    </h4>
                    <Tab.Container id="properties-builder-tabs" defaultActiveKey="mainProperties">
                        <Nav className="nav nav-tabs" role="tablist">
                            <Nav.Item>
                                <Nav.Link eventKey="mainProperties">Main Properties</Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="sharedProperties">Shared Properties</Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="globalProperties">Global Properties</Nav.Link>
                            </Nav.Item>
                        </Nav>
                        <Tab.Content>
                            <Tab.Pane eventKey="mainProperties">
                                <div>
                                    <SortableProperties onSortEnd={onSortEnd} useDragHandle>
                                        {global.options.properties.map((value, index) => (
                                            <SortableProperty key={`item-${value.field}`} index={index} propValues={value} />
                                        ))}
                                    </SortableProperties>
                                </div>
                                <div>
                                    <button onClick={addPropAction} type="button" className="btn btn-primary">
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Property
                                    </button>
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="sharedProperties">
                                <div>
                                    test2
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="globalProperties">
                                <div>
                                    test3
                                </div>
                            </Tab.Pane>
                        </Tab.Content>
                    </Tab.Container>
                </div>
            </div>
        </div>
    );
}

export default PropertyBuilderForm;
