package unstable;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import backend.stub.DummyRepoState;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import guitests.UITest;
import ui.IdGenerator;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

public class ModelUpdateUITest extends UITest {

    private static final int EVENT_DELAY = 1500;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--testjson=true", "--bypasslogin=true");
    }

    @Test
    public void modelUpdate_panel_addedIssuesShowUp()
            throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        FutureTask<Integer> countIssues = new FutureTask<>(getPanel(0)::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, countIssues.get().intValue());
    }

    @Test
    public void modelUpdate_panel_multipleAddedIssuesShowUp()
            throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        addIssue();
        FutureTask<Integer> countIssues = new FutureTask<>(getPanel(0)::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 3, countIssues.get().intValue());
    }

    @Test
    public void modelUpdate_panel_correctNumberOfIssuesShown()
            throws InterruptedException, ExecutionException {
        addIssue();
        resetRepo();
        FutureTask<Integer> countIssues = new FutureTask<>(getPanel(0)::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, countIssues.get().intValue());
    }

    @Test
    public void modelUpdate_panel_deletedIssuesNoLongerShowUp()
            throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        deleteIssue(1);
        FutureTask<Integer> countIssues = new FutureTask<>(getPanel(0)::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, countIssues.get().intValue());
    }

    @Test
    public void modelUpdate_panel_multipleDeletedIssuesNoLongerShowUp()
            throws InterruptedException, ExecutionException {
        resetRepo();
        addIssue();
        addIssue();
        addIssue();
        deleteIssue(1);
        deleteIssue(2);
        FutureTask<Integer> countIssues = new FutureTask<>(getPanel(0)::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, countIssues.get().intValue());
    }

    // TODO no way to check correctness of these events as of yet as they are not reflected on the UI
    @Test
    public void modelUpdate_events_eventsTriggerChanges() throws InterruptedException, ExecutionException {
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

    private void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
        sleep(EVENT_DELAY);
    }

    private void addIssue() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy/dummy"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }

    private void deleteIssue(int itemId) {
        UI.events.triggerEvent(UpdateDummyRepoEvent.deleteIssue("dummy/dummy", itemId));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
    }
}
