package unstable;

import guitests.UITest;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import ui.listpanel.ListPanelCell;

import static org.junit.Assert.assertEquals;

public class MilestonePickerTests extends UITest {
    @Test
    public void showMilestonePicker() {
        clickOn("#dummy/dummy_col0_9");
        press(KeyCode.M);

        waitUntilNodeAppears("#milestonePickerTextField");
        TextField textField = GuiTest.find("#milestonePickerTextField");
        clickOn("#milestonePickerTextField");
        type("milestone");
        assertEquals("milestone", textField.getText());
    }

    @Test
    public void pickMilestone_noMilestone_milestoneAssigned() {
        ListPanelCell listPanelCell = GuiTest.find("#dummy/dummy_col0_9");
        clickOn("#dummy/dummy_col0_9");
        press(KeyCode.M);

        waitUntilNodeAppears("#milestonePickerTextField");
        clickOn("#milestonePickerTextField");
        type("8 ");
        push(KeyCode.ENTER);
        assertEquals(true, listPanelCell.getIssue().getMilestone().isPresent());
    }
}
