package guitests;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

import org.junit.Test;

import backend.stub.DummyRepoState;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import ui.IdGenerator;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.IssueSelectedEventHandler;
import util.events.PanelClickedEventHandler;
import util.events.testevents.UIComponentFocusEvent;
import util.events.testevents.UIComponentFocusEventHandler;

public class KeyboardShortcutsTest extends UITest {

    private UIComponentFocusEvent.EventType uiComponentFocusEventType;
    private int selectedIssueId;
    private int panelIndex;

    @Test
    public void keyboardShortcutsTest() {
        UI.events.registerEvent((IssueSelectedEventHandler) e -> selectedIssueId = e.id);
        UI.events.registerEvent((UIComponentFocusEventHandler) e -> uiComponentFocusEventType = e.eventType);
        UI.events.registerEvent((PanelClickedEventHandler) e -> panelIndex = e.panelIndex);
        clearSelectedIssueId();
        clearUiComponentFocusEventType();
        clearPanelIndex();

        // maximize
        assertEquals(false, getStage().getWidth() > 500);
        press(MAXIMIZE_WINDOW);
        assertEquals(true, getStage().getWidth() > 500);

        // mid-sized window
        press(DEFAULT_SIZE_WINDOW);
        assertEquals(false, getStage().getWidth() > 500);

        // jump from panel focus to first issue
        // - This is because on startup focus is on panel and not on filter box
        press(JUMP_TO_FIRST_ISSUE);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        press(JUMP_TO_FILTER_BOX);
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        // jump from filter box to first issue
        // - To ensure shortcut works from filter box, too
        press(JUMP_TO_FIRST_ISSUE);
        press(JUMP_TO_FILTER_BOX);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, selectedIssueId);
        clearSelectedIssueId();

        // jump to nth issue using number keys 1-9
        for (int i = 1; i <= 9; i++) {
            push(KeyCode.ESCAPE);
            press(JUMP_TO_NTH_ISSUE_KEYS.get(i));
            PlatformEx.waitOnFxThread();
            assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - (i - 1), selectedIssueId);
            clearSelectedIssueId();
            if (i == 1) {
                press(JUMP_TO_FILTER_BOX);
                assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
                clearUiComponentFocusEventType();
            }
        }

        // jump to last issue
        push(LAST_ISSUE);
        assertEquals(1, selectedIssueId);
        clearSelectedIssueId();

        // jump to first issue
        push(FIRST_ISSUE);
        sleep(1000);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, selectedIssueId);
        clearSelectedIssueId();

        push(DOWN_ISSUE);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 1, selectedIssueId);
        clearSelectedIssueId();
        push(DOWN_ISSUE);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 2, selectedIssueId);
        clearSelectedIssueId();
        push(UP_ISSUE);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 1, selectedIssueId);
        clearSelectedIssueId();

        press(CREATE_RIGHT_PANEL);
        press(JUMP_TO_FIRST_ISSUE);

        push(RIGHT_PANEL);
        assertEquals(0, panelIndex);
        clearPanelIndex();
        push(LEFT_PANEL);
        assertEquals(1, panelIndex);
        clearPanelIndex();
        push(RIGHT_PANEL);
        assertEquals(0, panelIndex);
        clearPanelIndex();
        push(LEFT_PANEL);
        assertEquals(1, panelIndex);
        clearPanelIndex();

        // remove focus from repo selector
        ComboBox<String> repoSelectorComboBox = getRepositorySelector();
        clickRepositorySelector();
        assertEquals(true, repoSelectorComboBox.isFocused());
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        assertEquals(false, repoSelectorComboBox.isFocused());
        clearUiComponentFocusEventType();

        press(SHOW_REPO_PICKER);
        waitUntilNodeAppears("#repositoryPickerUserInputField");
        press(KeyCode.ESCAPE);

        // mark as read
        ListPanel issuePanel = getPanel(1);
        // mark as read an issue that has another issue below it
        push(FIRST_ISSUE);
        // focus should change to the issue below
        clickOn(IdGenerator.getPanelCellIdReference(1, 11));
        int issueIdBeforeMark = selectedIssueId;
        int issueIdExpected = issueIdBeforeMark - 1;
        push(MARK_AS_READ);
        PlatformEx.waitOnFxThread();
        assertEquals(issueIdExpected, selectedIssueId);
        push(UP_ISSUE); // required since focus has changed to next issue
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(true, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());

        // mark as read an issue at the bottom
        push(LAST_ISSUE);
        push(MARK_AS_READ);
        // focus should remain at bottom issue
        assertEquals(1, selectedIssueId);
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(true, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());

        // mark as unread
        push(MARK_AS_UNREAD);
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(false, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());
        clearSelectedIssueId();

        // close issue
        push(CLOSE_ISSUE);
        push(KeyCode.ENTER);
        waitUntilNodeAppears("Undo");
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(false, issuePanel.getSelectedElement().get().getIssue().isOpen());
        clearSelectedIssueId();

        // reopen issue
        push(REOPEN_ISSUE);
        push(KeyCode.ENTER);
        waitUntilNodeAppears("Undo");
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(true, issuePanel.getSelectedElement().get().getIssue().isOpen());
        clearSelectedIssueId();

        // testing corner case for mark as read where there is only one issue displayed
        clickFilterTextFieldAtPanel(1);
        type("id:5");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        press(JUMP_TO_FIRST_ISSUE);
        push(MARK_AS_READ);
        // focus should remain at the only issue shown
        assertEquals(5, selectedIssueId);

        // minimize window
        press(MINIMIZE_WINDOW);
        assertEquals(true, getStage().isIconified());

    }

    public void clearSelectedIssueId() {
        selectedIssueId = 0;
    }

    public void clearPanelIndex() {
        panelIndex = -1;
    }

    public void clearUiComponentFocusEventType() {
        uiComponentFocusEventType = UIComponentFocusEvent.EventType.NONE;
    }
}
