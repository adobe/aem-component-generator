export default function validateGlobalData(global) {
    const result = { valid: true, message: 'Validation success', invalidFields: [] };
    if (!global.codeOwner) {
        result.invalidFields.push('Code owner (global configuration)');
    }
    if (!global.bundlePath) {
        result.invalidFields.push('Bundle path (global configuration)');
    }
    if (!global.testPath) {
        result.invalidFields.push('Test path (global configuration)');
    }
    if (!global.appsPath) {
        result.invalidFields.push('App path (global configuration)');
    }
    if (!global.componentPath) {
        result.invalidFields.push('Component path (global configuration)');
    }
    if (!global.modelInterfacePackage) {
        result.invalidFields.push('Model interface (global configuration)');
    }
    if (!global.modelImplPackage) {
        result.invalidFields.push('Model impl package (global configuration)');
    }
    if (!global.copyrightYear) {
        result.invalidFields.push('Copyright year (global configuration)');
    }
    if (!global.componentTitle) {
        result.invalidFields.push('Component Title (Component Configs)');
    }
    if (!global.componentNodeName) {
        result.invalidFields.push('Component Node Name (Component Configs)');
    }
    if (!global.componentGroup) {
        result.invalidFields.push('Component Group (Component Configs)');
    }
    if (!global.componentType) {
        result.invalidFields.push('Component Type (Component Configs)');
    }
    if (!global.options.modelAdaptables) {
        result.invalidFields.push('Model Adapters (Component Configs)');
    }
    if (!global.options.properties) {
        result.invalidFields.push('Component Properties (need at least one)');
    } else if (global.options.properties.length <= 0) {
        result.invalidFields.push('Component Properties (need at least one)');
    }

    if (result.invalidFields.length > 0) {
        result.valid = false;
        result.message = 'Validation failed for the following required field(s):';
    }
    return result;
}
