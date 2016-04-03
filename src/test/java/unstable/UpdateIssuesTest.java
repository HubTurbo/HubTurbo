package unstable;

import guitests.UITest;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.loadui.testfx.utils.TestUtils;

import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;

public class UpdateIssuesTest extends UITest {

    @Test
    public void updateIssues() throws InterruptedException, ExecutionException {
        Label apiBox = find(IdGenerator.getApiBoxIdReference());
        resetRepo();

        clickFilterTextFieldAtPanel(0);
        type("updated:24");
        push(KeyCode.ENTER);

        // Updated view should contain Issue 9 and 10, which was commented on recently (as part of default test dataset)
        TestUtils.awaitCondition(() -> 3496 == getApiCount(apiBox.getText())); // 4 calls for issues 9 and 10.
        assertEquals(2, countIssuesShown());

        // After updating, issue with ID 5 should have title Issue 5.1
        updateIssue(5, "Issue 5.1"); // 2 calls for issue 5, 1 for issue 9, 1 for issue 10 when refreshing UI.
        clickFilterTextFieldAtPanel(0);
        push(KeyCode.ENTER); // 1 call for issue 5, 1 for issue 9, 1 for issue 10.

        // Updated view should now contain Issue 5.1, Issue 9 and Issue 10.
        TestUtils.awaitCondition(() -> 3489 == getApiCount(apiBox.getText()));
        assertEquals(3, countIssuesShown());

        // Then have a non-self comment for Issue 9.
        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 9, "Test comment", "test-nonself"));
        UI.events.triggerEvent(new UILogicRefreshEvent()); // 1 call for issues 5, 9, 10.
        clickFilterTextFieldAtPanel(0);
        push(KeyCode.ENTER); // 1 call for issues 5, 9, 10.
        TestUtils.awaitCondition(() -> 3483 == getApiCount(apiBox.getText()));
        assertEquals(3, countIssuesShown());

        clickFilterTextFieldAtPanel(0);
        push(KeyCode.ENTER); // 1 call for issues 5, 9, 10.
        TestUtils.awaitCondition(() -> 3480 == getApiCount(apiBox.getText()));
    }

    public void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
        PlatformEx.waitOnFxThread();
    }

    public void updateIssue(int issueId, String newIssueTitle) {
        UI.events.triggerEvent(UpdateDummyRepoEvent.updateIssue("dummy/dummy", issueId, newIssueTitle));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        PlatformEx.waitOnFxThread();
    }

    public int countIssuesShown() throws InterruptedException, ExecutionException {
        FutureTask<Integer> countIssues = new FutureTask<>(((ListPanel)
                TestController.getUI().getPanelControl().getPanel(0))::getIssuesCount);
        PlatformEx.runAndWait(countIssues);
        return countIssues.get();
    }

    private int getApiCount(String apiBoxText) {
        return Integer.parseInt(apiBoxText.substring(0, apiBoxText.indexOf('/')));
    }
}
