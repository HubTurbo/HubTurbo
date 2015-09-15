package guitests;

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
import static ui.components.KeyboardShortcuts.DOUBLE_PRESS;

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
        assertEquals(false, stage.getMinWidth() > 500);
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        assertEquals(true, stage.getMinWidth() > 500);

        // mid-sized window
        press(KeyCode.CONTROL).press(KeyCode.D).release(KeyCode.D).release(KeyCode.CONTROL);
        assertEquals(false, stage.getMinWidth() > 500);

        // jump from filter box to first issue
        press(KeyCode.CONTROL).press(KeyCode.DOWN).release(KeyCode.DOWN).release(KeyCode.CONTROL);
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        press(KeyCode.CONTROL).press(KeyCode.UP).release(KeyCode.UP).release(KeyCode.CONTROL);
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        // jump from filter box to first issue
        push(DOUBLE_PRESS.getCode()).push(DOUBLE_PRESS.getCode());
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        push(DOUBLE_PRESS.getCode()).push(DOUBLE_PRESS.getCode());
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        // jump to first issue using number key(1) or ENTER
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT1).release(KeyCode.DIGIT1).release(KeyCode.CONTROL);
        PlatformEx.waitOnFxThread();
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();
        push(DOUBLE_PRESS.getCode()).push(DOUBLE_PRESS.getCode());
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT2).release(KeyCode.CONTROL).release(KeyCode.DIGIT2);
        PlatformEx.waitOnFxThread();
        assertEquals(9, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT3).release(KeyCode.CONTROL).release(KeyCode.DIGIT3);
        PlatformEx.waitOnFxThread();
        assertEquals(8, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT4).release(KeyCode.CONTROL).release(KeyCode.DIGIT4);
        PlatformEx.waitOnFxThread();
        assertEquals(7, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT5).release(KeyCode.CONTROL).release(KeyCode.DIGIT5);
        PlatformEx.waitOnFxThread();
        assertEquals(6, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT6).release(KeyCode.CONTROL).release(KeyCode.DIGIT6);
        PlatformEx.waitOnFxThread();
        assertEquals(5, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT7).release(KeyCode.CONTROL).release(KeyCode.DIGIT7);
        PlatformEx.waitOnFxThread();
        assertEquals(4, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT8).release(KeyCode.CONTROL).release(KeyCode.DIGIT8);
        PlatformEx.waitOnFxThread();
        assertEquals(3, selectedIssueId);
        clearSelectedIssueId();
        push(KeyCode.ESCAPE);
        press(KeyCode.CONTROL).press(KeyCode.DIGIT9).release(KeyCode.CONTROL).release(KeyCode.DIGIT9);
        PlatformEx.waitOnFxThread();
        assertEquals(2, selectedIssueId);
        clearSelectedIssueId();

        // jump to last issue
        push(KeyCode.END);
        assertEquals(1, selectedIssueId);
        clearSelectedIssueId();

        // jump to first issue
        push(KeyCode.HOME);
        sleep(1000);
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();

        push(getKeyCode("DOWN_ISSUE"));
        assertEquals(9, selectedIssueId);
        clearSelectedIssueId();
        push(getKeyCode("DOWN_ISSUE"));
        assertEquals(8, selectedIssueId);
        clearSelectedIssueId();
        push(getKeyCode("UP_ISSUE"));
        assertEquals(9, selectedIssueId);
        clearSelectedIssueId();

        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        push(DOUBLE_PRESS.getCode()).push(DOUBLE_PRESS.getCode());

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
        ComboBox<String> comboBox = find("#repositorySelector");
        doubleClick(comboBox);
        assertEquals(true, comboBox.isFocused());
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        assertEquals(false, comboBox.isFocused());
        clearUiComponentFocusEventType();
        
        click("#dummy/dummy_col1_1");

        // mark as read
        ListPanel issuePanel = find("#dummy/dummy_col1");
        // mark as read an issue that has another issue below it
        push(KeyCode.HOME);
        // focus should change to the issue below
        int issueIdBeforeMark = selectedIssueId;
        int issueIdExpected = issueIdBeforeMark - 1;
        push(getKeyCode("MARK_AS_READ"));
        assertEquals(issueIdExpected, selectedIssueId);
        push(getKeyCode("UP_ISSUE")); // required since focus has changed to next issue
        assertEquals(true, issuePanel.getSelectedIssue().isCurrentlyRead());
        
        // mark as read an issue at the bottom
        push(KeyCode.END);
        push(getKeyCode("MARK_AS_READ"));
        // focus should remain at bottom issue
        assertEquals(1, selectedIssueId);
        assertEquals(true, issuePanel.getSelectedIssue().isCurrentlyRead());
        
        // mark as unread
        push(getKeyCode("MARK_AS_UNREAD"));
        assertEquals(false, issuePanel.getSelectedIssue().isCurrentlyRead());
        clearSelectedIssueId();

        // testing corner case for mark as read where there is only one issue displayed
        click("#dummy/dummy_col1_filterTextField");
        type("id");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("5");
        push(KeyCode.ENTER);
        push(KeyCode.SPACE).push(KeyCode.SPACE);
        push(getKeyCode("MARK_AS_READ"));
        // focus should remain at the only issue shown
        assertEquals(5, selectedIssueId);
        
        // minimize window
        press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL); // run this last
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
