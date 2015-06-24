package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.IssuePanel;
import util.events.ColumnClickedEventHandler;
import util.events.IssueSelectedEventHandler;
import util.events.testevents.UIComponentFocusEvent;
import util.events.testevents.UIComponentFocusEventHandler;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.DOUBLE_PRESS;

public class KeyboardShortcutsTest extends UITest {

    private UIComponentFocusEvent.EventType uiComponentFocusEventType;
    private int selectedIssueId;
    private int columnIndex;

    @Test
    public void keyboardShortcutsTest() {
        UI.events.registerEvent((IssueSelectedEventHandler) e -> selectedIssueId = e.id);
        UI.events.registerEvent((UIComponentFocusEventHandler) e -> uiComponentFocusEventType = e.eventType);
        UI.events.registerEvent((ColumnClickedEventHandler) e -> columnIndex = e.columnIndex);
        clearSelectedIssueId();
        clearUiComponentFocusEventType();
        clearColumnIndex();

        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        sleep(1000);
        assertEquals(true, stage.isMaximized());

        // mid-sized window
        press(KeyCode.CONTROL).press(KeyCode.D).release(KeyCode.D).release(KeyCode.CONTROL);
        sleep(1000);
        assertEquals(false, stage.isMaximized());

        // jump from filter box to first issue
        press(KeyCode.CONTROL).press(KeyCode.DOWN).release(KeyCode.DOWN).release(KeyCode.CONTROL);
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        press(KeyCode.CONTROL).press(KeyCode.UP).release(KeyCode.UP).release(KeyCode.CONTROL);
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        // jump from filter box to first issue
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);
        assertEquals(10, selectedIssueId);
        clearSelectedIssueId();

        // jump from issue list to filter box
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);
        assertEquals(UIComponentFocusEvent.EventType.FILTER_BOX, uiComponentFocusEventType);
        clearUiComponentFocusEventType();

        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

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
        push(DOUBLE_PRESS).push(DOUBLE_PRESS);

        push(getKeyCode("RIGHT_PANEL"));
        assertEquals(0, columnIndex);
        clearColumnIndex();
        push(getKeyCode("LEFT_PANEL"));
        assertEquals(1, columnIndex);
        clearColumnIndex();
        push(getKeyCode("RIGHT_PANEL"));
        assertEquals(0, columnIndex);
        clearColumnIndex();
        push(getKeyCode("LEFT_PANEL"));
        assertEquals(1, columnIndex);
        clearColumnIndex();

        click("#dummy/dummy_col1_1");

        // mark as read/unread
        IssuePanel issuePanel = find("#dummy/dummy_col1");
        push(getKeyCode("MARK_AS_READ"));
        assertEquals(true, issuePanel.getSelectedIssue().isCurrentlyRead());
        push(getKeyCode("MARK_AS_UNREAD"));
        assertEquals(false, issuePanel.getSelectedIssue().isCurrentlyRead());

        // minimize window
        press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL); // run this last
        sleep(1000);
        assertEquals(true, stage.isIconified());
    }

    public KeyCode getKeyCode(String shortcut) {
        return KeyCode.getKeyCode(KeyboardShortcuts.getDefaultKeyboardShortcuts().get(shortcut));
    }

    public void clearSelectedIssueId() {
        selectedIssueId = 0;
    }

    public void clearColumnIndex() {
        columnIndex = -1;
    }

    public void clearUiComponentFocusEventType() {
        uiComponentFocusEventType = UIComponentFocusEvent.EventType.NONE;
    }
}
