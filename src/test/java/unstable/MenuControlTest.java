package unstable;

import guitests.UITest;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.junit.Test;

import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.PanelControl;
import prefs.Preferences;
import util.PlatformEx;
import util.events.ModelUpdatedEventHandler;
import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

public class MenuControlTest extends UITest {

    private boolean modelUpdatedEventTriggered;

    @Test
    public void menuControlTest() {
        
        modelUpdatedEventTriggered = false;
        UI.events.registerEvent((ModelUpdatedEventHandler) e -> modelUpdatedEventTriggered = true);
        PanelControl panelControl = (PanelControl) find("#dummy/dummy_col0").getParent();
        Preferences testPref = UI.prefs;
        
        press(CLOSE_PANEL);
        assertEquals(0, panelControl.getNumberOfPanels());
        press(CREATE_RIGHT_PANEL);
        assertEquals(1, panelControl.getNumberOfPanels());
        press(CREATE_LEFT_PANEL);
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
        
        // Testing board save when no board is open because nothing has been saved
        // Expected: prompts user to save as new board
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("Board 1");
        push(KeyCode.ESCAPE);
        PlatformEx.waitOnFxThread();
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        
        // Testing board open keyboard shortcut when no board is saved
        // Expected: nothing happens
        pushKeys(SWITCH_BOARD);
        assertEquals(false, testPref.getLastOpenBoard().isPresent());
        
        // Testing board save as
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(2, panelControl.getNumberOfPanels());
        
        // Testing board open keyboard shortcut when there is only one saved board
        // Expected: nothing happens
        press(SWITCH_BOARD);
        assertEquals(true, testPref.getLastOpenBoard().isPresent());
        assertEquals("Board 1", testPref.getLastOpenBoard().get());

        press(CREATE_RIGHT_PANEL);
        assertEquals(3, panelControl.getNumberOfPanels());
        
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("Board 2");
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        
        // Testing invalid board names
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("");
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("   ");
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());

        press(CLOSE_PANEL);
        press(CLOSE_PANEL);
        press(CLOSE_PANEL);
        assertEquals(0, panelControl.getNumberOfPanels());

        // Testing board open
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER); // Opening Board "2"
        PlatformEx.waitOnFxThread();
        assertEquals(3, panelControl.getNumberOfPanels());

        // Testing First Panel selected
        assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        press(KeyboardShortcuts.JUMP_TO_NTH_ISSUE_KEYS.get(1));
        assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        
        // Testing board open keyboard shortcut
        press(SWITCH_BOARD);
        assertEquals(true, testPref.getLastOpenBoard().isPresent());
        assertEquals("Board 1", testPref.getLastOpenBoard().get());
        
        // Testing board save
        press(CLOSE_PANEL);
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
        
        // Testing board open keyboard shortcut when there are saved boards but none is open
        // Expected: nothing happens
        pushKeys(SWITCH_BOARD);
        assertEquals(false, testPref.getLastOpenBoard().isPresent());
        
        // Testing board save when no board is open because current board was closed
        // Expected: prompts user to save as new board
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");
        PlatformEx.waitOnFxThread();
        assertEquals(2, panelControl.getNumberOfSavedBoards());
        
        click("View");
        click("Refresh");
        push(KeyboardShortcuts.REFRESH.getCode());
        assertEquals(true, modelUpdatedEventTriggered);
        modelUpdatedEventTriggered = false;
    }
}