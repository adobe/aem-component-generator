/* eslint max-len: 0 */
import remove from 'lodash/remove';
import arrayMove from 'array-move';
import {
    FETCH_CONFIGS,
    CLEAR_COMPONENT_CONFIGS,
    REMOVE_PROPERTY,
    ADD_PROPERTY,
    ADD_SHARED_PROPERTY,
    ADD_GLOBAL_PROPERTY,
    ADD_TAB,
    REMOVE_TAB,
    UPDATE_TAB,
    REORDER_TAB,
    REORDER_SHARED_PROPERTY,
    REORDER_GLOBAL_PROPERTY,
    REORDER_PROPERTY,
} from '../actions';
import { FORM_TYPES, SHARED, MAIN, GLOBAL, SLING_ADAPTABLES } from '../utils/Constants';

const INITIAL_STATE = {
    codeOwner: 'NewCo Incorporated',
    bundlePath: 'core/src/main/java',
    testPath: 'core/src/test/java',
    appsPath: 'ui.apps/src/main/content/jcr_root/apps',
    componentPath: 'newco/components',
    modelInterfacePackage: 'com.newco.aem.base.models',
    modelImplPackage: 'com.newco.aem.base.models.impl',
    copyrightYear: 'current',
    componentTitle: '',
    componentNodeName: '',
    componentGroup: '',
    componentType: '',
    options: {
        genericJavadoc: false,
        js: true,
        jsTxt: true,
        css: true,
        cssTxt: true,
        html: true,
        testClass: true,
        junitMajorVersion: 5,
        slingModel: true,
        htmlContent: false,
        contentExporter: false,
        modelAdaptables: [
            { value: 'request', label: 'SlingHttpServletRequest' },
        ],
        properties: [],
        propertiesGlobal: [],
        propertiesShared: [],
        propertiesTabs: [],
        propertiesSharedTabs: [],
        propertiesGlobalTabs: [],
    },
};

function childAttrBuilder(attributes) {
    const attributesArr = [];
    if (attributes && Object.keys(attributes).length > 0) {
        Object.keys(attributes).forEach((keyName) => {
            attributesArr.push({ key: keyName, value: attributes[keyName] });
        });
    }
    return attributesArr;
}

function itemsBuilder(items) {
    const itemsArr = [];
    items.forEach((prop) => {
        let formTypesArr = {};
        if (prop.type) {
            FORM_TYPES.forEach((values) => {
                if (prop.type === values.value) {
                    formTypesArr = { label: values.label, value: values.value };
                }
            });
        }
        itemsArr.push({
            field: prop.field || '',
            description: prop.description || '',
            javadoc: prop.javadoc || '',
            type: formTypesArr,
            label: prop.label || '',
            id: prop.id,
            modelName: prop['model-name'] || '',
            jsonExpose: prop['json-expose'] || false,
            useExistingModel: prop['use-existing-model'] || false,
            attributes: childAttrBuilder(prop.attributes || []),
            jsonProperty: prop['json-property'] || '',
        });
    });
    return itemsArr;
}

function propertiesBuilder(propertiesPayload) {
    const propertiesArr = [];
    propertiesPayload.forEach((prop) => {
        let formTypesArr = {};
        if (prop.type) {
            FORM_TYPES.forEach((values) => {
                if (prop.type === values.value) {
                    formTypesArr = { label: values.label, value: values.value };
                }
            });
        }
        // convert attributes object to key value array
        const attributesArr = [];
        if (prop.attributes) {
            if (Object.keys(prop.attributes).length > 0) {
                Object.keys(prop.attributes).forEach((keyName) => {
                    attributesArr.push({ key: keyName, value: prop.attributes[keyName] });
                });
            }
        }
        propertiesArr.push({
            field: prop.field || '',
            description: prop.description || '',
            javadoc: prop.javadoc || '',
            type: formTypesArr,
            label: prop.label || '',
            id: prop.id,
            modelName: prop['model-name'] || '',
            jsonExpose: prop['json-expose'] || false,
            useExistingModel: prop['use-existing-model'] || false,
            attributes: attributesArr,
            jsonProperty: prop['json-property'] || '',
            items: itemsBuilder(prop.items || []),
        });
    });
    return propertiesArr;
}

function tabsBuilder(tabsPayload) {
    const tabsArr = [];
    tabsPayload.forEach((tab) => {
        const fieldsArr = [];
        if (tab.fields) {
            tab.fields.forEach((value) => {
                fieldsArr.push({ value, label: value });
            });
        }
        tabsArr.push({
            fields: fieldsArr,
            label: tab.label || '',
            id: tab.id,
        });
    });
    return tabsArr;
}

