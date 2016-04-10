package unstable;

import static org.loadui.testfx.controls.Commons.hasText;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import guitests.UITest;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;

public class BoardDuplicateTests extends UITest {
    PanelControl panelControl;

    @Before
    public void setupUIComponent() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();

        Preferences testPref = UI.prefs;

        List<String> boardNames = testPref.getAllBoardNames();
        boardNames.stream().forEach(testPref::removeBoard);
    }

    @Test
    public void duplicateNameTest() {
        // Save the current board
        traverseHubTurboMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        // Use the default 'New Board' as board name
        // Workaround since we are unable to get text field into focus on Travis
        clickOn("OK");
        waitAndAssertEquals(1, panelControl::getPanelCount);

        // Create a new panel, then save with the same name
        // Expected: Dialog shown to confirm duplicate name
        traverseHubTurboMenu("Panels", "Create");
        traverseHubTurboMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        // Sometimes not trigger if only click once
        doubleClickOn("OK");
        waitUntilNodeAppears(hasText("A board by the name 'New Board' already exists."));

        // Overwrite previous board, then open the board again
        // Expected: the board should contain 2 panels
        clickOn("Yes");
        traverseHubTurboMenu("Boards", "Open", "New Board");
        waitAndAssertEquals(2, panelControl::getPanelCount);


        // Create a new panel, then save with the same name to show warning dialog,
        // don't save and try to reopen board
        // Expected: Board is not overwritten, should contain 2 panels
        traverseHubTurboMenu("Panels", "Create");
        traverseHubTurboMenu("Boards", "Save as");
        waitUntilNodeAppears(hasText("OK"));
        clickOn("OK");
        clickOn("No");
        clickOn("Cancel");
        traverseHubTurboMenu("Boards", "Open", "New Board");
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }
}
