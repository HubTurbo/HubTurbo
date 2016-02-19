package tests;

import org.junit.Test;

import backend.resource.TurboLabel;
import ui.components.pickers.LabelPickerState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelPickerStateTests {

    @Test
    public void determineState_addedLabels() {
        String query = "f-aa priority.high ";
        LabelPickerState initialState = setupState();
        LabelPickerState nextState = initialState.determineState(query);

        assertEquals(2, nextState.getAddedLabels().size());
    }

    @Test
    public void determineState_exclusiveLabels_removeConflictingLabels() {
        LabelPickerState initialState = setupState("priority.low", "priority.high");
        assertEquals(2, initialState.getInitialLabels().size());
        LabelPickerState nextState = initialState.determineState("priority.medium ");

        assertEquals("priority.medium", nextState.getAddedLabels().get(0));
        assertEquals(1, nextState.getAddedLabels().size());
        assertEquals(2, nextState.getRemovedLabels().size());
        assertTrue(nextState.getRemovedLabels().contains("priority.low"));
        assertTrue(nextState.getRemovedLabels().contains("priority.high"));
    }

    @Test
    public void determineState_assignedLabels() {
        LabelPickerState initialState = setupState("priority.low", "priority.high");
        LabelPickerState nextState = initialState.determineState("priority.medium ");
        assertEquals(1, nextState.getAssignedLabels().size());

        nextState = nextState.determineState("priority.low ");
        nextState = nextState.determineState("Problem.Heavy ");
        nextState = nextState.determineState("priority.medium ");
        assertEquals(2, nextState.getAssignedLabels().size());
        assertTrue(nextState.getAssignedLabels().contains("priority.medium"));
        assertTrue(nextState.getAssignedLabels().contains("Problem.Heavy"));
    }

    public LabelPickerState setupState(String... labelNames) {
        return new LabelPickerState(getHashSet(labelNames), getTestRepoLabels());
    }

    public List<TurboLabel> getTestRepoLabels() {
        List<String> labelNames = getArrayList("priority.high", "priority.medium", "priority.low", 
                                               "highest", "Problem.Heavy", "f-aaa", "f-bbb");

        return labelNames.stream().map(name -> new TurboLabel("", name)).collect(Collectors.toList());
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
