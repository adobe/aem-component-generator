import remove from 'lodash/remove';
import arrayMove from 'array-move';
import {
    FETCH_CONFIGS,
    CLEAR_COMPONENT_CONFIGS,
    REMOVE_PROPERTY,
    ADD_PROPERTY,
    REORDER_PROPERTY,
} from '../actions';
import { FORM_TYPES, SLING_ADAPTABLES } from '../utils/Constants';

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

function propertiesBuilder(propertiesPayload) {
    const propertiesArr = [];
    // eslint-disable-next-line array-callback-return
    propertiesPayload.map((prop, index) => {
        let formTypesArr = {};
        if (prop.type) {
            // eslint-disable-next-line array-callback-return
            FORM_TYPES.map((values, ind) => {
                if (prop.type === values.value) {
                    formTypesArr = { label: values.label, value: values.value };
                }
            });
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
            attributes: prop.attributes || {},
            jsonProperty: prop['json-property'] || '',
            items: prop.items || [],
        });
    });
    return propertiesArr;
}

function optionsBuilder(optionsPayload) {
    const modelAdapters = [];
    if (optionsPayload['model-adaptables']) {
        // eslint-disable-next-line array-callback-return
        optionsPayload['model-adaptables'].map((value) => {
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
        propertiesGlobal: optionsPayload['properties-global'],
        propertiesShared: optionsPayload['properties-shared'],
        propertiesTabs: optionsPayload['properties-tabs'],
        propertiesSharedTabs: optionsPayload['properties-shared-tabs'],
        propertiesGlobalTabs: optionsPayload['properties-global-tabs'],
    };
}

function propertiesRemover(state, propToRemove) {
    const removedPropArr = remove(state.options.properties, (p) => p.id !== propToRemove.id);
    return {
        ...state.options,
        properties: removedPropArr,
    };
}

function propertiesAdder(state, propToAdd) {
    return {
        ...state.options,
        properties: state.options.properties.concat(propToAdd),
    };
}

function propertiesMover(state, propToMove) {
    return {
        ...state.options,
        properties: arrayMove(state.options.properties, propToMove.oldIndex, propToMove.newIndex),
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
    case ADD_PROPERTY:
        return {
            ...state,
            options: propertiesAdder(state, action.payload),
        };
    case REORDER_PROPERTY:
        return {
            ...state,
            options: propertiesMover(state, action.payload),
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
