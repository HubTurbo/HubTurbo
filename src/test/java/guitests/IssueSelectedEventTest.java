package guitests;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.IssueSelectedEventHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IssueSelectedEventTest extends UITest{

    private static final String PANEL_IDENTIFIER = "#dummy/dummy_col0";

    private static AtomicInteger eventTestCount = new AtomicInteger();

    public static void increaseEventTestCount() {
        eventTestCount.getAndIncrement();
    }

    private static void resetEventTestCount() {
        eventTestCount.set(0);
    }

    @Before
    public void setup() {
        UI.events.registerEvent((IssueSelectedEventHandler) e -> IssueSelectedEventTest.increaseEventTestCount());
    }

    @Test
    public void noTriggerIssueSelectedOnRightClick_IssueInPanelRightClicked_IssueSelectedNotTriggered() {
        resetEventTestCount();

        ListPanel issuePanel = find(PANEL_IDENTIFIER);

        //testing whether right click occurred by checking the presence of context menu items
        rightClick(PANEL_IDENTIFIER + "_9");
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            assertTrue(!menuItem.isDisable());
        }

        // testing IssueSelectedEvent not registered on right click
        assertEquals(0, eventTestCount.get());
    }

    /**
     * Tests whether left click and key press triggers IssueSelectedEvent
     */
    @Test
    public void triggerIssueSelectedOnLeftClickAndKey_IssueInPanelLeftClickedAndKeyed_IssueSelectedTriggered() {
        resetEventTestCount();

        click(PANEL_IDENTIFIER + "_9");

        // testing IssueSelectedEvent is triggered on left click
        assertEquals(1, eventTestCount.get());

        //testing IssueSelectedEvent is triggered on key down to a particular issue
        push(KeyCode.DOWN);
        assertEquals(2, eventTestCount.get());
    }

}
