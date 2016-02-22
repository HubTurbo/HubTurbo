package ui.components.pickers;

import backend.resource.TurboLabel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to represent the state of the label picker. In addition to representing a state,
 * it contains the logic that handles every state transition
 */
public class LabelPickerState {

    Set<String> initialLabels;
    List<String> addedLabels;
    List<String> removedLabels;
    List<String> matchedLabels;
    List<TurboLabel> repoLabels;
    OptionalInt currentSuggestionIndex;

    public LabelPickerState(Set<String> initialLabels, List<TurboLabel> repoLabels, String userInput) {
        this(initialLabels, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), repoLabels,
                OptionalInt.empty());
        update(userInput);
    }

    private LabelPickerState(Set<String> initialLabels, List<String> addedLabels, List<String> removedLabels,
                             List<String> matchedLabels, List<TurboLabel> repoLabels, 
                             OptionalInt currentSuggestionIndex) {
        this.initialLabels = initialLabels;
        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.matchedLabels = matchedLabels;
        this.repoLabels = repoLabels;
        this.currentSuggestionIndex = currentSuggestionIndex;
    }

    /**
     * Updates current state based on given user input
     * @param userInput 
     */
    public void update(String userInput) {
        List<String> confirmedKeywords = getConfirmedKeywords(userInput);
        for (String confirmedKeyword : confirmedKeywords) {
            findAndToggleMatchingLabel(confirmedKeyword);
        }

        Optional<String> keywordInProgess = getKeywordInProgress(userInput);
        if (keywordInProgess.isPresent()) {
            updateMatchedLabels(keywordInProgess.get());
        }
    }

    /**
     * Update current state if there is exactly one matching label based on the 
     * given keyword
     * @param keyword
     */
    private void findAndToggleMatchingLabel(String keyword) {
        if (TurboLabel.hasExactlyOneMatchedLabel(repoLabels, keyword)) {
            String labelName =
                    TurboLabel.getMatchedLabelName(repoLabels, keyword).get(0).getActualName();
            toggleLabel(labelName);
        }
    }

    /**
     * Updates current state based on properties of selected label
     * labelName is case-sensitive
     * @param labelName
     */
    public void toggleLabel(String labelName) {
        if (isAnInitialLabel(labelName)) {
            if (isARemovedLabel(labelName)) {
                // add back initial label
                removeConflictingLabels(labelName);
                removedLabels.remove(labelName);
            } else {
                removedLabels.add(labelName);
            }
        } else {
            if (isAnAddedLabel(labelName)) {
                addedLabels.remove(labelName);
            } else {
                // add new label
                removeConflictingLabels(labelName);
                addedLabels.add(labelName);
            }
        }
    }

    /**
     * Updates the list of labels which labels' names contain the current query.
     * This list of labels can then be retrieved later via getMatchedLabels()
     *
     * The suggestion index will be pointed to the first label that fits the query, if there is.
     *
     * @param query
     */
    private void updateMatchedLabels(String query) {
        TurboLabel queryLabel = new TurboLabel("", query);
        List<TurboLabel> newMatchedLabels = repoLabels;

        newMatchedLabels = TurboLabel.filterByNameQuery(newMatchedLabels, queryLabel.getSimpleName());
        newMatchedLabels = TurboLabel.filterByGroupQuery(newMatchedLabels, queryLabel.getGroupName());

        if (query.isEmpty() || newMatchedLabels.isEmpty()) {
            currentSuggestionIndex = OptionalInt.empty();
        } else {
            currentSuggestionIndex = OptionalInt.of(0);
        }

        matchedLabels = TurboLabel.getLabelsNameList(newMatchedLabels);
    }

    /*
     * Methods for getting the resulting labels
     */

    /**
     * Returns the final list of labels to be assigned
     * @return
     */
    public List<String> getAssignedLabels() {
        List<String> assignedLabels = new ArrayList<>();
        assignedLabels.addAll(initialLabels);
        assignedLabels.addAll(addedLabels);
        assignedLabels.removeAll(removedLabels);
        return assignedLabels;
    }

    /**
     * Returns the initial list of labels
     * @return
     */
    public List<String> getInitialLabels() {
        return convertToList(initialLabels);
    }

    /**
     * Returns the list of initial labels that have been removed
     * @return
     */
    public List<String> getRemovedLabels() {
        return removedLabels;
    }

    /**
     * Returns the list of labels that are newly added
     * @return
     */
    public List<String> getAddedLabels() {
        return addedLabels;
    }

    /**
     * Returns the name of the suggested label, if it exists
     * @return
     */
    public Optional<String> getCurrentSuggestion() {
        if (currentSuggestionIndex.isPresent()) {
            assert isValidSuggestionIndex();
            return Optional.of(getSuggestedLabel());
        }
        return Optional.empty();
    }

    /**
     * Returns the names of the labels that match the current query
     * @return
     */
    public List<String> getMatchedLabels() {
        return matchedLabels;
    }

    /*
     * Non-static Helper functions
     */

    private void removeConflictingLabels(String labelName) {
        TurboLabel queryLabel = new TurboLabel("", labelName);
        if (!queryLabel.isInExclusiveGroup()) return;

        String group = queryLabel.getGroupName();
        // Remove from addedLabels
        addedLabels = addedLabels.stream()
                .filter(label -> !new TurboLabel("", label).getGroupName().equals(group))
                .collect(Collectors.toList());

        // Add to removedLabels all initialLabels that have conflicting group
        removedLabels.addAll(initialLabels.stream()
                .filter(label -> new TurboLabel("", label).getGroupName().equals(group) 
                    && !removedLabels.contains(labelName))
                .collect(Collectors.toList()));
    }

    private boolean isAnInitialLabel(String name) {
        return this.initialLabels.contains(name);
    }

    private boolean isAnAddedLabel(String name) {
        return this.addedLabels.contains(name);
    }

    private boolean isARemovedLabel(String name) {
        return this.removedLabels.contains(name);
    }

    private String getSuggestedLabel() {
        return matchedLabels.get(currentSuggestionIndex.getAsInt());
    }

    private boolean isValidSuggestionIndex() {
        return currentSuggestionIndex.getAsInt() >= 0 && currentSuggestionIndex.getAsInt() < matchedLabels.size();
    }

    /*
     * Static Helper functions
     */

    private static List<String> getConfirmedKeywords(String userInput) {
        ArrayList<String> confirmedKeywords = new ArrayList<>();

        String[] keywords = userInput.split("\\s+");
        for (int i = 0; i < keywords.length; i++) {
            if (isConfirmedKeyword(userInput, i)) {
                confirmedKeywords.add(keywords[i]);
            }
        }

        return confirmedKeywords;
    }

    private static Optional<String> getKeywordInProgress(String userInput) {
        String[] keywords = userInput.split("\\s+");
        if (keywords.length == 0) return Optional.empty();

        if (isConfirmedKeyword(userInput, keywords.length - 1)) return Optional.empty();

        return Optional.of(keywords[keywords.length - 1]);
    }

    /**
     * Determines if the keywordIndex-th keyword is confirmed i.e. user has typed a space after it
     * Assumption: userInput has at at least keywordIndex+1 keywords, separated by whitespace.
     * @param userInput
     * @param keywordIndex
     * @return
     */
    private static boolean isConfirmedKeyword(String userInput, int keywordIndex) {
        return !(keywordIndex == userInput.split("\\s+").length - 1 && !userInput.endsWith(" "));
    }

    private static List<String> convertToList(Set<String> labelSet){
        return new ArrayList<>(labelSet);
    }


}
