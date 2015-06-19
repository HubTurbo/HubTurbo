package guitests;

import org.junit.Test;

import javafx.scene.input.KeyCode;

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
        push(KeyCode.SPACE).push(KeyCode.SPACE);

        // jump from issue list to filter box
        push(KeyCode.SPACE).push(KeyCode.SPACE);

        push(KeyCode.SPACE).push(KeyCode.SPACE);

        // jump to last issue
        push(KeyCode.END);

        // jump to first issue
        push(KeyCode.HOME);

        push(KeyCode.V);
        push(KeyCode.V);
        push(KeyCode.T);

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        push(KeyCode.SPACE).push(KeyCode.SPACE);

        push(KeyCode.F);
        push(KeyCode.D);
        push(KeyCode.F);
        push(KeyCode.D);

        click("#dummy/dummy_col1_1");

        // mark as read/unread
        push(KeyCode.E);
        push(KeyCode.U);

        // minimize window
        press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL); // run this last
    }
}
