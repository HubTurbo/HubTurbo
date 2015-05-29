package guitests;

import junit.framework.Assert;
import org.junit.Test;
import ui.UI;
import ui.issuepanel.IssuePanel;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

public class ModelUpdateUITest extends UITest {

    @Test
    public void addIssueTest() throws InterruptedException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(500);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(500);
        IssuePanel issuePanel = find("#dummy/dummy_col0");
        Assert.assertEquals(issuePanel.getIssueCount(), 11);
    }

    @Test
    public void countIssuesTest() throws InterruptedException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(500);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(500);
        IssuePanel issuePanel = find("#dummy/dummy_col0");
        Assert.assertEquals(issuePanel.getIssueCount(), 10);
    }
}
