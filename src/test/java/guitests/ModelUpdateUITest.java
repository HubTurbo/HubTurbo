package guitests;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Test;

import ui.UI;
import ui.issuepanel.IssuePanel;
import util.PlatformEx;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

public class ModelUpdateUITest extends UITest {

    private static final int EVENT_DELAY = 1500;

    @Test
    @SuppressWarnings("unchecked")
    public void addIssueTest() throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(11, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addMultipleIssuesTest() throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        addIssue();
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(13, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void countIssuesTest() throws InterruptedException, ExecutionException {
        addIssue();
        resetRepo();
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(10, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deleteIssueTest() throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        deleteIssue(1);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(11, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deleteMultipleIssuesTest() throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        addIssue();
        deleteIssue(1);
        deleteIssue(2);
        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(11, countIssues.get());
    }

    // TODO no way to check correctness of these events as of yet as they are not reflected on the UI
    @Test
    public void otherTriggersTest() throws InterruptedException, ExecutionException {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.NEW_LABEL, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.NEW_MILESTONE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.NEW_USER, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);

        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.DELETE_LABEL, "dummy/dummy", "Label 1"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.DELETE_MILESTONE, "dummy/dummy", "Milestone 1"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.DELETE_USER, "dummy/dummy", "User 1"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);

        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.UPDATE_ISSUE, "dummy/dummy", 1, null, "Issue 11"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.UPDATE_MILESTONE, "dummy/dummy", 1, null, "Milestone 11"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void resetRepo() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        sleep(EVENT_DELAY);
    }

    public void addIssue() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void deleteIssue(int itemId) {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
            UpdateDummyRepoEvent.UpdateType.DELETE_ISSUE, "dummy/dummy", itemId));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }
}
