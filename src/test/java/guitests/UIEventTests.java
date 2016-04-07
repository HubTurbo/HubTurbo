package guitests;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.NEW_ISSUE;
import static ui.components.KeyboardShortcuts.NEW_LABEL;
import static ui.components.KeyboardShortcuts.NEW_MILESTONE;

import org.junit.Test;

import javafx.scene.input.KeyCode;
import ui.UI;
import ui.components.KeyboardShortcuts;
import util.events.IssueCreatedEventHandler;
import util.events.LabelCreatedEventHandler;
import util.events.MilestoneCreatedEventHandler;
import util.events.PanelClickedEventHandler;
import util.events.ShowIssuePickerEventHandler;

public class UIEventTests extends UITest {

    static int eventTestCount;

    public static void increaseEventTestCount() {
        eventTestCount++;
    }

    private static void resetEventTestCount() {
        eventTestCount = 0;
    }

    @Test
    public void createIssueTest() {
        UI.events.registerEvent((IssueCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        traverseMenu("New", "Issue");
        push(KeyCode.ENTER);
        assertEquals(1, eventTestCount);
        resetEventTestCount();
        press(NEW_ISSUE);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createLabelTest() {
        UI.events.registerEvent((LabelCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        traverseMenu("New", "Label");
        push(KeyCode.ENTER);
        assertEquals(1, eventTestCount);
        resetEventTestCount();
        press(NEW_LABEL);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createMilestoneTest() {
        UI.events.registerEvent((MilestoneCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        traverseMenu("New", "Milestone");
        push(KeyCode.ENTER);
        assertEquals(1, eventTestCount);
        resetEventTestCount();
        press(NEW_MILESTONE);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void panelClickedTest() {
        UI.events.registerEvent((PanelClickedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        clickFilterTextFieldAtPanel(0);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void triggerIssuePicker_dialogAppears() {
        UI.events.registerEvent((ShowIssuePickerEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        press(KeyboardShortcuts.SHOW_ISSUE_PICKER);
        assertEquals(1, eventTestCount);
        press(KeyCode.ESCAPE);
    }
}
