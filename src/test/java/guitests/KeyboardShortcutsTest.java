package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import util.events.ColumnClickedEventHandler;
import util.events.IssueSelectedEventHandler;
import util.events.testevents.UIComponentFocusEvent;
import util.events.testevents.UIComponentFocusEventHandler;
import util.events.testevents.WindowResizeEvent;
import util.events.testevents.WindowResizeEventHandler;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.DOUBLE_PRESS;

public class KeyboardShortcutsTest extends UITest {

    private WindowResizeEvent.EventType windowResizeEventType;
    private UIComponentFocusEvent.EventType uiComponentFocusEventType;
    private int selectedIssueId;
    private int columnIndex;

    // TODO test that events have been triggered
    @Test
    public void keyboardShortcutsTest() {
        UI.events.registerEvent((WindowResizeEventHandler) e -> windowResizeEventType = e.eventType);
        UI.events.registerEvent((IssueSelectedEventHandler) e -> selectedIssueId = e.id);
        UI.events.registerEvent((UIComponentFocusEventHandler) e -> uiComponentFocusEventType = e.eventType);
        UI.events.registerEvent((ColumnClickedEventHandler) e -> columnIndex = e.columnIndex);
        clearEventType();
        clearSelectedIssueId();
        clearUiComponentFocusEventType();
        clearColumnIndex();

        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        assertEquals(WindowResizeEvent.EventType.MAXIMIZE_WINDOW, windowResizeEventType);
        clearEventType();

        // mid-sized window
        press(KeyCode.CONTROL).press(KeyCode.D).release(KeyCode.D).release(KeyCode.CONTROL);
        assertEquals(WindowResizeEvent.EventType.DEFAULT_SIZE_WINDOW, windowResizeEventType);
        clearEventType();

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
        push(getKeyCode("MARK_AS_READ"));

        push(getKeyCode("MARK_AS_UNREAD"));

        // minimize window
        press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL); // run this last
        assertEquals(WindowResizeEvent.EventType.MINIMIZE_WINDOW, windowResizeEventType);
        clearEventType();
    }

    public KeyCode getKeyCode(String shortcut) {
        return KeyCode.getKeyCode(KeyboardShortcuts.getDefaultKeyboardShortcuts().get(shortcut));
    }

    public void clearEventType() {
        windowResizeEventType = WindowResizeEvent.EventType.NONE;
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
