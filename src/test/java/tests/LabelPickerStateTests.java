package tests;

import org.junit.Test;
import ui.components.pickers.LabelPickerState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelPickerStateTests {

    @Test
    public void toggleLabel_labelsWithGroup_labelsAdded() {
        LabelPickerState initialState = setupState();
        LabelPickerState nextState = initialState.toggleLabel("priority.medium");
        assertEquals(1, nextState.getAddedLabels().size());
        nextState = nextState.toggleLabel("f-aaa");
        assertEquals(2, nextState.getAddedLabels().size());
    }

    @Test
    public void getRemovedLabels_exclusiveLabels_removeConflictingLabels() {
        LabelPickerState initialState = setupState("priority.low", "priority.high");
        assertEquals(2, initialState.getInitialLabels().size());
        LabelPickerState nextState = initialState.toggleLabel("priority.medium");

        assertEquals("priority.medium", nextState.getAddedLabels().get(0));
        assertEquals(1, nextState.getAddedLabels().size());
        assertEquals(2, nextState.getRemovedLabels().size());
        assertTrue(nextState.getRemovedLabels().contains("priority.low"));
        assertTrue(nextState.getRemovedLabels().contains("priority.high"));
    }

    @Test
    public void getAssignedLabels() {
        LabelPickerState initialState = setupState("priority.low", "priority.high");
        LabelPickerState nextState = initialState.toggleLabel("priority.medium");
        nextState = nextState.toggleLabel("priority.medium");
        assertEquals(0, nextState.getAssignedLabels().size());

        nextState = nextState.toggleLabel("priority.low");
        nextState = nextState.toggleLabel("priority.low");
        nextState = nextState.toggleLabel("priority.low");
        nextState = nextState.toggleLabel("Problem.Heavy");
        nextState = nextState.toggleLabel("priority.medium");
        assertEquals(2, nextState.getAssignedLabels().size());
        assertTrue(nextState.getAssignedLabels().contains("priority.medium"));
        assertTrue(nextState.getAssignedLabels().contains("Problem.Heavy"));
    }

    public LabelPickerState setupState(String... labelNames) {
        return new LabelPickerState(getHashSet(labelNames), getTestRepoLabels());
    }

    public List<String> getTestRepoLabels() {
        List<String> listOfLabelNames;
        listOfLabelNames = getArrayList("priority.high", "priority.medium", "priority.low", "highest", "Problem.Heavy",
                "f-aaa", "f-bbb");

        return listOfLabelNames;
    }

    public Set<String> getHashSet(String... labelNames) {
        Set<String> setOfLabelNames = new HashSet<>();
        for (String labelName : labelNames) {
            setOfLabelNames.add(labelName);
        }

        return setOfLabelNames;
    }

    public List<String> getArrayList(String... labelNames) {
        List<String> listOfLabelNames = new ArrayList<>();
        for (String labelName : labelNames) {
            listOfLabelNames.add(labelName);
        }

        return listOfLabelNames;
    }

}
