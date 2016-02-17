package tests;

import backend.resource.TurboMilestone;
import org.junit.Test;
import ui.components.pickers.MilestonePickerState;
import ui.components.pickers.PickerMilestone;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class MilestonePickerTests {
    public MilestonePickerState prepareUnassignedState() {
        List<PickerMilestone> labelList = new ArrayList<>();
        PickerMilestone label = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        labelList.add(label);
        MilestonePickerState state = new MilestonePickerState(labelList);
        return state;
    }

    public MilestonePickerState prepareAssignedState() {
        List<PickerMilestone> labelList = new ArrayList<>();
        PickerMilestone label = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        PickerMilestone label2 = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone2"));
        label2.setSelected(true);
        labelList.add(label);
        labelList.add(label2);
        MilestonePickerState state = new MilestonePickerState(labelList);
        return state;
    }

    @Test
    public void toggleMilestone_noMilestone_milestoneAssigned() {
        MilestonePickerState state = prepareUnassignedState();
        state.toggleMilestone("milestone1");
        assertEquals(true, state.getCurrentMilestonesList().get(0).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneReplaced() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleMilestone("milestone1");
        assertEquals(true, state.getCurrentMilestonesList().get(0).isSelected());
        assertEquals(false, state.getCurrentMilestonesList().get(1).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneUnassigned() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleMilestone("milestone2");
        assertEquals(false, state.getCurrentMilestonesList().get(0).isSelected());
        assertEquals(false, state.getCurrentMilestonesList().get(1).isSelected());
    }
}
