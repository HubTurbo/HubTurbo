package guitests;


import org.junit.Before;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
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
        type("board 1");
        click("OK");
        assertEquals(1, panelControl.getPanelCount());

        // Create a new panel, then save with the same name
        // Expected: Dialog shown to confirm duplicate name
        clickMenu("Panels", "Create");
        clickMenu("Boards", "Save as");
        type("board 1");
        click("OK");
        waitUntilNodeAppears(hasText("A board by the name 'board 1' already exists."));

        // Overwrite previous board, then open the board again
        // Expected: the board should contain 2 panels
        click("Yes");
        clickMenu("Boards", "Open", "board 1");
        assertEquals(2, panelControl.getPanelCount());


        // Create a new panel, then save with the same name to show warning dialog,
        // don't save and try to reopen board
        // Expected: Board is not overwritten, should contain 2 panels
        clickMenu("Panels", "Create");
        clickMenu("Boards", "Save as");
        type("board 1");
        click("OK");
        click("No");
        click("Cancel");
        clickMenu("Boards", "Open", "board 1");
        assertEquals(2, panelControl.getPanelCount());
    }
}
