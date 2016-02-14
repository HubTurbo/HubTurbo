package unstable;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

import org.junit.Before;
import org.junit.Test;

import guitests.UITest;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import util.Utility;

public class BoardTests extends UITest {

    /**
     * The initial state is one panel with no filter, and no saved boards
     */
    private static void reset() {
        UI ui = TestController.getUI();
        ui.getPanelControl().closeAllPanels();
        ui.getPanelControl().createNewPanelAtStart();
        UI.prefs.clearAllBoards();
        ui.updateTitle();
    }

    @Before
    public void before() {
        PlatformEx.runAndWait(BoardTests::reset);
    }

    @Test
    public void creatingAndClosingPanels() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        press(CLOSE_PANEL);
        waitAndAssertEquals(0, panelControl::getPanelCount);
        press(CREATE_RIGHT_PANEL);
        waitAndAssertEquals(1, panelControl::getPanelCount);
        press(CREATE_LEFT_PANEL);
        waitAndAssertEquals(2, panelControl::getPanelCount);

        clickMenu("Panels", "Create");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        clickMenu("Panels", "Create (Left)");
        waitAndAssertEquals(4, panelControl::getPanelCount);
        clickMenu("Panels", "Close");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        clickMenu("Panels", "Close");
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }

    @Test
    public void saveWhenNoBoardOpen() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // No board is open
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("none"));

        // Saving when no board is open should prompt the user to save a new board
        clickMenu("Boards", "Save");
        type("Board 1");
        push(KeyCode.ESCAPE);
    }

    @Test
    public void boardNotSavedOnCancellation() {
        PanelControl panelControl = TestController.getUI().getPanelControl();

        clickMenu("Boards", "Save");
        type("Board 1");
        push(KeyCode.ESCAPE);

        // No board is saved on cancellation
        waitAndAssertEquals(0, panelControl::getNumberOfSavedBoards);
    }

    @Test
    public void switchWhenNoBoardOpen() {
        // Switching when no board is open should do nothing
        pushKeys(SWITCH_BOARD);
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void boardSaveAs() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        saveBoardWithName("Board 1");

        waitAndAssertEquals(1, panelControl::getNumberOfSavedBoards);
        assertEquals(1, panelControl.getPanelCount());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    private void saveBoardWithName(String name) {
        clickMenu("Boards", "Save as");
        waitUntilNodeAppears("#boardnameinput");
        ((TextField) find("#boardnameinput")).setText(name);
        click("OK");
    }

    @Test
    public void switchingBoardWithOnlyOneSaved() {

        Preferences prefs = UI.prefs;

        saveBoardWithName("Board 1");

        // Nothing happens
        press(SWITCH_BOARD);
        assertTrue(prefs.getLastOpenBoard().isPresent());
        assertEquals("Board 1", prefs.getLastOpenBoard().get());
    }

    @Test
    public void switchingBoardWithManySaved() {

        Preferences prefs = UI.prefs;

        saveBoardWithName("Board 1");
        saveBoardWithName("Board 2");

        assertTrue(prefs.getLastOpenBoard().isPresent());
        assertEquals("Board 2", prefs.getLastOpenBoard().get());

        // Wraps around to the first board
        press(SWITCH_BOARD);
        assertTrue(prefs.getLastOpenBoard().isPresent());
        assertEquals("Board 1", prefs.getLastOpenBoard().get());

        // Back to the second board
        press(SWITCH_BOARD);
        assertTrue(prefs.getLastOpenBoard().isPresent());
        assertEquals("Board 2", prefs.getLastOpenBoard().get());
    }

    @Test
    public void boardNameValidation() {
        tryBoardName("");
        tryBoardName("   ");
        tryBoardName("   none  ");
    }

    private void tryBoardName(String name) {
        clickMenu("Boards", "Save as");
        waitUntilNodeAppears("#boardnameinput");
        ((TextField) find("#boardnameinput")).setText(name);
        assertTrue(find("#boardsavebutton").isDisabled());
        pushKeys(KeyCode.ESCAPE);
        waitUntilNodeDisappears("#boardnameinput");
    }

    private void selectNthMenuItem(int n) {
        for (int i = 0; i < n; i++) {
            push(KeyCode.DOWN);
        }
        push(KeyCode.ENTER);
    }

    @Test
    public void openingBoard() {

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // Board 1 has 1 panel, Board 2 has 2
        saveBoardWithName("Board 1");
        pushKeys(CREATE_RIGHT_PANEL);
        saveBoardWithName("Board 2");

        // We're at Board 2 now
        assertEquals(2, panelControl.getPanelCount());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 2"));

        // This navigates to the second item on the submenu under 'Open',
        // 'Board 1'. We do this because trying to click it will move the
        // mouse over 'Delete' and click the wrong node.
        // There isn't a good solution to this without more specialised
        // ways to move the mouse.
        clickMenu("Boards", "Open");
        selectNthMenuItem(2);

        // We've switched to Board 1
        assertEquals(1, panelControl.getPanelCount());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    @Test
    public void savingBoard() {

        PanelControl panelControl = TestController.getUI().getPanelControl();

        saveBoardWithName("Board 1");
        pushKeys(CREATE_RIGHT_PANEL);

        clickMenu("Boards", "Open");
        selectNthMenuItem(1);

        // Without having saved, we lose the extra panel
        waitAndAssertEquals(1, panelControl::getPanelCount);

        pushKeys(CREATE_RIGHT_PANEL);
        clickMenu("Boards", "Save");
        selectNthMenuItem(1);

        // After saving, the panel is there
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }

    @Test
    public void deleteBoard() {

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        saveBoardWithName("Board 1");

        clickMenu("Boards", "Delete");
        selectNthMenuItem(1);
        click("OK");

        // No board is open now
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("none"));

    }

    @Test
    public void noBoardsOpen() {

        deleteBoard();

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // Switching board has no effect
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());
        pushKeys(SWITCH_BOARD);
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());

        // Saving will prompt the user to save as a new board
        clickMenu("Boards", "Save");
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");

        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    private static String getUiTitleWithOpenBoard(String boardName) {
        String version = Utility.version(UI.VERSION_MAJOR, UI.VERSION_MINOR, UI.VERSION_PATCH);
        return String.format(UI.WINDOW_TITLE, version, boardName);
    }
}
