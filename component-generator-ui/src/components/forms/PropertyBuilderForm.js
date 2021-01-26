/* eslint max-len: 0 */
import React from 'react';
import { Nav, Tab } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { v4 as uuidv4 } from 'uuid';
import {
    EMPTY_PROP,
    GLOBAL,
    MAIN,
    SHARED,
} from '../../utils/Constants';
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
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: MAIN }).json();
        dispatch({ type: REORDER_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const onSortEndShared = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: SHARED }).json();
        dispatch({ type: REORDER_SHARED_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const onSortEndGlobal = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/properties`).post({ oldIndex, newIndex, moveProp: true, propType: GLOBAL }).json();
        dispatch({ type: REORDER_GLOBAL_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const addPropAction = () => {
        dispatch({ type: ADD_PROPERTY, payload: { ...EMPTY_PROP, id: uuidv4() } });
    };

    const addSharedPropAction = () => {
        dispatch({ type: ADD_SHARED_PROPERTY, payload: { ...EMPTY_PROP, id: uuidv4() } });
    };

    const addGlobalPropAction = () => {
        dispatch({ type: ADD_GLOBAL_PROPERTY, payload: { ...EMPTY_PROP, id: uuidv4() } });
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
                                <Nav.Link eventKey="mainProperties">
                                    Main Properties
                                    <span title="number of properties defined" className="badge badge-pill badge-primary ml-2">{global.options.properties.length || 0}</span>
                                </Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="sharedProperties">
                                    Shared Properties
                                    <span title="number of shared properties defined" className="badge badge-pill badge-primary ml-2">{global.options.propertiesShared.length || 0}</span>
                                </Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="globalProperties">
                                    Global Properties
                                    <span title="number of global properties defined" className="badge badge-pill badge-primary ml-2">{global.options.propertiesGlobal.length || 0}</span>
                                </Nav.Link>
                            </Nav.Item>
                        </Nav>
                        <Tab.Content>
                            <Tab.Pane eventKey="mainProperties">
                                <div>
                                    <SortableProperties onSortEnd={onSortEnd} useDragHandle>
                                        {global.options.properties.map((value, index) => (
                                            <SortableProperty key={`item-${value.id}`} index={index} propValues={value} type={MAIN} />
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
                                            <SortableProperty key={`item-${value.id}`} index={index} propValues={value} type={SHARED} />
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
                                            <SortableProperty key={`item-${value.id}`} index={index} propValues={value} type={GLOBAL} />
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
