import { v4 as uuidv4 } from 'uuid';
import { randomId } from './Utils';

export const FORM_TYPES = [
    { value: 'checkbox', label: 'Checkbox' },
    { value: 'datepicker', label: 'Date picker' },
    // { value: 'heading', label: 'Heading field' },
    { value: 'hidden', label: 'Hidden field' },
    { value: 'image', label: 'Image field' },
    { value: 'multifield', label: 'Multifield' },
    { value: 'numberfield', label: 'Number field' },
    { value: 'pagefield', label: 'Page Field' },
    { value: 'pathfield', label: 'Path field' },
    { value: 'radio', label: 'Radio field' },
    { value: 'radiogroup', label: 'Radio group' },
    { value: 'select', label: 'Select box (Granite select)' },
    { value: 'tagfield', label: 'Tag Field' },
    { value: 'textarea', label: 'Text area' },
    { value: 'textfield', label: 'Text field' },
];
export const SLING_ADAPTABLES = [
    { value: 'resource', label: 'Resource' },
    { value: 'request', label: 'SlingHttpServletRequest' },
];
export const EMPTY_PROP = {
    field: '',
    description: '',
    javadoc: '',
    type: '',
    label: '',
    jsonExpose: false,
    useExistingModel: false,
    id: uuidv4(),
    attributes: [],
    items: [],
};
export const EMPTY_TAB = {
    label: '',
    id: `tab-${randomId(5)}`,
    fields: [],
};
export const MAIN = 'main';
export const SHARED = 'shared';
export const GLOBAL = 'global';
