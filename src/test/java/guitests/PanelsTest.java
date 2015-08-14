package guitests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;

import javafx.scene.input.KeyCode;
import ui.UI;
import util.events.PanelClickedEventHandler;

public class PanelsTest extends UITest {

    private static class Bool {
        public boolean value = false;
        public void negate() {
            value = !value;
        }
    }

    // TODO check if interactions result in any effects
    @Test
    public void panelsTest() {

        Bool eventTriggered = new Bool();

        UI.events.registerEvent((PanelClickedEventHandler) e -> eventTriggered.negate());

        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        push(KeyCode.ENTER);

        // Drag
        // TODO check whether panels are actually reordered
        drag("#dummy/dummy_col1_closeButton").to("#dummy/dummy_col0_closeButton");

        // Click
        eventTriggered.value = false;
        find("#dummy/dummy_col0_closeButton");
        moveBy(-50, 0);
        click(); // Click
        assertTrue(eventTriggered.value);

        // Close
        click("#dummy/dummy_col0_closeButton");
        try {
            find("#dummy/dummy_col0");
            fail();
        } catch (NoNodesFoundException e) {
            // Expected behavior.
        }

        // Switch primary repo
        doubleClick("#repositorySelector");
        doubleClick();
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        // Actually a check. If #dummy2/dummy2_col1 did not exist, this would throw an exception.
        click("#dummy2/dummy2_col1");
    }
}
