export function randomId(length) {
    const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let str = '';
    let now = Date.now();
    if (typeof performance === 'object' && typeof performance.now === 'function') {
        now = performance.now().toString().replace('.', '');
    }
    for (let i = 0; i < length; i += 1) {
        const rand = Math.floor(Math.random() * ALPHABET.length);
        str += ALPHABET.substring(rand, rand + 1);
    }
    return str.substr(0, str.length - 1)
        + now.toString().substr(now.toString().length - 1, now.toString().length);
}

export function validateGlobalData(global) {
    const result = { valid: true, message: 'Validation success', invalidFields: [] };
    if (!global.codeOwner) {
        result.invalidFields.push('Code owner (global configs)');
    }
    if (!global.bundlePath) {
        result.invalidFields.push('Bundle path (global configs)');
    }
    if (!global.testPath) {
        result.invalidFields.push('Test path (global configs)');
    }
    if (!global.appsPath) {
        result.invalidFields.push('App path (global configs)');
    }
    if (!global.componentPath) {
        result.invalidFields.push('Component path (global configs)');
    }
    if (!global.modelInterfacePackage) {
        result.invalidFields.push('Model interface (global configs)');
    }
    if (!global.modelImplPackage) {
        result.invalidFields.push('Model impl package (global configs)');
    }
    if (!global.copyrightYear) {
        result.invalidFields.push('Copyright year (global configs)');
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
    if (!global.options.propertiesTabs) {
        result.invalidFields.push('Dialog Tabs (need at least one)');
    } else if (global.options.propertiesTabs <= 0) {
        result.invalidFields.push('Dialog Tabs (need at least one)');
    }

    if (result.invalidFields.length > 0) {
        result.valid = false;
        result.message = 'Validation failed for the following required field(s):';
    }
    return result;
}
