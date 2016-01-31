package tests;

import org.junit.Test;
import ui.components.pickers.LabelPickerState;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LabelPickerStateTests {

    public LabelPickerState setupState(String... labelNames) {
        return new LabelPickerState(getLabelHashSet(labelNames));
    }

    public Set<String> getLabelHashSet(String... labelNames) {
        Set<String> setOfLabelNames = new HashSet<>();
        for (String labelName : labelNames) {
            setOfLabelNames.add(labelName);
        }

        return setOfLabelNames;
    }

    @Test
    public void updateMatchedLabelTest() {
        LabelPickerState initialState = setupState();
        LabelPickerState nextState = initialState.updateMatchedLabels(
                getLabelHashSet("priority.high", "priority.low", "highest", "Problem.Heavy"),
                "p.h"
        );

        assertEquals(2, nextState.getMatchedLabels().size());
    }

    @Test
    public void currentSuggestionTest() {
        LabelPickerState initialState = setupState();
        LabelPickerState nextState = initialState.updateMatchedLabels(
                getLabelHashSet("priority.high", "priority.low", "highest", "Problem.Heavy"),
                "p.h"
        );

        assertEquals(true, nextState.getCurrentSuggestion().isPresent());
        assertEquals("priority.high", nextState.getCurrentSuggestion().get());

        nextState = nextState.nextSuggestion();
        assertEquals(true, nextState.getCurrentSuggestion().isPresent());
        assertEquals("Problem.Heavy", nextState.getCurrentSuggestion().get());
    }

}
