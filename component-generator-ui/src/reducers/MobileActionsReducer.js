import {
    MOBILE_MENU_CLICK,
} from '../actions';

const INITIAL_STATE = {
    menuOpen: false,
};

export default function (state = INITIAL_STATE, action) {
    switch (action.type) {
    case MOBILE_MENU_CLICK:
        return {
            menuOpen: !state.menuOpen,
        };
    default:
        return {
            ...state,
        };
    }
}
