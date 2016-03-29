package guitests;

import org.junit.Before;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;

import java.util.List;

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
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        // Use the default 'New Board' as board name
        // Workaround since we are unable to get text field into focus on Travis
        click("OK");
        waitAndAssertEquals(1, panelControl::getPanelCount);

        // Create a new panel, then save with the same name
        // Expected: Dialog shown to confirm duplicate name
        traverseMenu("Panels", "Create");
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        click("OK");
        waitUntilNodeAppears(hasText("A board by the name 'New Board' already exists."));

        // Overwrite previous board, then open the board again
        // Expected: the board should contain 2 panels
        click("Yes");
        traverseMenu("Boards", "Open", "New Board");
        waitAndAssertEquals(2, panelControl::getPanelCount);


        // Create a new panel, then save with the same name to show warning dialog,
        // don't save and try to reopen board
        // Expected: Board is not overwritten, should contain 2 panels
        traverseMenu("Panels", "Create");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        click("OK");
        click("No");
        click("Cancel");
        traverseMenu("Boards", "Open", "New Board");
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }
}
