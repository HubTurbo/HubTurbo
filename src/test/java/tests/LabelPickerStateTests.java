package tests;

import org.junit.Test;

import backend.resource.TurboLabel;
import ui.components.pickers.LabelPickerState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelPickerStateTests {

    @Test
    public void determineState_addMatchedLabels() {
        LabelPickerState state = setupState("f-aa p.high ", "");

        assertEquals(2, state.getAddedLabels().size());
    }

    @Test
    public void determineState_removeMatchedAddedLabel() {
        LabelPickerState state = setupState("p.medium ", "priority.medium");

        assertEquals(0, state.getAddedLabels().size());
    }

    @Test
    public void determineState_invalidQuery_noChangeToState() {
        LabelPickerState state = setupState("       ", "test");

        assertEquals(0, state.getAddedLabels().size());
    }

    @Test
    public void determineState_exclusiveLabels_removeConflictingLabels() {
        LabelPickerState state = setupState("p.medium ", "priority.low");
        assertEquals(1, state.getInitialLabels().size());
        assertEquals(new TurboLabel("", "priority.medium"), state.getAddedLabels().get(0));
        assertEquals(1, state.getAddedLabels().size());
        assertEquals(1, state.getRemovedLabels().size());
        assertTrue(state.getRemovedLabels().contains(new TurboLabel("", "priority.low")));
    }

    @Test
    public void determineState_labelsInSameGroup_oneLabelAssigned() {
        LabelPickerState state = setupState("p.medium ", "priority.low", "priority.high");

        assertEquals(1, state.getAssignedLabels().size());
    }


    private LabelPickerState setupState(String userInput, String... labelNames) {
        return new LabelPickerState(getLabelsFromNames(labelNames), getTestRepoLabels(), userInput);
    }

    private List<TurboLabel> getTestRepoLabels() {
        return getLabelsFromNames("priority.high", "priority.medium", "priority.low", "highest", 
                                  "Problem.Heavy", "f-aaa", "f-bbb");
    }

    private List<TurboLabel> getLabelsFromNames(String... names) {
        return Arrays.asList(names).stream().map(name -> new TurboLabel("", name)).collect(Collectors.toList());
    }
}
