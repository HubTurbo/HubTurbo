package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.components.KeyboardShortcuts;

import static ui.components.KeyboardShortcuts.DOUBLE_PRESS;

public class KeyboardShortcutsTest extends UITest {

    // TODO test that events have been triggered
    @Test
    public void keyboardShortcutsTest() {
        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);

        // mid-sized window
        press(KeyCode.CONTROL).press(KeyCode.D).release(KeyCode.D).release(KeyCode.CONTROL);

        // jump from filter box to first issue
        press(KeyCode.CONTROL).press(KeyCode.DOWN).release(KeyCode.DOWN).release(KeyCode.CONTROL);

        // jump from issue list to filter box
        press(KeyCode.CONTROL).press(KeyCode.UP).release(KeyCode.UP).release(KeyCode.CONTROL);

        // jump from filter box to first issue
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

        // jump from issue list to filter box
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

        // jump to last issue
        push(KeyCode.END);

        // jump to first issue
        push(KeyCode.HOME);

        push(getKeyCode("DOWN_ISSUE"));
        push(getKeyCode("DOWN_ISSUE"));
        push(getKeyCode("UP_ISSUE"));

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

        push(getKeyCode("RIGHT_PANEL"));
        push(getKeyCode("LEFT_PANEL"));
        push(getKeyCode("RIGHT_PANEL"));
        push(getKeyCode("LEFT_PANEL"));

        click("#dummy/dummy_col1_1");

        // mark as read/unread
        push(getKeyCode("MARK_AS_READ"));
        push(getKeyCode("MARK_AS_UNREAD"));

        // minimize window
        press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL); // run this last
    }

    public KeyCode getKeyCode(String shortcut) {
        return KeyCode.getKeyCode(KeyboardShortcuts.getDefaultKeyboardShortcuts().get(shortcut));
    }
}
