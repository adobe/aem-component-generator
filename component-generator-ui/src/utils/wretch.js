import wretch from 'wretch';

function defaultErrorCatcher(error, originalRequest) {
    if (error.json === undefined) {
        throw new Error('invalid response format');
    }
    if (error.json.message !== undefined) {
        throw new Error(error.json.message);
    } else {
        const newError = new Error('Invalid response format');
        newError.errorJson = error.json;
        throw newError;
    }
}

const wretchInstance = wretch()
    .errorType('json')
    .catcher(401, defaultErrorCatcher)
    .catcher(400, defaultErrorCatcher)
    .catcher(403, defaultErrorCatcher)
    .catcher(429, defaultErrorCatcher)
    .catcher(404, defaultErrorCatcher)
    .catcher(500, defaultErrorCatcher)
    .catcher(503, defaultErrorCatcher);

export default wretchInstance;
