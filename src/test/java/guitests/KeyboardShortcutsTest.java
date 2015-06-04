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
        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);

        // jump from issue list to filter box
        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);

        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);

        // jump to last issue
        press(KeyCode.END).release(KeyCode.END);

        // jump to first issue
        press(KeyCode.HOME).release(KeyCode.HOME);

        press(KeyCode.V).release(KeyCode.V);
        press(KeyCode.V).release(KeyCode.V);
        press(KeyCode.T).release(KeyCode.T);

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);

        press(KeyCode.F).release(KeyCode.F);
        press(KeyCode.D).release(KeyCode.D);
        press(KeyCode.F).release(KeyCode.F);
        press(KeyCode.D).release(KeyCode.D);
    }
}
