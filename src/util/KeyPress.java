package util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyPress {

	private static int doublePressSpeed = 1000; // time difference between keypresses in ms
    private static long timeKeyDown = 0;
	public static KeyCode lastKeyPressedCode;

	public static  boolean isDoublePress(KeyEvent event, long KeyEventTime) {
		if ((KeyEventTime - timeKeyDown) < doublePressSpeed) {
			return true;
	    } else {
	        timeKeyDown = KeyEventTime;
	    }
	    lastKeyPressedCode = event.getCode();
	    return false;
	}
}
