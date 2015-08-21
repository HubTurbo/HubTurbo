package guitests;

import javafx.scene.control.TextField;
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

        assertEquals(0, panelControl.getNumberOfSavedBoards());
        
        // Testing board save as
        click("Boards");
        click("Save as");
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");
        sleep(1000);
        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(2, panelControl.getNumberOfPanels());

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        assertEquals(3, panelControl.getNumberOfPanels());
        
        click("Boards");
        click("Save as");
        ((TextField) find("#boardnameinput")).setText("Board 2");
        click("OK");
        sleep(1000);
        assertEquals(2, panelControl.getNumberOfSavedBoards());

        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, panelControl.getNumberOfPanels());
        
        // Testing board open
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER); // Opening Board "1"
        sleep(1000);
        assertEquals(2, panelControl.getNumberOfPanels());
        
        // Testing board save
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        click("Boards");
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);
        sleep(1000);
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        
        // Testing board delete
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER); // Deleting current board (Board 1): no board is set as open
        click("OK");
        sleep(1000);
        assertEquals(1, panelControl.getNumberOfSavedBoards());
        
        // Testing board save when no board is open
        // Expected: prompts user to save as new board
        click("Boards");
        click("Save");
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");
        sleep(1000);
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        
        click("View");
        click("Refresh");
        push(KeyboardShortcuts.REFRESH);
        assertEquals(true, modelUpdatedEventTriggered);
        modelUpdatedEventTriggered = false;
    }
}
