package unstable;

import guitests.UITest;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.loadui.testfx.utils.TestUtils;

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
        Label apiBox = find("#apiBox");
        resetRepo();

        click("#dummy/dummy_col0_filterTextField");
        type("updated");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        // Updated view should contain Issue 9 and 10, which was commented on recently (as part of default test dataset)
        TestUtils.awaitCondition(() -> 2 == countIssuesShown());
        assertEquals(3496, getApiCount(apiBox.getText())); // 4 calls for issues 9 and 10.

        // After updating, issue with ID 5 should have title Issue 5.1
        updateIssue(5, "Issue 5.1"); // 2 calls for issue 5, 1 for issue 9, 1 for issue 10 when refreshing UI.
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER); // 1 call for issue 5, 1 for issue 9, 1 for issue 10.
        PlatformEx.waitOnFxThread();

        // Updated view should now contain Issue 5.1, Issue 9 and Issue 10.
        TestUtils.awaitCondition(() -> 3489 == getApiCount(apiBox.getText()));
        assertEquals(3, countIssuesShown());

        // Then have a non-self comment for Issue 9.
        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 9, "Test comment", "test-nonself"));
        UI.events.triggerEvent(new UILogicRefreshEvent()); // 1 call for issues 5, 9, 10.
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER); // 1 call for issues 5, 9, 10.
        PlatformEx.waitOnFxThread();
        TestUtils.awaitCondition(() -> 3483 == getApiCount(apiBox.getText()));
        assertEquals(3, countIssuesShown());

        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER); // 1 call for issues 5, 9, 10.
        PlatformEx.waitOnFxThread();
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

    @SuppressWarnings("unchecked")
    public int countIssuesShown() throws InterruptedException, ExecutionException {
        FutureTask<Integer> countIssues = new FutureTask<Integer>(((ListPanel) 
                TestController.getUI().getPanelControl().getPanel(0))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        return (int) countIssues.get();
    }

    private int getApiCount(String apiBoxText) {
        return Integer.parseInt(apiBoxText.substring(0, apiBoxText.indexOf("/")));
    }
}
