package guitests;

import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;

import org.junit.Test;

import ui.UI;
import ui.TestController;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.PanelControl;
import prefs.Preferences;
import util.PlatformEx;
import util.events.ModelUpdatedEventHandler;
import util.Utility;
import static org.junit.Assert.assertEquals;

public class MenuControlTest extends UITest {

    private boolean modelUpdatedEventTriggered;

    @Test
    public void menuControlTest() {
        
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();
        Preferences testPref = UI.prefs;
        
        String uiTitle = ("HubTurbo "
                + Utility.version(ui.getVersionMajor(), ui.getVersionMinor(), ui.getVersionPatch())
                + " (%s)");
        modelUpdatedEventTriggered = false;
        UI.events.registerEvent((ModelUpdatedEventHandler) e -> modelUpdatedEventTriggered = true);
        
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
        
        assertEquals(ui.getTitle(), String.format(uiTitle, "none"));
        
        // Testing board save when no board is open because nothing has been saved
        // Expected: prompts user to save as new board
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.ENTER);
        type("Board 1");
        push(KeyCode.ESCAPE);
        PlatformEx.waitOnFxThread();
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        
        // Testing board open keyboard shortcut when no board is saved
        // Expected: nothing happens
        press(KeyCode.CONTROL).press(KeyCode.B).release(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(false, testPref.getLastOpenBoard().isPresent());
        
        // Testing board save as
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        type("Board 1");
        click("Save");
        PlatformEx.waitOnFxThread();
        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(2, panelControl.getNumberOfPanels());
        assertEquals(ui.getTitle(), String.format(uiTitle, "Board 1"));
        
        // Testing board switch keyboard shortcut when there is only one saved board
        // Expected: nothing happens
        press(KeyCode.CONTROL).press(KeyCode.B).release(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(true, testPref.getLastOpenBoard().isPresent());
        assertEquals("Board 1", testPref.getLastOpenBoard().get());

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        assertEquals(3, panelControl.getNumberOfPanels());
        
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        type("Board 2");
        click("Save");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), String.format(uiTitle, "Board 2"));
        
        // Testing invalid board names
        // Expected: save button disabled
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        push(KeyCode.BACK_SPACE);
        Button saveButton1 = (Button) find("Save");
        assertEquals(true, saveButton1.isDisabled());
        push(KeyCode.ESCAPE);

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        type("   ");
        Button saveButton2 = (Button) find("Save");
        assertEquals(true, saveButton2.isDisabled());
        click("Cancel");

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        type(" none   ");
        Button saveButton3 = (Button) find("Save");
        assertEquals(true, saveButton3.isDisabled());
        push(KeyCode.ESCAPE);

        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, panelControl.getNumberOfPanels());
        
        // Testing board open
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER); // Opening Board "2"
        PlatformEx.waitOnFxThread();
        assertEquals(3, panelControl.getNumberOfPanels());
        assertEquals(ui.getTitle(), String.format(uiTitle, "Board 2"));

        // Testing First Panel selected
        assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        press(KeyCode.CONTROL).press(KeyCode.DIGIT1).release(KeyCode.DIGIT1).release(KeyCode.CONTROL);
        assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        
        // Testing board open keyboard shortcut
        press(KeyCode.CONTROL).press(KeyCode.B).release(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(true, testPref.getLastOpenBoard().isPresent());
        assertEquals("Board 1", testPref.getLastOpenBoard().get());
        assertEquals(ui.getTitle(), String.format(uiTitle, "Board 1"));
        
        // Testing board save
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        click("Boards");
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        
        // Testing board delete
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER); // Deleting current board (Board 1): no board is set as open
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), String.format(uiTitle, "none"));
        
        // Testing board open keyboard shortcut when there are saved boards but none is open
        // Expected: nothing happens
        press(KeyCode.CONTROL).press(KeyCode.B).release(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(false, testPref.getLastOpenBoard().isPresent());
        
        // Testing board save when no board is open because current board was closed
        // Expected: prompts user to save as new board
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.ENTER);
        type("Board 1");
        click("Save");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), String.format(uiTitle, "Board 1"));
        
        click("View");
        click("Refresh");
        push(KeyboardShortcuts.REFRESH.getCode());
        assertEquals(true, modelUpdatedEventTriggered);
        modelUpdatedEventTriggered = false;
    }
}
