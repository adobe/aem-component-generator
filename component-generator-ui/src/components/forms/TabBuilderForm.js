/* eslint max-len: 0 */
import React from 'react';
import { Nav, Tab, Alert } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import { Link } from 'react-router-dom';
import { ADD_TAB, API_ROOT, REORDER_TAB } from '../../actions';
import SortableTab from './helper/SortableTab';
import wretch from '../../utils/wretch';
import routes from '../../routes';
import { EMPTY_TAB, GLOBAL, MAIN, SHARED } from '../../utils/Constants';
import { randomId } from '../../utils/Utils';

function TabBuilderForm() {
    const dispatch = useDispatch();
    const global = useSelector((state) => state.compData);

    const SortableTabsContainer = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const SortableSharedTabsContainer = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const SortableGlobalTabsContainer = SortableContainer(({ children }) => (
        <div className="py-2">
            {children}
        </div>
    ));

    const onSortEnd = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/tabs`).post({ oldIndex, newIndex, moveTab: true, tabType: MAIN }).json();
        dispatch({ type: REORDER_TAB, payload: { oldIndex, newIndex } });
    };

    const onSortEndShared = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/tabs`).post({ oldIndex, newIndex, moveTab: true, tabType: SHARED }).json();
        dispatch({ type: REORDER_TAB, payload: { oldIndex, newIndex } });
    };

    const onSortEndGlobal = async ({ oldIndex, newIndex }) => {
        await wretch.url(`${API_ROOT}/tabs`).post({ oldIndex, newIndex, moveTab: true, tabType: GLOBAL }).json();
        dispatch({ type: REORDER_TAB, payload: { oldIndex, newIndex } });
    };

    const addTabAction = (event) => {
        const { type } = event.target.dataset;
        dispatch({ type: ADD_TAB, payload: { ...EMPTY_TAB, type, id: `tab-${randomId(5)}` } });
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
                                            <SortableTab key={`item-${value.id}`} index={index} propValues={value} type={MAIN} />
                                        ))}
                                    </SortableTabsContainer>
                                </div>
                                <div>
                                    <button title="Add new dialog tab" onClick={addTabAction} type="button" className="btn btn-primary" data-type={MAIN}>
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Tab
                                    </button>
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="sharedTabProperties">
                                {global.options.propertiesShared.length <= 0
                                && (
                                    <Alert variant="danger">
                                        You still need to define your shared&nbsp;
                                        <Link to={`${routes.dialogProperties}`}>dialog properties </Link>
                                        before you can add them to your
                                        dialog&apos;s tabs.
                                    </Alert>
                                )}
                                <div>
                                    <SortableSharedTabsContainer onSortEnd={onSortEndShared} useDragHandle>
                                        {global.options.propertiesSharedTabs.map((value, index) => (
                                            <SortableTab key={`item-${value.id}`} index={index} propValues={value} type={SHARED} />
                                        ))}
                                    </SortableSharedTabsContainer>
                                </div>
                                <div>
                                    <button onClick={addTabAction} type="button" className="btn btn-primary" data-type={SHARED}>
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Shared Tab
                                    </button>
                                </div>
                            </Tab.Pane>
                            <Tab.Pane eventKey="globalTabProperties">
                                {global.options.propertiesGlobal.length <= 0
                                && (
                                    <Alert variant="danger">
                                        You still need to define your global&nbsp;
                                        <Link to={`${routes.dialogProperties}`}>dialog properties </Link>
                                        before you can add them to your
                                        dialog&apos;s tabs.
                                    </Alert>
                                )}
                                <div>
                                    <SortableGlobalTabsContainer onSortEnd={onSortEndGlobal} useDragHandle>
                                        {global.options.propertiesGlobalTabs.map((value, index) => (
                                            <SortableTab key={`item-${value.id}`} index={index} propValues={value} type={GLOBAL} />
                                        ))}
                                    </SortableGlobalTabsContainer>
                                </div>
                                <div>
                                    <button onClick={addTabAction} type="button" className="btn btn-primary" data-type={GLOBAL}>
                                        <i className="mdi mdi-plus pr-1" />
                                        Add Global Tab
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

export default TabBuilderForm;
