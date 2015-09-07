package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

public class UpdateIssuesTest extends UITest {

    private static final int EVENT_DELAY = 500;

    @Test
    public void updateIssues() throws InterruptedException, ExecutionException {
        resetRepo();

        click("#dummy/dummy_col0_filterTextField");
        type("updated");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);

        // Updated view should contain Issue 9 and 10, which was commented on recently (as part of default test dataset)
        assertEquals(2, countIssuesShown());

        // After updating, issue with ID 5 should have title Issue 5.1
        updateIssue(5, "Issue 5.1");
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);

        // Updated view should now contain Issue 5.1 and Issue 10.
        assertEquals(2, countIssuesShown());

        // Then have a non-self comment for Issue 9.
        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 9, "Test comment", "test-nonself"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(3, countIssuesShown());
    }

    public void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
        sleep(EVENT_DELAY);
    }

    public void updateIssue(int issueId, String newIssueTitle) {
        UI.events.triggerEvent(UpdateDummyRepoEvent.updateIssue("dummy/dummy", issueId, newIssueTitle));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    @SuppressWarnings("unchecked")
    public int countIssuesShown() throws InterruptedException, ExecutionException {
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        return (int) countIssues.get();
    }
}
