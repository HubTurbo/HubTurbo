package guitests;

import org.junit.Test;
import ui.UI;
import util.events.ColumnClickedEventHandler;
import util.events.IssueCreatedEventHandler;
import util.events.LabelCreatedEventHandler;
import util.events.MilestoneCreatedEventHandler;

import static org.junit.Assert.assertEquals;

public class UIEventTests extends UITest {

    public static int eventTestCount;

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
    }

    @Test
    public void createLabelTest() {
        UI.events.registerEvent((LabelCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        click("New");
        click("Label");
        assertEquals(1, eventTestCount);
    }

    @Test
    public void createMilestoneTest() {
        UI.events.registerEvent((MilestoneCreatedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        click("New");
        click("Milestone");
        assertEquals(1, eventTestCount);
    }

    @Test
    public void columnClickedTest() {
        UI.events.registerEvent((ColumnClickedEventHandler) e -> UIEventTests.increaseEventTestCount());
        resetEventTestCount();
        click("#dummy/dummy_col0_filterTextField");
        assertEquals(1, eventTestCount);
    }
}
