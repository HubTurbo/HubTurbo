package ui.components.issuepicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import backend.resource.TurboIssue;

/**
 * This class is used to represent the state of the issue picker. 
 */
public class IssuePickerState {

    private List<TurboIssue> selectedIssues;
    private List<TurboIssue> suggestedIssues;
    private Optional<TurboIssue> currentSuggestion = Optional.empty();

    private final List<TurboIssue> allIssues;

    public IssuePickerState(List<TurboIssue> allIssues, String userInput) {
        this(allIssues, new ArrayList<>(), new ArrayList<>());
        update(userInput);
    }

    private IssuePickerState(List<TurboIssue> allIssues, List<TurboIssue> selectedIssues, 
                             List<TurboIssue> suggestedIssues) {
        this.selectedIssues = selectedIssues;
        this.suggestedIssues = suggestedIssues;
        this.allIssues = allIssues;
    }

    public List<TurboIssue> getSelectedIssues() {
        return selectedIssues;
    }

    public List<TurboIssue> getSuggestedIssues() {
        return suggestedIssues;
    }

    /**
     * @return current suggestion if there are matches
     */
    public Optional<TurboIssue> getCurrentSuggestion() {
        return currentSuggestion;
    }

    /**
     * Updates current state based on given user input
     * @param userInput 
     */
    private final void update(String userInput) {
        String[] keywords = userInput.split(";");

        for (int i = 0; i < keywords.length; i++) {
            String query = keywords[i].trim();

            if (isKeywordInProgress(keywords, i)) {
                updateSuggestedIssues(allIssues, query);
                if (!query.isEmpty()) currentSuggestion = suggestedIssues.stream().findFirst();
            } else {
                TurboIssue.getFirstMatchingIssue(allIssues, query).ifPresent(this::updateSelectedIssues);
            }
        }
    }


    /**
     * Only the last keyword is considered in progress except if it is the only keyword
     */
    private boolean isKeywordInProgress(String[] keywords, int i) {
        return i == keywords.length - 1;
    }

    /**
     * Updates selected issues if issue is present
     * @param issue
     */
    public void updateSelectedIssues(TurboIssue selectedIssue) {
       
        if (selectedIssues.contains(selectedIssue)) {
            selectedIssues.remove(selectedIssue);
        } else {
            selectedIssues.add(selectedIssue);
        }
    }

    /**
     * Updates suggested issues with given query
     * @param issue
     */
    private void updateSuggestedIssues(List<TurboIssue> issues, String query) {
        suggestedIssues.clear();
        suggestedIssues.addAll(TurboIssue.getMatchedIssues(issues, query));
    }
}
