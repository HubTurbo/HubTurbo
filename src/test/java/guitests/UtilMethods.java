package guitests;

import java.util.HashMap;
import javafx.scene.input.KeyCode;

public class UtilMethods extends UITest {
    private static final HashMap<Character, KeyCode> chars; 
    static {
        chars = new HashMap<Character, KeyCode>();
        chars.put('~', KeyCode.BACK_QUOTE);
        chars.put('!', KeyCode.DIGIT1);
        chars.put('@', KeyCode.DIGIT2);
        chars.put('#', KeyCode.DIGIT3);
        chars.put('$', KeyCode.DIGIT4);
        chars.put('%', KeyCode.DIGIT5);
        chars.put('^', KeyCode.DIGIT6);
        chars.put('&', KeyCode.DIGIT7);
        chars.put('*', KeyCode.DIGIT8);
        chars.put('(', KeyCode.DIGIT9);
        chars.put(')', KeyCode.DIGIT0);
        chars.put('_', KeyCode.MINUS);
        chars.put('+', KeyCode.EQUALS);
        chars.put('{', KeyCode.OPEN_BRACKET);
        chars.put('}', KeyCode.CLOSE_BRACKET);
        chars.put(':', KeyCode.SEMICOLON);
        chars.put('"', KeyCode.QUOTE);
        chars.put('<', KeyCode.COMMA);
        chars.put('>', KeyCode.PERIOD);
        chars.put('?', KeyCode.SLASH);
    }
    
    public void typeString(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (chars.containsKey(text.charAt(i))){
                press(KeyCode.SHIFT).press(chars.get(text.charAt(i)))
                .release(chars.get(text.charAt(i))).release(KeyCode.SHIFT);
             
            } else {
                type(text.charAt(i));
            }
        }
    }
}
