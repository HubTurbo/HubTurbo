package guitests;


import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class BoardTests extends UITest {
    PanelControl panelControl;
    @Before
    public void setup() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();

        Preferences testPref = UI.prefs;

        List<String> boardNames = testPref.getAllBoardNames();
        boardNames.stream().forEach(testPref::removeBoard);
    }

    @Test
    public void duplicateNameTest() {
        // Save the current board
        clickMenu("Boards", "Save as");
        // Use the default 'New Board' as board name
        // Workaround since we are unable to get text field into focus on Travis
        click("OK");
        assertEquals(1, panelControl.getPanelCount());

        // Create a new panel, then save with the same name
        // Expected: Dialog shown to confirm duplicate name
        clickMenu("Panels", "Create");
        clickMenu("Boards", "Save as");
        click("OK");
        waitUntilNodeAppears(hasText("A board by the name 'New Board' already exists."));

        // Overwrite previous board, then open the board again
        // Expected: the board should contain 2 panels
        click("Yes");
        clickMenu("Boards", "Open", "New Board");
        assertEquals(2, panelControl.getPanelCount());


        // Create a new panel, then save with the same name to show warning dialog,
        // don't save and try to reopen board
        // Expected: Board is not overwritten, should contain 2 panels
        clickMenu("Panels", "Create");
        assertEquals(3, panelControl.getPanelCount());
        clickMenu("Boards", "Save as");
        click("OK");
        click("No");
        click("Cancel");
        clickMenu("Boards", "Open", "New Board");
        assertEquals(2, panelControl.getPanelCount());
    }
}
