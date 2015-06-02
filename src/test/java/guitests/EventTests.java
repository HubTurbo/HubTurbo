package guitests;

import org.junit.Test;
import ui.UI;
import util.events.IssueCreatedEventHandler;

import static org.junit.Assert.assertEquals;

public class EventTests extends UITest {

    public static int createIssueCount;

    public static void increaseCreateIssueCount() {
        createIssueCount++;
    }

    @Test
    public void createIssueTest() {
        createIssueCount = 0;
        UI.events.registerEvent((IssueCreatedEventHandler) e -> {
            EventTests.increaseCreateIssueCount();
        });
        click("New");
        click("Issue");
        assertEquals(1, createIssueCount);
    }
}
