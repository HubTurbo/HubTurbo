package util;

import javafx.scene.input.KeyCode;

public class KeyPress {

	private static int doublePressSpeed = 1000; // time difference between keypresses in ms
    private static long lastKeyEventTime = 0;
    private static KeyCode lastKeyPressedCode;

	public static  boolean isDoublePress(KeyCode code) {
		long keyEventTime = System.currentTimeMillis();
		if ((keyEventTime - lastKeyEventTime) < doublePressSpeed && code.equals(lastKeyPressedCode)) {
			lastKeyEventTime = 0;
			return true;
	    } else {
	    	lastKeyPressedCode = code;
	        lastKeyEventTime = keyEventTime;
	    }
	    return false;
	}
}
