import React from 'react';
import { Nav, Tab } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { useUIDSeed } from 'react-uid';
import { ADD_TAB } from '../../actions';

function TabBuilderForm() {
    const dispatch = useDispatch();
    const seed = useUIDSeed();
    const global = useSelector((state) => state.compData);

    const SortableTabs = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const onSortEnd = ({ oldIndex, newIndex }) => {
        console.log('sorted', oldIndex, newIndex);
        // dispatch({ type: REORDER_PROPERTY, payload: { oldIndex, newIndex } });
    };

    const addTabAction = () => {
        const values = {
            label: '',
            id: `tab-${seed('tab')}`,
            fields: [],
        };
        dispatch({ type: ADD_TAB, payload: values });
    };

    return (
        <div className="col-12 grid-margin stretch-card">
            <div className="card">
                <div className="card-body">
                    <h4 className="card-title">
                        Component Dialog Tabs:
                    </h4>
                    <Tab.Container id="tab-builder-ui" defaultActiveKey="mainTabProperties">
                        <Nav className="nav nav-tabs" role="tablist">
                            <Nav.Item>
                                <Nav.Link eventKey="mainTabProperties">Main Tab(s)</Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="sharedTabProperties">Shared Tab(s)</Nav.Link>
                            </Nav.Item>
                            <Nav.Item>
                                <Nav.Link eventKey="globalTabProperties">Global Tab(s)</Nav.Link>
                            </Nav.Item>
                        </Nav>
                        <Tab.Content>
                            <Tab.Pane eventKey="mainTabProperties">
                                <div>
                                    Main tab properties
                                </div>
                                <div>
                                    <button onClick={addTabAction} type="button" className="btn btn-primary">
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Tab
                                    </button>
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="sharedTabProperties">
                                <div>
                                    test2
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="globalTabProperties">
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

export default TabBuilderForm;
