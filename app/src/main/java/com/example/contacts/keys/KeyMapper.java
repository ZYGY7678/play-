package com.example.contacts.keys;

import android.view.KeyEvent;

public final class KeyMapper {
    private KeyMapper() {}

    public enum Action {
        UP, DOWN, LEFT, RIGHT, SELECT, CALL, BACK, MENU,
        SOFT_LEFT, SOFT_RIGHT, DIGIT, STAR, POUND, PAGE_UP, PAGE_DOWN, OTHER
    }

    public static Action map(KeyEvent e, boolean menuBackMapping) {
        if (e == null) return Action.OTHER;
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.KEYCODE_DPAD_UP: return Action.UP;
            case KeyEvent.KEYCODE_DPAD_DOWN: return Action.DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT: return Action.LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return Action.RIGHT;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: return Action.SELECT;
            case KeyEvent.KEYCODE_CALL: return Action.CALL;
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_BACK: return Action.BACK;
            case KeyEvent.KEYCODE_MENU: return menuBackMapping ? Action.SOFT_LEFT : Action.MENU;
            case KeyEvent.KEYCODE_SOFT_LEFT: return Action.SOFT_LEFT;
            case KeyEvent.KEYCODE_SOFT_RIGHT: return menuBackMapping ? Action.BACK : Action.SOFT_RIGHT;
            case KeyEvent.KEYCODE_STAR: return Action.STAR;
            case KeyEvent.KEYCODE_POUND: return Action.POUND;
            case KeyEvent.KEYCODE_VOLUME_UP: return Action.PAGE_UP;
            case KeyEvent.KEYCODE_VOLUME_DOWN: return Action.PAGE_DOWN;
            case KeyEvent.KEYCODE_0: return Action.DIGIT;
            case KeyEvent.KEYCODE_1: return Action.DIGIT;
            case KeyEvent.KEYCODE_2: return Action.DIGIT;
            case KeyEvent.KEYCODE_3: return Action.DIGIT;
            case KeyEvent.KEYCODE_4: return Action.DIGIT;
            case KeyEvent.KEYCODE_5: return Action.DIGIT;
            case KeyEvent.KEYCODE_6: return Action.DIGIT;
            case KeyEvent.KEYCODE_7: return Action.DIGIT;
            case KeyEvent.KEYCODE_8: return Action.DIGIT;
            case KeyEvent.KEYCODE_9: return Action.DIGIT;
            default: return Action.OTHER;
        }
    }

    public static int digit(KeyEvent e) {
        if (e == null) return -1;
        int k = e.getKeyCode();
        if (k >= KeyEvent.KEYCODE_0 && k <= KeyEvent.KEYCODE_9) return k - KeyEvent.KEYCODE_0;
        return -1;
    }
}
