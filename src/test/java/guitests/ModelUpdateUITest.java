package guitests;

import junit.framework.Assert;
import org.junit.Test;
import ui.UI;
import ui.issuepanel.IssuePanel;
import util.PlatformEx;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ModelUpdateUITest extends UITest {

    private final int EVENT_DELAY = 750;

    @Test
    @SuppressWarnings("unchecked")
    public void addIssueTest() throws InterruptedException, ExecutionException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        Assert.assertEquals(11, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addMultipleIssuesTest() throws InterruptedException, ExecutionException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        Assert.assertEquals(13, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void countIssuesTest() throws InterruptedException, ExecutionException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        Assert.assertEquals(10, countIssues.get());
    }
}
