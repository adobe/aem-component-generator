import React from 'react';
import { Nav, Tab, Alert } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { ADD_TAB, API_ROOT, REORDER_TAB } from '../../actions';
import SortableTab from './helper/SortableTab';
import { randomId } from '../../utils/Utils';
import wretch from '../../utils/wretch';
import { Link } from 'react-router-dom';
import routes from '../../routes';

function TabBuilderForm() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);

    const SortableTabsContainer = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const onSortEnd = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/tabs`).post({ oldIndex, newIndex, moveTab: true }).json();
        dispatch({ type: REORDER_TAB, payload: { oldIndex, newIndex } });
    };

    const addTabAction = () => {
        const values = {
            label: '',
            id: `tab-${randomId(5)}`,
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
                                {global.options.properties.length <= 0
                                    && (
                                        <Alert variant="danger">
                                            You still need to define your&nbsp;
                                            <Link to={`${routes.dialogProperties}`}>dialog properties </Link>
                                            before you can add them to your
                                            dialog&apos;s tabs.
                                        </Alert>
                                    )}
                                <div>
                                    <SortableTabsContainer onSortEnd={onSortEnd} useDragHandle>
                                        {global.options.propertiesTabs.map((value, index) => (
                                            <SortableTab key={`item-${value.id}`} index={index} propValues={value} />
                                        ))}
                                    </SortableTabsContainer>
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
