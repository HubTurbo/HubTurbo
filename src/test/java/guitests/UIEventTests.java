package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import util.PlatformEx;
import util.events.*;
import util.events.testevents.PrimaryRepoChangedEvent;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

public class UIEventTests extends UITest {

    private static String defaultRepoId;
    static int eventTestCount;

    public static void increaseEventTestCount() {
        eventTestCount++;
    }

    private static void resetEventTestCount() {
        eventTestCount = 0;
    }
    
    private static void getEventRepoId(PrimaryRepoChangedEvent e) {
        defaultRepoId = e.repoId;
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
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(1, eventTestCount);
    }
    
    @Test
    public void defaultRepoSwitchedTest() {
        UI.events.registerEvent((PrimaryRepoChangedEventHandler) e -> UIEventTests.increaseEventTestCount());
        UI.events.registerEvent((PrimaryRepoChangedEventHandler) e -> UIEventTests.getEventRepoId(e));
        resetEventTestCount();
        press(KeyboardShortcuts.SWITCH_DEFAULT_REPO);
        assertEquals(1, eventTestCount);
        resetEventTestCount();

        // Test with multiple repositories
        ComboBox<String> comboBox = find("#repositorySelector");
        click(comboBox);
        selectAll();
        type("dummy3/dummy3");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        resetEventTestCount();
        press(KeyboardShortcuts.SWITCH_DEFAULT_REPO);
        assertEquals(1, eventTestCount);
        press(KeyboardShortcuts.SWITCH_DEFAULT_REPO);
        assertEquals("dummy3/dummy3", defaultRepoId);
    }
}
