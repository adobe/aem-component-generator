import {
    FETCH_CONFIGS,
    CLEAR_COMPONENT_CONFIGS,
} from '../actions';

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

function optionsBuilder(optionsPayload) {
    const slingAdaptables = [
        { value: 'request', label: 'SlingHttpServletRequest' },
        { value: 'resource', label: 'Resource' },
    ];
    const modelAdapters = [];
    if (optionsPayload['model-adaptables']) {
        optionsPayload['model-adaptables'].map((value) => {
            if (value === 'request') {
                modelAdapters.push(slingAdaptables[0]);
            } else if (value === 'resource') {
                modelAdapters.push(slingAdaptables[1]);
            }
        });
    }
    return {
        genericJavadoc: optionsPayload['generic-javadoc'],
        js: optionsPayload.js,
        jsTxt: optionsPayload.jstxt,
        css: optionsPayload.css,
        cssTxt: optionsPayload.cssTxt,
        html: optionsPayload.html,
        testClass: optionsPayload.testClass,
        junitMajorVersion: optionsPayload['junit-major-version'],
        contentExporter: optionsPayload['content-exporter'],
        modelAdaptables: modelAdapters,
        properties: optionsPayload.properties,
        propertiesGlobal: optionsPayload['properties-global'],
        propertiesShared: optionsPayload['properties-shared'],
        propertiesTabs: optionsPayload['properties-tabs'],
        propertiesSharedTabs: optionsPayload['properties-shared-tabs'],
        propertiesGlobalTabs: optionsPayload['properties-global-tabs'],
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
