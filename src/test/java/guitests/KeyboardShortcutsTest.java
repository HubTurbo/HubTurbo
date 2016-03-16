package guitests;

import backend.stub.DummyRepoState;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;

import org.junit.Test;

import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.IssueSelectedEventHandler;
import util.events.PanelClickedEventHandler;
import util.events.testevents.UIComponentFocusEvent;
import util.events.testevents.UIComponentFocusEventHandler;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

import static ui.components.KeyboardShortcuts.SWITCH_DEFAULT_REPO;

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
        assertEquals(false, stage.getWidth() > 500);
        press(MAXIMIZE_WINDOW);
        assertEquals(true, stage.getWidth() > 500);

        // mid-sized window
        press(DEFAULT_SIZE_WINDOW);
        assertEquals(false, stage.getWidth() > 500);

        // jump from panel focus to first issue
        // - This is because on startup focus is on panel and not on filter box
        press(JUMP_TO_FIRST_ISSUE);
        assertEquals(DummyRepoState.noOfDummyIssues, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        press(JUMP_TO_FILTER_BOX);
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        // jump from filter box to first issue
        // - To ensure shortcut works from filter box, too
        press(JUMP_TO_FIRST_ISSUE);
        press(JUMP_TO_FILTER_BOX);
        assertEquals(DummyRepoState.noOfDummyIssues, selectedIssueId);
        clearSelectedIssueId();

        // jump to nth issue using number keys 1-9
        for (int i = 1; i <= 9; i++) {
            push(KeyCode.ESCAPE);
            press(JUMP_TO_NTH_ISSUE_KEYS.get(i));
            PlatformEx.waitOnFxThread();
            assertEquals(DummyRepoState.noOfDummyIssues - (i - 1), selectedIssueId);
            clearSelectedIssueId();
            if (i == 1) {
                press(JUMP_TO_FILTER_BOX);
                assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
                clearUiComponentFocusEventType();
            }
        }

        // jump to last issue
        push(KeyCode.END);
        assertEquals(1, selectedIssueId);
        clearSelectedIssueId();

        // jump to first issue
        push(KeyCode.HOME);
        sleep(1000);
        assertEquals(DummyRepoState.noOfDummyIssues, selectedIssueId);
        clearSelectedIssueId();

        push(getKeyCode("DOWN_ISSUE"));
        assertEquals(DummyRepoState.noOfDummyIssues - 1, selectedIssueId);
        clearSelectedIssueId();
        push(getKeyCode("DOWN_ISSUE"));
        assertEquals(DummyRepoState.noOfDummyIssues - 2, selectedIssueId);
        clearSelectedIssueId();
        push(getKeyCode("UP_ISSUE"));
        assertEquals(DummyRepoState.noOfDummyIssues - 1, selectedIssueId);
        clearSelectedIssueId();

        press(CREATE_RIGHT_PANEL);
        press(JUMP_TO_FIRST_ISSUE);

        push(getKeyCode("RIGHT_PANEL"));
        assertEquals(0, panelIndex);
        clearPanelIndex();
        push(getKeyCode("LEFT_PANEL"));
        assertEquals(1, panelIndex);
        clearPanelIndex();
        push(getKeyCode("RIGHT_PANEL"));
        assertEquals(0, panelIndex);
        clearPanelIndex();
        push(getKeyCode("LEFT_PANEL"));
        assertEquals(1, panelIndex);
        clearPanelIndex();

        // remove focus from repo selector
        ComboBox<String> repoSelectorComboBox = find("#repositorySelector");
        click(repoSelectorComboBox);
        assertEquals(true, repoSelectorComboBox.isFocused());
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        assertEquals(false, repoSelectorComboBox.isFocused());
        clearUiComponentFocusEventType();

        // switch default repo tests
        assertEquals(1, repoSelectorComboBox.getItems().size());
        // setup - add a new repo
        click(repoSelectorComboBox);
        selectAll();
        type("dummy1/dummy1");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(2, repoSelectorComboBox.getItems().size());
        assertEquals(repoSelectorComboBox.getValue(), "dummy1/dummy1");
        // test shortcut on repo dropdown
        doubleClick(repoSelectorComboBox);
        pushKeys(SWITCH_DEFAULT_REPO);
        // wait for issue 9 to appear then click on it
        // issue 9 is chosen instead of issue 10
        // as there is a problem with finding issue 10's node due to it being the first card in the panel
        waitUntilNodeAppears("#dummy/dummy_col1_" + (DummyRepoState.noOfDummyIssues - 1));
        assertEquals(repoSelectorComboBox.getValue(), "dummy/dummy");
        // test shortcut when focus is on panel
        click("#dummy/dummy_col1_" + (DummyRepoState.noOfDummyIssues - 1));
        press(SWITCH_DEFAULT_REPO);
        PlatformEx.waitOnFxThread();
        assertEquals(repoSelectorComboBox.getValue(), "dummy1/dummy1");
        // test shortcut when focus is on issue list
        press(JUMP_TO_NTH_ISSUE_KEYS.get(1));
        press(SWITCH_DEFAULT_REPO);
        PlatformEx.waitOnFxThread();
        assertEquals(repoSelectorComboBox.getValue(), "dummy/dummy");

        // mark as read
        ListPanel issuePanel = find("#dummy/dummy_col1");
        // mark as read an issue that has another issue below it
        push(KeyCode.HOME);
        // focus should change to the issue below
        int issueIdBeforeMark = selectedIssueId;
        int issueIdExpected = issueIdBeforeMark - 1;
        push(getKeyCode("MARK_AS_READ"));
        PlatformEx.waitOnFxThread();
        assertEquals(issueIdExpected, selectedIssueId);
        push(getKeyCode("UP_ISSUE")); // required since focus has changed to next issue
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(true, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());

        // mark as read an issue at the bottom
        push(KeyCode.END);
        push(getKeyCode("MARK_AS_READ"));
        // focus should remain at bottom issue
        assertEquals(1, selectedIssueId);
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(true, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());

        // mark as unread
        push(getKeyCode("MARK_AS_UNREAD"));
        assertEquals(true, issuePanel.getSelectedElement().isPresent());
        assertEquals(false, issuePanel.getSelectedElement().get().getIssue().isCurrentlyRead());
        clearSelectedIssueId();

        // testing corner case for mark as read where there is only one issue displayed
        click("#dummy/dummy_col1_filterTextField");
        type("id:5");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        press(JUMP_TO_FIRST_ISSUE);
        push(getKeyCode("MARK_AS_READ"));
        // focus should remain at the only issue shown
        assertEquals(5, selectedIssueId);

        // minimize window
        press(MINIMIZE_WINDOW);
        assertEquals(true, stage.isIconified());

    }

    public KeyCode getKeyCode(String shortcut) {
        return KeyCode.getKeyCode(KeyboardShortcuts.getDefaultKeyboardShortcuts().get(shortcut));
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
