package ui.components.issuepicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import backend.resource.TurboIssue;

/**
 * Represents the state of the issue picker.
 */
public class IssuePickerState {

    private List<TurboIssue> suggestedIssues;
    private Optional<TurboIssue> selectedIssue = Optional.empty();

    private final List<TurboIssue> allIssues;

    public IssuePickerState(List<TurboIssue> allIssues, String userInput) {
        this(allIssues, new ArrayList<>());
        setIssues(userInput);
    }

    private IssuePickerState(List<TurboIssue> allIssues, List<TurboIssue> suggestedIssues) {
        this.suggestedIssues = suggestedIssues;
        this.allIssues = allIssues;
    }

    public Optional<TurboIssue> getSelectedIssue() {
        return selectedIssue;
    }

    public List<TurboIssue> getSuggestedIssues() {
        return suggestedIssues;
    }

    /**
     * Sets issues based on given user input
     *
     * @param userInput
     */
    private final void setIssues(String userInput) {
        String query = userInput.trim();
        setSuggestedIssues(allIssues, query);
        if (query.isEmpty()) return;
        selectedIssue = suggestedIssues.stream().findFirst();
    }

    /**
     * Sets selected issues if issue is present
     *
     * @param issue
     */
    public void setSelectedIssues(TurboIssue selectedIssue) {
        this.selectedIssue = Optional.of(selectedIssue);
    }

    /**
     * Sets suggested issues with given query
     *
     * @param issue
     */
    private void setSuggestedIssues(List<TurboIssue> issues, String query) {
        suggestedIssues.clear();
        suggestedIssues.addAll(TurboIssue.getMatchedIssues(issues, query));
    }
}
