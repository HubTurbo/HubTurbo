package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.PanelControl;
import util.events.ModelUpdatedEventHandler;

import static org.junit.Assert.assertEquals;

public class MenuControlTest extends UITest {

    private boolean modelUpdatedEventTriggered;

    @Test
    public void menuControlTest() {
        modelUpdatedEventTriggered = false;
        UI.events.registerEvent((ModelUpdatedEventHandler) e -> modelUpdatedEventTriggered = true);
        PanelControl panelControl = (PanelControl) find("#dummy/dummy_col0").getParent();
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, panelControl.getNumberOfPanels());
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        assertEquals(1, panelControl.getNumberOfPanels());
        press(KeyCode.CONTROL).press(KeyCode.SHIFT).press(KeyCode.P).release(KeyCode.P)
            .release(KeyCode.SHIFT).release(KeyCode.CONTROL);
        assertEquals(2, panelControl.getNumberOfPanels());

        click("Panels");
        click("Create");
        assertEquals(3, panelControl.getNumberOfPanels());
        click("Panels");
        click("Create (Left)");
        assertEquals(4, panelControl.getNumberOfPanels());
        click("Panels");
        click("Close");
        assertEquals(3, panelControl.getNumberOfPanels());
        click("Panels");
        click("Close");
        assertEquals(2, panelControl.getNumberOfPanels());

        click("Boards");
        click("Save as");
        type("1");
        click("OK");
        assertEquals(1, panelControl.getNumberOfSavedBoards());

        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, panelControl.getNumberOfPanels());

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN); // Opening Board "1"
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        assertEquals(2, panelControl.getNumberOfPanels());

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN); // Deleting Board "1"
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        click("OK");
        assertEquals(0, panelControl.getNumberOfSavedBoards());

        click("View");
        click("Refresh");
        push(KeyboardShortcuts.REFRESH);
        assertEquals(true, modelUpdatedEventTriggered);
        modelUpdatedEventTriggered = false;
    }
}
