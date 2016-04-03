package guitests;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.issuepanel.PanelControl;

public class PanelFocusTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Override
    public void beforeStageStarts() {
        createDefaultPanels();
    }

    @Test
    public void panelFocusOnActionsTest() throws IllegalAccessException {

        PanelControl panelControl = TestController.getUI().getPanelControl();

        panelFocus_focusedPanel_focusCorrectOnStartup(panelControl);
        panelFocus_focusedPanel_focusCorrectOnCreatingPanels(panelControl);
        panelFocus_firstPanel_firstPanelShown(panelControl);

    }

    private void panelFocus_focusedPanel_focusCorrectOnStartup(PanelControl panelControl) {
        /**
         * Testing Panel Focus on Startup
         * ==============================
         * Only doing test for multiple panel at the start (not testing cases of
         * 1 recent panel or 0 recent panel) since it is only possible to launch
         * the application once for one test.
         * Having tests with multiple start ups will require each start up case
         * to be in its own test file.
         */
        // Check that there are multiple panels on startup (for precaution)
        assertEquals(3, panelControl.getPanelCount());

        // check that focus is on first panel
        awaitCondition(() ->
            0 == panelControl.getCurrentlySelectedPanel().get());

        // Check that pressing F will go to second panel
        // This checks that no filter text field is in focus and panel
        // shortcut works at startup
        pushKeys(KeyCode.F);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get());

        // More shortcut checks to ensure the focus is always correct
        pushKeys(JUMP_TO_FILTER_BOX);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get());
        pushKeys(JUMP_TO_FIRST_ISSUE);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get());
        pushKeys(KeyCode.F);
        awaitCondition(() ->
            2 == panelControl.getCurrentlySelectedPanel().get());
    }

    private void panelFocus_focusedPanel_focusCorrectOnCreatingPanels(PanelControl panelControl) {
        /**
         * Testing Panel Focus on Creating Panels
         * ======================================
         */
        // test that upon creating panel on the right, focus is on the last panel
        // - this includes testing double space as the last panel might be
        //   colour focused but the real JavaFX focus is on first panel
        pushKeys(CREATE_RIGHT_PANEL);
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get() ==
            panelControl.getPanelCount() - 1);

        type("  ");
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get() ==
            panelControl.getPanelCount() - 1);

        // test that upon creating panel on the left, focus is on the first panel
        // - same consideration as above
        pushKeys(CREATE_LEFT_PANEL);
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get());
        type("  ");
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get());
    }

    private void panelFocus_firstPanel_firstPanelShown(PanelControl panelControl)
        throws IllegalAccessException {
        /**
         * Testing First Panel is shown (i.e. scrollbar is set to left end)
         * and on focus upon Opening Board
         * ================================================================
         */
        // Setup:
        // 1. Save a board
        click("Boards");
        pushKeys(KeyCode.DOWN);
        pushKeys(KeyCode.DOWN);
        pushKeys(KeyCode.ENTER);
        ((TextField) find(IdGenerator.getBoardNameInputFieldIdReference())).setText("Board 1");
        click("OK");
        awaitCondition(() -> 1 == panelControl.getNumberOfSavedBoards());
        // 2. Create a new panel so that scroll bar is on the left
        pushKeys(CREATE_RIGHT_PANEL);
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get() ==
            panelControl.getPanelCount() - 1);
        // 3. Open board
        pushKeys(SWITCH_BOARD);

        // Check that first panel is on focus
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get());
        // Check that first panel is shown by checking scrollbar position
        ScrollPane panelsScrollPaneReflection =
            (ScrollPane) FieldUtils.readField(panelControl, "panelsScrollPane", true);
        assertEquals(0, panelsScrollPaneReflection.getHvalue(), 0.001);
    }

    private void createDefaultPanels() {
        Preferences prefs = TestController.createTestPreferences();

        PanelInfo test1 = new PanelInfo();
        PanelInfo test2 = new PanelInfo();
        PanelInfo test3 = new PanelInfo();
        List<PanelInfo> panels = new ArrayList<>();
        panels.add(test1);
        panels.add(test2);
        panels.add(test3);

        prefs.setPanelInfo(panels);
    }
}
