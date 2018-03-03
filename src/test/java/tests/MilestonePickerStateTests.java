package tests;

import backend.resource.TurboMilestone;
import org.junit.Test;
import ui.components.pickers.MilestonePickerState;
import ui.components.pickers.PickerMilestone;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MilestonePickerStateTests {
    public MilestonePickerState prepareUnassignedState() {
        List<PickerMilestone> milestoneList = new ArrayList<>();
        PickerMilestone milestone = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        milestoneList.add(milestone);
        return new MilestonePickerState(milestoneList, "");
    }

    public MilestonePickerState prepareAssignedState() {
        List<PickerMilestone> milestoneList = new ArrayList<>();
        PickerMilestone milestone1 = new PickerMilestone(new TurboMilestone("testrepo", 1, "milestone1"));
        PickerMilestone milestone2 = new PickerMilestone(new TurboMilestone("testrepo", 2, "milestone2"));
        milestoneList.add(milestone1);
        milestoneList.add(milestone2);
        // userInput is set to 'milestone2'
        return new MilestonePickerState(milestoneList, milestone2.getTitle());
    }

    @Test
    public void toggleMilestone_noMilestone_milestoneAssigned() {
        MilestonePickerState state = prepareUnassignedState();
        state.toggleExactMatchMilestone("milestone1");
        assertTrue(state.getAllMilestones().get(0).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneReplaced() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleExactMatchMilestone("milestone1");
        assertTrue(state.getAllMilestones().get(0).isSelected());
        assertFalse(state.getAllMilestones().get(1).isSelected());
    }

    @Test
    public void toggleMilestone_hasMilestone_milestoneUnassigned() {
        MilestonePickerState state = prepareAssignedState();
        state.toggleExactMatchMilestone("milestone2");
        assertFalse(state.getAllMilestones().get(0).isSelected());
        assertFalse(state.getAllMilestones().get(1).isSelected());
    }

    @Test
    public void bestMatches_withUserInput_previousMatchingMilestoneIncluded() {
        MilestonePickerState state = prepareAssignedState();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());
    }

    @Test
    public void selectNextBestMatch_firstBestMatchSelected_secondBestMatchSelected() {
        MilestonePickerState state = prepareAssignedState();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());

        // move selection to next best matching milestone
        state.selectNextBestMatchingMilestone();
        // first milestone is no longer selected even though it is matching
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        // second milestone in the list is selected although it is not matching
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());
    }

    @Test
    public void selectNextBestMatch_lastBestMatchSelected_noChanges() {
        MilestonePickerState state = prepareAssignedState();
        state.selectNextBestMatchingMilestone();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());

        // move selection to next best matching milestone
        // however, state should be same since we are already on the last milestone in tne list
        state.selectNextBestMatchingMilestone();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());
    }

    @Test
    public void selectPreviousBestMatch_firstBestMatchSelected_noChanges() {
        MilestonePickerState state = prepareAssignedState();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());

        // move selection to the previous best matching milestone
        // however, state should be same since we are already on the first milestone in the list
        state.selectPreviousBestMatchingMilestone();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());
    }

    @Test
    public void selectPreviousBestMatch_secondBestMatchSelected_firstBestMatchSelected() {
        MilestonePickerState state = prepareAssignedState();
        state.selectNextBestMatchingMilestone();
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());

        // move selection to the previous best matching milestone
        state.selectPreviousBestMatchingMilestone();
        // first milestone should be selected again
        assertEquals("milestone2", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
        // second milestone should not be selected
        assertEquals("milestone1", state.getBestMatchingMilestones().get(1).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(1).isSelected());
        assertFalse(state.getBestMatchingMilestones().get(1).isMatching());
    }

    @Test
    public void selectNextBestMatch_noSelected_firstBestMatchSelected() {
        MilestonePickerState state = prepareUnassignedState();
        assertEquals("milestone1", state.getBestMatchingMilestones().get(0).getTitle());
        assertFalse(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());

        state.selectNextBestMatchingMilestone();
        assertEquals("milestone1", state.getBestMatchingMilestones().get(0).getTitle());
        assertTrue(state.getBestMatchingMilestones().get(0).isSelected());
        assertTrue(state.getBestMatchingMilestones().get(0).isMatching());
    }
}
