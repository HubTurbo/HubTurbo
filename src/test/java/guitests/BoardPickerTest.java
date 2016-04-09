package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;


import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.SHOW_BOARD_PICKER;

public class BoardPickerTest extends UITest {

    private static final String boardNameInputId = IdGenerator.getBoardNameInputFieldIdReference();
    private static final String boardNameSaveButtonId = IdGenerator.getBoardNameSaveButtonIdReference();

    @Test
    public void testBoardPicker() {
        Preferences prefs = TestController.getUI().prefs;

        VBox suggestedBoardList;

        // Create some boards
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) GuiTest.find(boardNameInputId)).setText("Board 1");
        clickOn(boardNameSaveButtonId);
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) GuiTest.find(boardNameInputId)).setText("Board 2");
        clickOn(boardNameSaveButtonId);
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) GuiTest.find(boardNameInputId)).setText("Board 3");
        clickOn(boardNameSaveButtonId);
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) GuiTest.find(boardNameInputId)).setText("Dummy Board 1");
        clickOn(boardNameSaveButtonId);
        traverseMenu("Boards", "Save as");
        waitUntilNodeAppears(boardNameInputId);
        ((TextField) GuiTest.find(boardNameInputId)).setText("Dummy Board 2");
        clickOn(boardNameSaveButtonId);

        // Should be able to match an exact word
        pushKeys(SHOW_BOARD_PICKER);
        PlatformEx.waitOnFxThread();
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(5, suggestedBoardList.getChildren().size());
        clickOn(IdGenerator.getBoardPickerTextFieldReference());
        type("Board");
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(5, suggestedBoardList.getChildren().size());
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals("Board 1", prefs.getLastOpenBoard().get());

        // Should be able to match single-letter prefixes
        pushKeys(SHOW_BOARD_PICKER);
        PlatformEx.waitOnFxThread();
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(5, suggestedBoardList.getChildren().size());
        clickOn(IdGenerator.getBoardPickerTextFieldReference());
        type("d b");
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(2, suggestedBoardList.getChildren().size());
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals("Dummy Board 1", prefs.getLastOpenBoard().get());

        // Should be able to match multiple-letter prefixes
        pushKeys(SHOW_BOARD_PICKER);
        PlatformEx.waitOnFxThread();
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(5, suggestedBoardList.getChildren().size());
        clickOn(IdGenerator.getBoardPickerTextFieldReference());
        type("boa dum 2");
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(1, suggestedBoardList.getChildren().size());
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals("Dummy Board 2", prefs.getLastOpenBoard().get());

        // Should be able to match a fully exact match
        pushKeys(SHOW_BOARD_PICKER);
        PlatformEx.waitOnFxThread();
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(5, suggestedBoardList.getChildren().size());
        clickOn("Dummy Board 1");
        suggestedBoardList = findOrWaitFor(IdGenerator.getBoardPickerSuggestedBoardListReference());
        assertEquals(1, suggestedBoardList.getChildren().size());
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals("Dummy Board 1", prefs.getLastOpenBoard().get());
    }
}