function optionsBuilder(optionsPayload) {
    const modelAdapters = [];
    if (optionsPayload['model-adaptables']) {
        optionsPayload['model-adaptables'].forEach((value) => {
            if (value === 'request') {
                modelAdapters.push(SLING_ADAPTABLES[1]);
            } else if (value === 'resource') {
                modelAdapters.push(SLING_ADAPTABLES[0]);
            }
        });
    }
    return {
        genericJavadoc: optionsPayload['generic-javadoc'],
        js: optionsPayload.js,
        jsTxt: optionsPayload.jstxt,
        css: optionsPayload.css,
        cssTxt: optionsPayload.csstxt,
        html: optionsPayload.html,
        htmlContent: optionsPayload['html-content'],
        testClass: optionsPayload.testclass,
        slingModel: optionsPayload.slingmodel,
        junitMajorVersion: optionsPayload['junit-major-version'],
        contentExporter: optionsPayload['content-exporter'],
        modelAdaptables: modelAdapters,
        properties: propertiesBuilder(optionsPayload.properties),
        propertiesGlobal: propertiesBuilder(optionsPayload['properties-global']),
        propertiesShared: propertiesBuilder(optionsPayload['properties-shared']),
        propertiesTabs: tabsBuilder(optionsPayload['properties-tabs']),
        propertiesSharedTabs: tabsBuilder(optionsPayload['properties-shared-tabs']),
        propertiesGlobalTabs: tabsBuilder(optionsPayload['properties-global-tabs']),
    };
}

function propertiesRemover(state, propToRemove) {
    const { type } = propToRemove;
    switch (type) {
    case MAIN: {
        const removedPropArr = remove(state.options.properties, (p) => p.id !== propToRemove.id);
        const propertiesTabUpdate = state.options.propertiesTabs;
        propertiesTabUpdate.forEach((tab, index) => {
            propertiesTabUpdate[index].fields = remove(tab.fields, (f) => f.value !== propToRemove.field);
        });
        return {
            ...state.options,
            properties: removedPropArr,
            propertiesTabs: propertiesTabUpdate,
        };
    }
    case SHARED: {
        const removedSharedPropArr = remove(state.options.propertiesShared, (p) => p.id !== propToRemove.id);
        const propertiesTabUpdate = state.options.propertiesSharedTabs;
        propertiesTabUpdate.forEach((tab, index) => {
            propertiesTabUpdate[index].fields = remove(tab.fields, (f) => f.value !== propToRemove.field);
        });
        return {
            ...state.options,
            propertiesShared: removedSharedPropArr,
            propertiesSharedTabs: propertiesTabUpdate,
        };
    }
    case GLOBAL: {
        const removedGlobalPropArr = remove(state.options.propertiesGlobal, (p) => p.id !== propToRemove.id);
        const propertiesGlobalTabUpdate = state.options.propertiesGlobalTabs;
        propertiesGlobalTabUpdate.forEach((tab, index) => {
            propertiesGlobalTabUpdate[index].fields = remove(tab.fields, (f) => f.value !== propToRemove.field);
        });
        return {
            ...state.options,
            propertiesGlobal: removedGlobalPropArr,
            propertiesGlobalTabs: propertiesGlobalTabUpdate,
        };
    }
    default:
        return {
            ...state.options,
        };
    }
}

function tabRemover(state, tabToRemove) {
    const { type } = tabToRemove;
    switch (type) {
    case MAIN: {
        const removedTabArr = remove(state.options.propertiesTabs, (t) => t.id !== tabToRemove.id);
        return {
            ...state.options,
            propertiesTabs: removedTabArr,
        };
    }
    case SHARED: {
        const removedTabArr = remove(state.options.propertiesSharedTabs, (t) => t.id !== tabToRemove.id);
        return {
            ...state.options,
            propertiesSharedTabs: removedTabArr,
        };
    }
    case GLOBAL: {
        const removedTabArr = remove(state.options.propertiesGlobalTabs, (t) => t.id !== tabToRemove.id);
        return {
            ...state.options,
            propertiesGlobalTabs: removedTabArr,
        };
    }
    default:
        return {
            ...state.options,
        };
    }
}

function tabUpdate(state, tabToUpdate) {
    const { type } = tabToUpdate;
    switch (type) {
    case MAIN: {
        const propertiesTabUpdate = state.options.propertiesTabs;
        propertiesTabUpdate.forEach((tab, index) => {
            if (tab.id === tabToUpdate.id) {
                propertiesTabUpdate[index].fields = tabToUpdate.fields;
            }
        });
        return {
            ...state.options,
            propertiesTabs: propertiesTabUpdate,
        };
    }
    case SHARED: {
        const propertiesTabUpdate = state.options.propertiesSharedTabs;
        propertiesTabUpdate.forEach((tab, index) => {
            if (tab.id === tabToUpdate.id) {
                propertiesTabUpdate[index].fields = tabToUpdate.fields;
            }
        });
        return {
            ...state.options,
            propertiesSharedTabs: propertiesTabUpdate,
        };
    }
    case GLOBAL: {
        const propertiesTabUpdate = state.options.propertiesGlobalTabs;
        propertiesTabUpdate.forEach((tab, index) => {
            if (tab.id === tabToUpdate.id) {
                propertiesTabUpdate[index].fields = tabToUpdate.fields;
            }
        });
        return {
            ...state.options,
            propertiesGlobalTabs: propertiesTabUpdate,
        };
    }
    default:
        return {
            ...state.options,
        };
    }
}

