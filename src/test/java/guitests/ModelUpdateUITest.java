package guitests;

import org.junit.Test;
import ui.UI;
import ui.issuepanel.IssuePanel;
import util.PlatformEx;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ModelUpdateUITest extends UITest {

    private final int EVENT_DELAY = 1000;

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

//    @Test
//    @SuppressWarnings("unchecked")
//    public void deleteIssueTest() throws InterruptedException, ExecutionException {
//        resetRepo();
//        addIssue();
//        addIssue();
//        deleteIssue();
//        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
//        PlatformEx.runAndWait(countIssues);
//        Assert.assertEquals(11, countIssues.get());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    public void deleteMultipleIssuesTest() throws InterruptedException, ExecutionException {
//        resetRepo();
//        addIssue();
//        addIssue();
//        addIssue();
//        deleteIssue();
//        deleteIssue();
//        FutureTask countIssues = new FutureTask(((IssuePanel) find("#dummy/dummy_col0"))::getIssueCount);
//        PlatformEx.runAndWait(countIssues);
//        Assert.assertEquals(11, countIssues.get());
//    }

    public void resetRepo() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.RESET_REPO, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void addIssue() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void deleteIssue() {
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.DELETE_ISSUE, "dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }
}
