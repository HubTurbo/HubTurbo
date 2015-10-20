package unstable;

import guitests.UITest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ui.TestController;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;

import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;

import javafx.scene.control.Label;
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
        PanelControl panels = TestController.getUI().getPanelControl();

        Bool eventTriggered = new Bool();

        UI.events.registerEvent((PanelClickedEventHandler) e -> eventTriggered.negate());

        press(MAXIMIZE_WINDOW);

        press(CREATE_RIGHT_PANEL);
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);

        // Drag
        // TODO check whether panels are actually reordered

        Label closeButton0 = ((FilterPanel) panels.getPanel(0)).getCloseButton();
        Label closeButton1 = ((FilterPanel) panels.getPanel(1)).getCloseButton();
        drag(closeButton1).to(closeButton0);

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
        press(CREATE_RIGHT_PANEL);
        // Actually a check. If #dummy2/dummy2_col1 did not exist, this would throw an exception.
        click("#dummy2/dummy2_col1");
    }
}
