package unstable;

import org.junit.Test;

import guitests.UITest;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

public class ModelUpdateUITest extends UITest {

    private static final int EVENT_DELAY = 1500;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--testjson=true", "--bypasslogin=true");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addIssueTest() throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
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
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(13, countIssues.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void countIssuesTest() throws InterruptedException, ExecutionException {
        addIssue();
        resetRepo();
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
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
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
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
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(11, countIssues.get());
    }

    // TODO no way to check correctness of these events as of yet as they are not reflected on the UI
    @Test
    public void otherTriggersTest() throws InterruptedException, ExecutionException {
        UI.events.triggerEvent(UpdateDummyRepoEvent.newLabel("dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.newMilestone("dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.newUser("dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);

        UI.events.triggerEvent(UpdateDummyRepoEvent.deleteLabel("dummy/dummy", "Label 1"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.deleteMilestone("dummy/dummy", 1));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.deleteUser("dummy/dummy", "User 1"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);

        UI.events.triggerEvent(UpdateDummyRepoEvent.updateIssue("dummy/dummy", 1, "Issue 11"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(UpdateDummyRepoEvent.updateMilestone("dummy/dummy", 1, "Milestone 11"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
        sleep(EVENT_DELAY);
    }

    public void addIssue() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    public void deleteIssue(int itemId) {
        UI.events.triggerEvent(UpdateDummyRepoEvent.deleteIssue("dummy/dummy", itemId));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }
}
