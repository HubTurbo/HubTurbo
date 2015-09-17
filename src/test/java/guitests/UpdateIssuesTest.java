package guitests;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

    @Test
    public void updateIssues() throws InterruptedException, ExecutionException {
        Label apiBox = find("#apiBox");
        resetRepo();
        TextField filterField = find("#dummy/dummy_col0_filterTextField");

        click(filterField);
        type("updated");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        // Updated view should contain Issue 9 and 10, which was commented on recently (as part of default test dataset)
        assertEquals(2, countIssuesShown());

        // After updating, issue with ID 5 should have title Issue 5.1
        updateIssue(5, "Issue 5.1");
        click(filterField);
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        // Updated view should now contain Issue 5.1, Issue 9 and Issue 10.
        assertEquals(3494, getApiCount(apiBox.getText())); // 2 calls for Issue 5
        assertEquals(3, countIssuesShown());

        // Then have a non-self comment for Issue 9.
        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 9, "Test comment", "test-nonself"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        click(filterField);
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(3492, getApiCount(apiBox.getText()));
        assertEquals(3, countIssuesShown());

        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(3492, getApiCount(apiBox.getText())); // No change to issues, so no additional API quota spent

        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 6, "Test comment", "test"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        filterField.selectAll();
        push(KeyCode.BACK_SPACE);
        type("updated-self");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("2");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(2, countIssuesShown());

        UI.events.triggerEvent(UpdateDummyRepoEvent.addComment("dummy/dummy", 8, "Test comment", "test-nonself"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        filterField.selectAll();
        push(KeyCode.BACK_SPACE);
        type("updated");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(5, countIssuesShown());

        filterField.selectAll();
        push(KeyCode.BACK_SPACE);
        type("updated-others");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(4, countIssuesShown());
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
        FutureTask countIssues = new FutureTask(((ListPanel) find("#dummy/dummy_col0"))::getIssueCount);
        PlatformEx.runAndWait(countIssues);
        return (int) countIssues.get();
    }

    private int getApiCount(String apiBoxText) {
        return Integer.parseInt(apiBoxText.substring(0, apiBoxText.indexOf("/")));
    }
}
