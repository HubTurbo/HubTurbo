package util;

import javafx.scene.input.KeyCode;

public final class KeyPress {

    private static int keyPressSpeed = 1000; // time difference between keypresses in ms
    private static long lastKeyEventTime = 0;
    private static KeyCode lastKeyPressedCode;

    public static boolean isDoublePress(KeyCode matchingKeyCode, KeyCode currentKeyCode) {
        long keyEventTime = System.currentTimeMillis();
        if ((keyEventTime - lastKeyEventTime) < keyPressSpeed
                && currentKeyCode.equals(lastKeyPressedCode)
                && currentKeyCode.equals(matchingKeyCode)) {
            lastKeyPressedCode = null;
            lastKeyEventTime = 0;
            return true;
        } else {
            lastKeyPressedCode = currentKeyCode;
            lastKeyEventTime = keyEventTime;
        }
        return false;
    }

    public static boolean isValidKeyCombination(KeyCode firstKeyPressed, KeyCode keyPressed) {
        long keyEventTime = System.currentTimeMillis();
        if ((keyEventTime - lastKeyEventTime) < keyPressSpeed && firstKeyPressed.equals(lastKeyPressedCode)) {
            lastKeyPressedCode = null;
            lastKeyEventTime = 0;
            return true;
        } else {
            lastKeyPressedCode = keyPressed;
            lastKeyEventTime = keyEventTime;
        }
        return false;
    }

    public static void setLastKeyPressedCodeAndTime(KeyCode code) {
        lastKeyPressedCode = code;
        lastKeyEventTime = System.currentTimeMillis();

    }

    private KeyPress() {
    }
}
