package ui.components.pickers;

import backend.resource.TurboLabel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to represent the state of the label picker. In addition to representing a state,
 * it contains the logic which handles toggling/highlighting of labels and simplifies the retrieval of
 * the resulting status for UI to display.
 */
public class LabelPickerState {
    Set<String> initialLabels;
    List<String> addedLabels;
    List<String> removedLabels;
    List<String> matchedLabels;
    List<String> repoLabels;
    OptionalInt currentSuggestionIndex;

    public LabelPickerState(Set<String> initialLabels, List<String> repoLabels) {
        this(initialLabels, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), repoLabels,
                OptionalInt.empty());

        this.updateMatchedLabels("");
    }

    private LabelPickerState(Set<String> initialLabels, List<String> addedLabels, List<String> removedLabels,
                             List<String> matchedLabels, List<String> repoLabels) {
        this(initialLabels, addedLabels, removedLabels, matchedLabels, repoLabels, OptionalInt.empty());
    }

    private LabelPickerState(Set<String> initialLabels, List<String> addedLabels, List<String> removedLabels,
                             List<String> matchedLabels, List<String> repoLabels, OptionalInt currentSuggestionIndex) {
        this.initialLabels = initialLabels;
        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.matchedLabels = matchedLabels;
        this.repoLabels = repoLabels;
        this.currentSuggestionIndex = currentSuggestionIndex;
    }

    /**
     * Determines resulting state based on given user input
     *
     * @param userInput 
     * @return new state that corresponds with the user input
     */
    public LabelPickerState determineState(String userInput) {
        LabelPickerState state = this;
        List<String> confirmedKeywords = getConfirmedKeywords(userInput);
        for (String confirmedKeyword : confirmedKeywords) {
            state = state.toggleLabel(confirmedKeyword);
        }

        Optional<String> keywordInProgess = getKeywordInProgress(userInput);
        if (keywordInProgess.isPresent()) {
            state = state.updateMatchedLabels(keywordInProgess.get());
        }

        return state;
    }

    /**
     * Gives a new state with the label that contains keyword toggled.
     *
     * This will simply return the same state if there are more than 1
     * labels that contain the keyword
     *
     * keyword is case-insensitive
     *
     * @param keyword
     * @return
     */
    public LabelPickerState toggleLabel(String keyword) {
        if (!TurboLabel.hasExactlyOneMatchedLabel(repoLabels, keyword)) return this;
        String labelName = TurboLabel.getMatchedLabelName(repoLabels, keyword);

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

        return new LabelPickerState(initialLabels, addedLabels, removedLabels, repoLabels, repoLabels);
    }

    /**
     * Updates the list of labels which labels' names contain the current query.
     * This list of labels can then be retrieved later via getMatchedLabels()
     *
     * The suggestion index will be pointed to the first label that fits the query, if there is.
     *
     * @param query
     * @return
     */
    private LabelPickerState updateMatchedLabels(String query) {
        List<String> newMatchedLabels = repoLabels;

        newMatchedLabels = TurboLabel.filterByPartialName(newMatchedLabels, TurboLabel.getName(query));
        newMatchedLabels = TurboLabel.filterByPartialGroupName(newMatchedLabels, TurboLabel.getGroup(query));

        OptionalInt newSuggestionIndex;
        if (query.isEmpty() || newMatchedLabels.isEmpty()) {
            newSuggestionIndex = OptionalInt.empty();
        } else {
            newSuggestionIndex = OptionalInt.of(0);
        }

        return new LabelPickerState(initialLabels, addedLabels, removedLabels, newMatchedLabels, repoLabels,
                newSuggestionIndex);
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

    private void removeConflictingLabels(String name) {
        if (!TurboLabel.hasExclusiveGroup(name)) return;

        String group = TurboLabel.getGroup(name);
        // Remove from addedLabels
        addedLabels = addedLabels.stream()
                .filter(label -> !TurboLabel.getGroup(label).equals(group))
                .collect(Collectors.toList());

        // Add to removedLabels all initialLabels that have conflicting group
        removedLabels.addAll(initialLabels.stream()
                .filter(label -> TurboLabel.getGroup(label).equals(group) && !removedLabels.contains(name))
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
