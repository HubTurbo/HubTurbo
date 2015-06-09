package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import ui.UI;
import util.events.ColumnClickedEventHandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ColumnsTest extends UITest {

    private static boolean eventTriggered = false;

    // TODO check if interactions result in any effects
    @Test
    public void columnsTest() {
        UI.events.registerEvent((ColumnClickedEventHandler) e -> eventTriggered = !eventTriggered);

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        press(KeyCode.ENTER).release(KeyCode.ENTER);

        // Drag
        // TODO check whether columns are actually reordered
        drag("#dummy/dummy_col1_closeButton").to("#dummy/dummy_col0_closeButton");

        // Click
        eventTriggered = false;
        find("#dummy/dummy_col0_closeButton");
        moveBy(-15, 0);
        click(); // Click
        assertTrue(eventTriggered);

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
        type("#dummy2/dummy2");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        // Actually a check. If #dummy2/dummy2_col1 did not exist, this would throw an exception.
        click("#dummy2/dummy2_col1");
    }
}