function propertiesAdder(state, propToAdd) {
    return {
        ...state.options,
        properties: state.options.properties.concat(propToAdd),
    };
}

function propertiesSharedAdder(state, propToAdd) {
    return {
        ...state.options,
        propertiesShared: state.options.propertiesShared.concat(propToAdd),
    };
}

function propertiesGlobalAdder(state, propToAdd) {
    return {
        ...state.options,
        propertiesGlobal: state.options.propertiesGlobal.concat(propToAdd),
    };
}

function tabsAdder(state, tabToAdd) {
    const { type } = tabToAdd;
    switch (type) {
    case MAIN: {
        return {
            ...state.options,
            propertiesTabs: state.options.propertiesTabs.concat(tabToAdd),
        };
    }
    case SHARED: {
        return {
            ...state.options,
            propertiesSharedTabs: state.options.propertiesSharedTabs.concat(tabToAdd),
        };
    }
    case GLOBAL: {
        return {
            ...state.options,
            propertiesGlobalTabs: state.options.propertiesGlobalTabs.concat(tabToAdd),
        };
    }
    default:
        return {
            ...state.options,
        };
    }
}

function propertiesMover(state, propToMove) {
    return {
        ...state.options,
        properties: arrayMove(state.options.properties, propToMove.oldIndex, propToMove.newIndex),
    };
}

function propertiesMoverShared(state, propToMove) {
    return {
        ...state.options,
        propertiesShared: arrayMove(state.options.propertiesShared, propToMove.oldIndex, propToMove.newIndex),
    };
}

function propertiesMoverGlobal(state, propToMove) {
    return {
        ...state.options,
        propertiesGlobal: arrayMove(state.options.propertiesGlobal, propToMove.oldIndex, propToMove.newIndex),
    };
}

function tabsMover(state, tabToMove) {
    return {
        ...state.options,
        propertiesTabs: arrayMove(state.options.propertiesTabs, tabToMove.oldIndex, tabToMove.newIndex),
    };
}

export default function (state = INITIAL_STATE, action) {
    switch (action.type) {
    case FETCH_CONFIGS:
        return {
            ...state,
            codeOwner: action.payload['project-settings']['code-owner'],
            bundlePath: action.payload['project-settings']['bundle-path'],
            testPath: action.payload['project-settings']['test-path'],
            appsPath: action.payload['project-settings']['apps-path'],
            copyrightYear: action.payload['project-settings'].year,
            componentPath: action.payload['project-settings']['component-path'],
            modelInterfacePackage: action.payload['project-settings']['model-interface-pkg'],
            modelImplPackage: action.payload['project-settings']['model-impl-pkg'],
            componentTitle: action.payload.title,
            componentNodeName: action.payload.name,
            componentGroup: action.payload.group,
            componentType: action.payload.type,
            options: optionsBuilder(action.payload.options),
        };
    case REMOVE_PROPERTY:
        return {
            ...state,
            options: propertiesRemover(state, action.payload),
        };
    case REMOVE_TAB:
        return {
            ...state,
            options: tabRemover(state, action.payload),
        };
    case UPDATE_TAB:
        return {
            ...state,
            options: tabUpdate(state, action.payload),
        };
    case ADD_PROPERTY:
        return {
            ...state,
            options: propertiesAdder(state, action.payload),
        };
    case ADD_SHARED_PROPERTY:
        return {
            ...state,
            options: propertiesSharedAdder(state, action.payload),
        };
    case ADD_GLOBAL_PROPERTY:
        return {
            ...state,
            options: propertiesGlobalAdder(state, action.payload),
        };
    case ADD_TAB:
        return {
            ...state,
            options: tabsAdder(state, action.payload),
        };
    case REORDER_PROPERTY:
        return {
            ...state,
            options: propertiesMover(state, action.payload),
        };
    case REORDER_SHARED_PROPERTY:
        return {
            ...state,
            options: propertiesMoverShared(state, action.payload),
        };
    case REORDER_GLOBAL_PROPERTY:
        return {
            ...state,
            options: propertiesMoverGlobal(state, action.payload),
        };
    case REORDER_TAB:
        return {
            ...state,
            options: tabsMover(state, action.payload),
        };
    case CLEAR_COMPONENT_CONFIGS:
        return {
            ...state,
            componentTitle: '',
            componentNodeName: '',
        };
    default:
        return {
            ...state,
        };
    }
}
