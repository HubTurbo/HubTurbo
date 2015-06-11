package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.issuepanel.IssuePanel;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import static org.junit.Assert.assertEquals;

public class IssuePanelTests extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Test
    public void keepSelectionTest() {
        // checks to see if IssuePanel keeps the same issue selected even after
        // the list is updated
        IssuePanel issuePanel = find("#dummy/dummy_col0");
        click("#dummy/dummy_col0_filterTextField");
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("date");
        push(KeyCode.ENTER);
        push(KeyCode.SPACE).push(KeyCode.SPACE);
        push(KeyCode.DOWN).push(KeyCode.DOWN);
        sleep(EVENT_DELAY);
        assertEquals(3, issuePanel.getSelectedIssue().getId());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new UpdateDummyRepoEvent(
                UpdateDummyRepoEvent.UpdateType.UPDATE_ISSUE, "dummy/dummy", 3, "updated issue"));
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(EVENT_DELAY);
        assertEquals(4, issuePanel.getSelectedIssue().getId());
    }

}
