package util;

import javafx.scene.input.KeyEvent;

public class KeyPress {

	private static int doublePressSpeed = 1000; // time difference between keypresses in ms
    private static long timeKeyDown = 0;

	public static  boolean isDoublePress(KeyEvent event, long KeyEventTime) {
		if ((KeyEventTime - timeKeyDown) < doublePressSpeed) {
			return true;
	    } else {
	        timeKeyDown = KeyEventTime;
	    }
	    return false;
	}
	
	public static void setTimeKeyDown(long time){
		timeKeyDown = time;
	}
}
