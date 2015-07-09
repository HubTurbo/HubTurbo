package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.listpanel.ListPanelCell;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class LabelPickerTests extends UITest {

    @Test
    public void showLabelPickerTest() {
        click("#dummy/dummy_col0_9");
        push(KeyCode.L);
        sleep(1000);
        assertNodeExists(hasText("Issue #9: Issue 9"));
        push(KeyCode.ENTER);
    }

    @Test
    public void addAndRemoveLabelTest() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click(listPanelCell);
        assertEquals(1, listPanelCell.getIssueLabels().size());
        push(KeyCode.L);
        sleep(500);
        type("3 ");
        push(KeyCode.ENTER);
        assertEquals(2, listPanelCell.getIssueLabels().size());
        sleep(500);
        push(KeyCode.L);
        type("3 ");
        push(KeyCode.ENTER);
        assertEquals(1, listPanelCell.getIssueLabels().size());
    }

}
