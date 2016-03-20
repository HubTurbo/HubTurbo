package tests;

import backend.resource.TurboMilestone;
import org.junit.Test;
import ui.components.pickers.MilestonePickerState;
import ui.components.pickers.PickerMilestone;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class MilestonePickerStateTests {
    public MilestonePickerState prepareUnassignedState() {
        List<PickerMilestone> labelList = new ArrayList<>();
        PickerMilestone label = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        labelList.add(label);
        return new MilestonePickerState(labelList);
    }

    public MilestonePickerState prepareAssignedState() {
        List<PickerMilestone> labelList = new ArrayList<>();
        PickerMilestone label = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        PickerMilestone label2 = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone2"));
        label2.setSelected(true);
        labelList.add(label);
        labelList.add(label2);
        return new MilestonePickerState(labelList);
    }

    @Test
    public void toggleMilestone_noMilestone_milestoneAssigned() {
        MilestonePickerState state = prepareUnassignedState();
        state.toggleExactMatchMilestone("milestone1");
        assertTrue(state.getCurrentMilestonesList().get(0).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneReplaced() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleExactMatchMilestone("milestone1");
        assertTrue(state.getCurrentMilestonesList().get(0).isSelected());
        assertFalse(state.getCurrentMilestonesList().get(1).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneUnassigned() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleExactMatchMilestone("milestone2");
        assertFalse(state.getCurrentMilestonesList().get(0).isSelected());
        assertFalse(state.getCurrentMilestonesList().get(1).isSelected());
    }
}
