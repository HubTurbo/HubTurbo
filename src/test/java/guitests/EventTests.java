package guitests;

import org.junit.Test;
import ui.UI;
import util.events.IssueCreatedEventHandler;
import util.events.LabelCreatedEventHandler;
import util.events.MilestoneCreatedEventHandler;

import static org.junit.Assert.assertEquals;

public class EventTests extends UITest {

    public static int eventTestCount;

    public static void increaseEventTestCount() {
        eventTestCount++;
    }

    private static void resetEventTestCount() {
        eventTestCount = 0;
    }

    @Test
    public void createIssueTest() {
        resetEventTestCount();
        UI.events.registerEvent((IssueCreatedEventHandler) e -> {
            EventTests.increaseEventTestCount();
        });
        click("New");
        click("Issue");
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createLabelTest() {
        resetEventTestCount();
        UI.events.registerEvent((LabelCreatedEventHandler) e -> {
            EventTests.increaseEventTestCount();
        });
        click("New");
        click("Label");
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createMilestoneTest() {
        resetEventTestCount();
        UI.events.registerEvent((MilestoneCreatedEventHandler) e -> {
            EventTests.increaseEventTestCount();
        });
        click("New");
        click("Milestone");
        assertEquals(1, eventTestCount);
    }
}
