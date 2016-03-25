package unstable;

import guitests.UITest;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowMilestonePickerEvent;

import static org.junit.Assert.assertEquals;

public class MilestonePickerTests extends UITest {
    @Test
    public void showMilestonePicker() {
        click("#dummy/dummy_col0_9");
        press(KeyCode.M);

        waitUntilNodeAppears("#milestonePickerTextField");
        TextField textField = find("#milestonePickerTextField");
        click("#milestonePickerTextField");
        type("milestone");
        assertEquals("milestone", textField.getText());
    }

    @Test
    public void pickMilestone_noMilestone_milestoneAssigned() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");
        click("#dummy/dummy_col0_9");
        press(KeyCode.M);

        waitUntilNodeAppears("#milestonePickerTextField");
        click("#milestonePickerTextField");
        type("8 ");
        push(KeyCode.ENTER);
        assertEquals(true, listPanelCell.getIssue().getMilestone().isPresent());
    }
}
