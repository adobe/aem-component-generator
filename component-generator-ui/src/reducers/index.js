import { combineReducers } from 'redux';
import ComponentGeneratorDataReducer from './ComponentGeneratorDataReducer';
import MobileActionsReducer from './MobileActionsReducer';

export default () => combineReducers({
    compData: ComponentGeneratorDataReducer,
    mobileAction: MobileActionsReducer,
});
