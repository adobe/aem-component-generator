/* eslint max-len: 0 */
import React from 'react';
import { Nav, Tab } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { EMPTY_PROP } from '../../utils/Constants';
import {
    ADD_PROPERTY,
    ADD_SHARED_PROPERTY,
    ADD_GLOBAL_PROPERTY,
    REORDER_SHARED_PROPERTY,
    REORDER_GLOBAL_PROPERTY,
    API_ROOT,
    REORDER_PROPERTY,
} from '../../actions';
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

    const SortableSharedProperties = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const SortableGlobalProperties = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const onSortEnd = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: 'main' }).json();
        dispatch({ type: REORDER_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const onSortEndShared = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: 'shared' }).json();
        dispatch({ type: REORDER_SHARED_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const onSortEndGlobal = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: 'global' }).json();
        dispatch({ type: REORDER_GLOBAL_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const addPropAction = () => {
        dispatch({ type: ADD_PROPERTY, payload: EMPTY_PROP });
    };

    const addSharedPropAction = () => {
        dispatch({ type: ADD_SHARED_PROPERTY, payload: EMPTY_PROP });
    };

    const addGlobalPropAction = () => {
        dispatch({ type: ADD_GLOBAL_PROPERTY, payload: EMPTY_PROP });
    };

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
                                            <SortableProperty key={`item-${value.field}`} index={index} propValues={value} type="main" />
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
                                    <SortableSharedProperties onSortEnd={onSortEndShared} useDragHandle>
                                        {global.options.propertiesShared.map((value, index) => (
                                            <SortableProperty key={`item-${value.field}`} index={index} propValues={value} type="shared" />
                                        ))}
                                    </SortableSharedProperties>
                                </div>
                                <div>
                                    <button onClick={addSharedPropAction} type="button" className="btn btn-primary">
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Shared Property
                                    </button>
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="globalProperties">
                                <div>
                                    <SortableGlobalProperties onSortEnd={onSortEndGlobal} useDragHandle>
                                        {global.options.propertiesGlobal.map((value, index) => (
                                            <SortableProperty key={`item-${value.field}`} index={index} propValues={value} type="global" />
                                        ))}
                                    </SortableGlobalProperties>
                                </div>
                                <div>
                                    <button onClick={addGlobalPropAction} type="button" className="btn btn-primary">
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Global Property
                                    </button>
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
