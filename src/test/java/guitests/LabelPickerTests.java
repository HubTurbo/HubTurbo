package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.listpanel.ListPanelCell;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class LabelPickerTests extends UITest {

    public static final int EVENT_DELAY = 1000;

    @Test
    public void showLabelPickerTest() {
        click("#dummy/dummy_col0_9");
        push(KeyCode.L);
        sleep(EVENT_DELAY);
        assertNodeExists(hasText("Issue #9: Issue 9"));
        push(KeyCode.ENTER);
    }

    @Test
    public void addAndRemoveLabelTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);
        assertEquals(1, listPanelCell.getIssueLabels().size());
        push(KeyCode.L);
        sleep(EVENT_DELAY);
        type("3 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(2, listPanelCell.getIssueLabels().size());
        sleep(EVENT_DELAY);
        push(KeyCode.L);
        type("3 ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

}
