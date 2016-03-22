package guitests;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.IssueSelectedEventHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IssueSelectedEventTest extends UITest {

    @Test
    public void eventTriggerOnIssueSelection_byRightClick_selectionEventNotTriggered() {
        AtomicInteger eventCount = new AtomicInteger(0);
        UI.events.registerEvent((IssueSelectedEventHandler) e -> eventCount.incrementAndGet());
        ListPanel issuePanel = getPanel(0);

        //testing whether right click occurred by checking the presence of context menu items
        rightClickIssue(0, 9);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()) {
            assertTrue(!menuItem.isDisable());
        }

        // testing IssueSelectedEvent not registered on right click
        assertEquals(0, eventCount.get());
    }

    /**
     * Tests whether left click and key press triggers IssueSelectedEvent
     */
    @Test
    public void triggerIssueSelectedOnLeftClickAndKey_IssueInPanelLeftClickedAndKeyed_IssueSelectedTriggered() {
        AtomicInteger eventCount = new AtomicInteger(0);
        UI.events.registerEvent((IssueSelectedEventHandler) e -> eventCount.incrementAndGet());

        clickIssue(0, 10);

        // testing IssueSelectedEvent is triggered on left click
        assertEquals(1, eventCount.get());

        //testing IssueSelectedEvent is triggered on key down to a particular issue
        push(KeyCode.DOWN);
        assertEquals(2, eventCount.get());
    }

}
