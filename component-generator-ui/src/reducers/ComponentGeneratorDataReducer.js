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
};

export default function (state = INITIAL_STATE, action) {
    switch (action.type) {
    case FETCH_CONFIGS:
        return {
            ...state,
            codeOwner: action.payload.codeOwner,
            bundlePath: action.payload.bundlePath,
            testPath: action.payload.testPath,
            appsPath: action.payload.appsPath,
            componentPath: action.payload.componentPath,
            modelInterfacePackage: action.payload.modelInterfacePackage,
            modelImplPackage: action.payload.modelImplPackage,
            copyrightYear: action.payload.copyrightYear,
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
