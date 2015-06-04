package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.issuepanel.IssuePanel;
import ui.issuepanel.IssuePanelCard;
import util.PlatformEx;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

public class UpdateIssuesTest extends UITest {

    private final int EVENT_DELAY = 1000;

    @Test
    public void updateIssues() throws InterruptedException, ExecutionException {
        resetRepo();
        updateIssue(5, "Issue 5.1");
        sleep(3000);
        // After updating, issue with ID 5 should have title Issue 5.1

        // Updated view should only contain Issue 5.1
        click("#dummy/dummy_col0_filterTextField");
        type("updated");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(3000);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(1, countIssues.get());
    }

    public void resetRepo() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void updateIssue(int issueId, String newIssueTitle) {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.UPDATE_ISSUE,
                "dummy/dummy",
                issueId,
                newIssueTitle));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }
}
