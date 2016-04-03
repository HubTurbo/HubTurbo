package guitests;

import javafx.scene.input.KeyCode;

import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import util.events.*;
import util.events.testevents.PrimaryRepoChangedEvent;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

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
        click("New");
        click("Issue");
        assertEquals(1, eventTestCount);
        resetEventTestCount();
        press(NEW_ISSUE);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createLabelTest() {
        UI.events.registerEvent((LabelCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        click("New");
        click("Label");
        assertEquals(1, eventTestCount);
        resetEventTestCount();
        press(NEW_LABEL);
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createMilestoneTest() {
        UI.events.registerEvent((MilestoneCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        click("New");
        click("Milestone");
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
