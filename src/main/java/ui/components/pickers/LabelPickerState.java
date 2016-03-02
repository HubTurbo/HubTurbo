package ui.components.pickers;

import backend.resource.TurboLabel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to represent the state of the label picker. In addition to representing a state,
 * it contains the logic that handles every state transition
 */
public class LabelPickerState {

    private Set<String> initialLabels;
    private List<String> addedLabels;
    private List<String> removedLabels;
    private List<String> matchedLabels;
    private List<TurboLabel> allLabels;
    private OptionalInt currentSuggestionIndex;

    public LabelPickerState(Set<String> initialLabels, List<TurboLabel> allLabels, String userInput) {
        this(initialLabels, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), allLabels,
                OptionalInt.empty());
        update(userInput);
    }

    private LabelPickerState(Set<String> initialLabels, List<String> addedLabels, List<String> removedLabels,
                             List<String> matchedLabels, List<TurboLabel> allLabels, 
                             OptionalInt currentSuggestionIndex) {
        this.initialLabels = initialLabels;
        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.matchedLabels = matchedLabels;
        this.allLabels = allLabels;
        this.currentSuggestionIndex = currentSuggestionIndex;
    }

    /**
     * Updates current state based on given user input
     * @param userInput 
     */
    private final void update(String userInput) {
        List<String> confirmedKeywords = getConfirmedKeywords(userInput);
        for (String confirmedKeyword : confirmedKeywords) {
            updateIfMatchesLabel(confirmedKeyword);
        }

        Optional<String> keywordInProgess = getKeywordInProgress(userInput);
        if (keywordInProgess.isPresent()) {
            updateMatchedLabels(keywordInProgess.get());
            updateSuggestionIndex(keywordInProgess.get(), matchedLabels);
        }
    }

    /**
     * Updates current state if there is at least one matching label based on the 
     * given keyword
     * @param keyword
     */
    private final void updateIfMatchesLabel(String keyword) {
        if (TurboLabel.hasMatchedLabel(allLabels, keyword)) {
            updateAssignedLabels(TurboLabel.getFirstMatchingTurboLabel(allLabels, keyword));
        }
    }

    /**
     * Updates assignedLabels based on properties of a label 
     * @param label
     */
    public final void updateAssignedLabels(TurboLabel label) {
        String labelName = label.getFullName();

        if (isAnInitialLabel(labelName)) {
            if (isARemovedLabel(labelName)) {
                removeConflictingLabels(label);
                removedLabels.remove(labelName);
            } else {
                removedLabels.add(labelName);
            }
        } else {
            if (isAnAddedLabel(labelName)) {
                addedLabels.remove(labelName);
            } else {
                // add new label
                removeConflictingLabels(label);
                addedLabels.add(labelName);
            }
        }
    }

    /**
     * Updates the list of labels which labels' names contain the current keyword.
     * This list of labels can then be retrieved later via getMatchedLabels()
     * @param keyword
     */
    private final void updateMatchedLabels(String keyword) {
        List<TurboLabel> newMatchedLabels = TurboLabel.getMatchedLabels(allLabels, keyword);
        matchedLabels = TurboLabel.getLabelsNameList(newMatchedLabels);
    }

    /**
     * Updates suggeston index to first label that matches the keyword
     * @param keyword
     * @param newMatchedLabels
     */
    private final void updateSuggestionIndex(String keyword, List<String> newMatchedLabels) {
        if (keyword.isEmpty() || newMatchedLabels.isEmpty()) {
            currentSuggestionIndex = OptionalInt.empty();
        } else {
            currentSuggestionIndex = OptionalInt.of(0);
        }
    }

    /*
     * Methods for getting the resulting labels
     */

    /**
     * @return final list of labels to be assigned
     */
    public List<String> getAssignedLabels() {
        List<String> assignedLabels = new ArrayList<>();
        assignedLabels.addAll(initialLabels);
        assignedLabels.addAll(addedLabels);
        assignedLabels.removeAll(removedLabels);
        return assignedLabels;
    }

    /**
     * @return the initial list of labels
     */
    public List<String> getInitialLabels() {
        return new ArrayList<>(initialLabels);
    }

    /**
     * @return the list of initial labels that have been removed
     */
    public List<String> getRemovedLabels() {
        return removedLabels;
    }

    /**
     * @return the list of labels that are newly added
     */
    public List<String> getAddedLabels() {
        return addedLabels;
    }

    /**
     * @return the name of the suggested label, if it exists
     */
    public Optional<String> getCurrentSuggestion() {
        if (currentSuggestionIndex.isPresent()) {
            assert isValidSuggestionIndex();
            return Optional.of(getSuggestedLabel());
        }
        return Optional.empty();
    }

    /**
     * @return the names of the labels that match the current query
     */
    public List<String> getMatchedLabels() {
        return matchedLabels;
    }

    /**
     * Removes labels that belong to the same group as the given label
     * @param label
     */
    private void removeConflictingLabels(TurboLabel label) {
        if (!label.isInExclusiveGroup()) return;

        String group = label.getGroupName();
        // Remove from addedLabels
        addedLabels = addedLabels.stream()
                .filter(name -> !new TurboLabel("", name).getGroupName().equals(group))
                .collect(Collectors.toList());

        // Add to removedLabels all initialLabels that have conflicting group
        removedLabels.addAll(initialLabels.stream()
                .filter(name -> new TurboLabel("", name).getGroupName().equals(group) 
                    && !removedLabels.contains(label.getFullName()))
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

    /**
     * @return suggested label 
     */
    private String getSuggestedLabel() {
        return matchedLabels.get(currentSuggestionIndex.getAsInt());
    }

    /**
     * @return true if a suggestion index is positive and within the list of matched labels
     */
    private boolean isValidSuggestionIndex() {
        return currentSuggestionIndex.getAsInt() >= 0 && currentSuggestionIndex.getAsInt() < matchedLabels.size();
    }


    /**
     * @param userInput
     * @return list of confirmed keywords based on given userInput
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

    /**
     * If userInput does not end with space, split by space and return last word.
     * @param userInput
     * @return the keyword in progress based on the userInput
     */
    private static Optional<String> getKeywordInProgress(String userInput) {
        String[] keywords = userInput.split("\\s+");
        if (keywords.length == 0) return Optional.empty();

        if (isConfirmedKeyword(userInput, keywords.length - 1)) return Optional.empty();

        return Optional.of(keywords[keywords.length - 1]);
    }

    /**
     * Determines if the keywordIndex-th keyword is confirmed i.e. user has typed a space after it
     * Assumption: userInput has at least keywordIndex+1 keywords, separated by whitespace.
     * @param userInput
     * @param keywordIndex
     * @return
     */
    private static boolean isConfirmedKeyword(String userInput, int keywordIndex) {
        return keywordIndex != userInput.split("\\s+").length - 1 || userInput.endsWith(" ");
    }
}
