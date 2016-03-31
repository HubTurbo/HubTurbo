package unstable;

import guitests.UITest;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MilestonePickerTests extends UITest {

    @Test
    public void showMilestonePicker() {
        clickIssue(0, 9);
        push(KeyCode.M);

        clickMilestonePickerTextField();
        getMilestonePickerTextField().clear();
        type("milestone");
        assertEquals("milestone", getMilestonePickerTextField().getText());
    }

    @Test
    public void pickMilestone_noMilestone_milestoneAssigned() {
        clickIssue(0, 9);
        push(KeyCode.M);

        clickMilestonePickerTextField();
        getMilestonePickerTextField().clear();
        type("8 ");
        push(KeyCode.ENTER);
        assertEquals(true, getIssueCell(0, 9).getIssue().getMilestone().isPresent());
    }
}
