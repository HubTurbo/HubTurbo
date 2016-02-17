package tests;

import backend.resource.TurboMilestone;
import org.junit.Test;
import ui.components.pickers.MilestonePickerState;
import ui.components.pickers.PickerMilestone;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class MilestonePickerTests {
    public MilestonePickerState prepareState() {
        List<PickerMilestone> labelList = new ArrayList<>();
        PickerMilestone label = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        labelList.add(label);
        MilestonePickerState state = new MilestonePickerState(labelList);
        return state;
    }

    @Test
    public void toggleMilestone_noMilestone_milestoneAssigned() {
        MilestonePickerState state = prepareState();
        state.toggleMilestone("milestone1");
        assertEquals(true, state.getCurrentMilestonesList().get(0).isSelected());
    }
}
