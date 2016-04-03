package unstable;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.controls.Commons.hasText;
import static ui.components.KeyboardShortcuts.*;

import org.junit.Before;
import org.junit.Test;

import guitests.UITest;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import util.Utility;

public class BoardTests extends UITest {

    private static final String boardNameInputId = IdGenerator.getBoardNameInputFieldIdReference();
    private static final String boardNameSaveButtonId = IdGenerator.getBoardNameSaveButtonIdReference();

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
    public void boards_panelCount_creatingAndClosingPanels() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        press(CLOSE_PANEL);
        waitAndAssertEquals(0, panelControl::getPanelCount);
        press(CREATE_RIGHT_PANEL);
        waitAndAssertEquals(1, panelControl::getPanelCount);
        press(CREATE_LEFT_PANEL);
        waitAndAssertEquals(2, panelControl::getPanelCount);

        traverseMenu("Panels", "Create");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        traverseMenu("Panels", "Create (Left)");
        waitAndAssertEquals(4, panelControl::getPanelCount);
        traverseMenu("Panels", "Close");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        traverseMenu("Panels", "Close");
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }

    @Test
    public void boards_saveDialog_willSaveWhenNoBoardOpen() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // No board is open
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("none"));

        // Saving when no board is open should prompt the user to save a new board
        traverseMenu("Boards", "Save");
        type("Board 1");
        push(KeyCode.ESCAPE);
    }

    @Test
    public void boards_saveDialog_willNotSaveOnCancellation() {
        PanelControl panelControl = TestController.getUI().getPanelControl();

        traverseMenu("Boards", "Save");
        type("Board 1");
        push(KeyCode.ESCAPE);

        // No board is saved on cancellation
        waitAndAssertEquals(0, panelControl::getNumberOfSavedBoards);
    }

    @Test
    public void boards_lastOpenedBoard_switchingWhenNoBoardIsOpen() {
        // Switching when no board is open should do nothing
        pushKeys(SWITCH_BOARD);
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void boards_panelCount_boardsSaveSuccessfully() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        saveBoardWithName("Board 1");

        waitAndAssertEquals(1, panelControl::getNumberOfSavedBoards);
        assertEquals(1, panelControl.getPanelCount());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    private void saveBoardWithName(String name) {
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) find(boardNameInputId)).setText(name);
        click("OK");
    }

    @Test
    public void baords_lastOpenedBoard_cannotSwitchBoardWithOnlyOneSaved() {

        Preferences prefs = UI.prefs;

        saveBoardWithName("Board 1");

        // Nothing happens
        press(SWITCH_BOARD);
        assertTrue(prefs.getLastOpenBoard().isPresent());
        assertEquals("Board 1", prefs.getLastOpenBoard().get());
    }

    @Test
    public void boards_lastOpenedBoard_canSwitchBoardWithOnlyOneSaved() {

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
    public void boards_validation_displaysFeedbackOnFailingValidation() {
        tryBoardName("");
        tryBoardName("   ");
        tryBoardName("   none  ");
    }

    private void tryBoardName(String name) {
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) find(boardNameInputId)).setText(name);
        assertTrue(find(boardNameSaveButtonId).isDisabled());
        pushKeys(KeyCode.ESCAPE);
        waitUntilNodeDisappears(boardNameInputId);
    }

    @Test
    public void boards_panelCount_boardsCanBeOpenedSuccessfully() {

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // Board 1 has 1 panel, Board 2 has 2
        saveBoardWithName("Board 1");
        pushKeys(CREATE_RIGHT_PANEL);
        saveBoardWithName("Board 2");

        // We're at Board 2 now
        waitAndAssertEquals(2, panelControl::getPanelCount);
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 2"));

        traverseMenu("Boards", "Open", "Board 1");

        // We've switched to Board 1
        waitAndAssertEquals(1, panelControl::getPanelCount);
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    @Test
    public void boards_panelCount_boardsCanBeSavedSuccessfully() {

        PanelControl panelControl = TestController.getUI().getPanelControl();

        saveBoardWithName("Board 1");
        pushKeys(CREATE_RIGHT_PANEL);
        awaitCondition(() -> 2 == panelControl.getPanelCount());

        traverseMenu("Boards", "Open", "Board 1");

        // Without having saved, we lose the extra panel
        waitAndAssertEquals(1, panelControl::getPanelCount);

        pushKeys(CREATE_RIGHT_PANEL);
        traverseMenu("Boards", "Save");

        // After saving, the panel is there
        waitAndAssertEquals(2, panelControl::getPanelCount);
    }

    @Test
    public void boards_panelCount_boardsCanBeDeletedSuccessfully() {

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        saveBoardWithName("Board 1");

        traverseMenu("Boards", "Delete", "Board 1");
        waitUntilNodeAppears(hasText("OK"));
        click("OK");

        // No board is open now
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("none"));

    }

    @Test
    public void boards_panelCount_nothingHappensWhenNoBoardIsOpen() {

        boards_panelCount_boardsCanBeDeletedSuccessfully();

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        // Switching board has no effect
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());
        pushKeys(SWITCH_BOARD);
        assertFalse(UI.prefs.getLastOpenBoard().isPresent());

        // Saving will prompt the user to save as a new board
        traverseMenu("Boards", "Save");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) find(boardNameInputId)).setText("Board 1");
        click("OK");

        assertEquals(1, panelControl.getNumberOfSavedBoards());
        assertEquals(ui.getTitle(), getUiTitleWithOpenBoard("Board 1"));
    }

    private static String getUiTitleWithOpenBoard(String boardName) {
        String version = Utility.version(UI.VERSION_MAJOR, UI.VERSION_MINOR, UI.VERSION_PATCH);
        return String.format(UI.WINDOW_TITLE, version, boardName);
    }

    @Test
    public void boards_panelCount_nothingHappensWhenNewBoardIsCancelled() {
        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(1, panelControl.getPanelCount());

        traverseMenu("Boards", "New");
        press(KeyCode.ESCAPE);
        assertEquals(0, panelControl.getNumberOfSavedBoards());
        assertEquals(1, panelControl.getPanelCount());
    }

    @Test
    public void boards_panelCount_newBoardCreated() {

        UI ui = TestController.getUI();
        PanelControl panelControl = ui.getPanelControl();

        traverseMenu("Boards", "New");
        waitUntilNodeAppears(hasText("OK"));
        click("OK");

        waitUntilNodeAppears(boardNameInputId);
        ((TextField) find(boardNameInputId)).setText("empty");
        waitUntilNodeAppears(hasText("OK"));
        click("OK");

        waitAndAssertEquals(0, panelControl::getPanelCount);
        waitAndAssertEquals(ui.getTitle(), () -> getUiTitleWithOpenBoard("empty"));
    }
}
