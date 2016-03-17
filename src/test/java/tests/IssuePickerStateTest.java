package tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import backend.resource.TurboIssue;
import ui.components.issuepicker.IssuePickerState;

public class IssuePickerStateTest {

    @Test
    public void determineState_addMatchedLabels() {
        IssuePickerState state = setupState("dummy/dummy", Arrays.asList("test", "hello"), "test; ");

        assertEquals(1, state.getSelectedIssues().size());
    }

    private IssuePickerState setupState(String repoId, List<String> issueTitles, String userInput) {
        return new IssuePickerState(getTestIssues(repoId, issueTitles), userInput);
    }

    private List<TurboIssue> getTestIssues(String repoId, List<String> issueTitles) {
        return IntStream.range(0, issueTitles.size())
            .mapToObj(index -> new TurboIssue(repoId, index + 1, issueTitles.get(index)))
            .collect(Collectors.toList());
    }
}
