package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

import static org.junit.Assert.assertEquals;

public class ScrollableListViewTests extends UITest {

    @Test
    public void scrollAndShowTest() {
        for (int i = 0; i < 40; i++) {
            UI.events.triggerEvent(new UpdateDummyRepoEvent(
                    UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy/dummy"));
        }
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(2000);
        ListPanel col0 = find("#dummy/dummy_col0");
        click("#dummy/dummy_col0_49");
        for (int i = 0; i < 40; i++) {
            push(KeyCode.DOWN);
        }
        sleep(1000);
        assertEquals(9, col0.getSelectedIssue().getId());
        for (int i = 0; i < 40; i++) {
            push(KeyCode.UP);
        }
        sleep(1000);
        assertEquals(49, col0.getSelectedIssue().getId());
    }

}
