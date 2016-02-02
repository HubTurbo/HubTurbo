package ui.components.pickers;

import backend.resource.TurboLabel;
import util.Utility;

import java.util.*;
import java.util.stream.Collectors;


public class LabelPickerState {
    Set<String> addedLabels;
    Set<String> removedLabels;
    Set<String> initialLabels;
    List<String> matchedLabels;
    OptionalInt currentSuggestionIndex;

    private LabelPickerState(Set<String> initialLabels,
                             Set<String> addedLabels,
                             Set<String> removedLabels,
                             List<String> matchedLabels,
                             OptionalInt currentSuggestionIndex) {
        this.initialLabels = initialLabels;
        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.matchedLabels = matchedLabels;
        this.currentSuggestionIndex = currentSuggestionIndex;
    }

    private LabelPickerState(Set<String> initialLabels,
                             Set<String> addedLabels,
                             Set<String> removedLabels,
                             List<String> matchedLabels) {
        this.initialLabels = initialLabels;
        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.matchedLabels = matchedLabels;
        this.currentSuggestionIndex = OptionalInt.empty();
    }

    /*
     * Methods for Logic
     */

    public LabelPickerState(Set<String> initialLabels) {
        this.initialLabels = initialLabels;
        this.addedLabels = new HashSet<>();
        this.removedLabels = new HashSet<>();
        this.matchedLabels = new ArrayList<>();
        this.currentSuggestionIndex = OptionalInt.empty();
    }

    public LabelPickerState clearMatchedLabels() {
        return new LabelPickerState(initialLabels, addedLabels, removedLabels,
                    new ArrayList<>(), currentSuggestionIndex);
    }

    public LabelPickerState toggleLabel(Set<String> repoLabels, String keyword) {
        if (!hasExactlyOneMatchedLabel(repoLabels, keyword)) return this;
        String labelName = getMatchedLabelName(repoLabels, keyword);

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

        return new LabelPickerState(initialLabels, addedLabels, removedLabels, new ArrayList<>());
    }

    public LabelPickerState nextSuggestion() {
        if (canIncreaseIndex()) {
            return new LabelPickerState(initialLabels, removedLabels, addedLabels,
                    matchedLabels, OptionalInt.of(currentSuggestionIndex.getAsInt() + 1));
        }
        return this;
    }

    public LabelPickerState previousSuggestion() {
        if (canDecreaseIndex()) {
            return new LabelPickerState(initialLabels, removedLabels, addedLabels,
                        matchedLabels, OptionalInt.of(currentSuggestionIndex.getAsInt() - 1));
        }
        return this;
    }

    public LabelPickerState updateMatchedLabels(Set<String> repoLabels, String query) {
        List<String> newMatchedLabels = convertToList(repoLabels);

        newMatchedLabels = filterByName(newMatchedLabels, getName(query));
        newMatchedLabels = filterByGroup(newMatchedLabels, getGroup(query));

        OptionalInt newSuggestionIndex;
        if (query.equals("") || newMatchedLabels.isEmpty()) {
            newSuggestionIndex = OptionalInt.empty();
        } else {
            newSuggestionIndex = OptionalInt.of(0);
        }

        return new LabelPickerState(initialLabels, addedLabels, removedLabels, newMatchedLabels, newSuggestionIndex);
    }

    /*
     * Methods for updating UI
     */

    public List<String> getAssignedLabels() {
        List<String> assignedLabels = new ArrayList<>();
        assignedLabels.addAll(initialLabels);
        assignedLabels.addAll(addedLabels);
        assignedLabels.removeAll(removedLabels);
        return assignedLabels;
    }

    public List<String> getInitialLabels() {
        return convertToList(initialLabels);
    }

    public List<String> getRemovedLabels() {
        return convertToList(removedLabels);
    }

    public List<String> getAddedLabels() {
        return convertToList(addedLabels);
    }

    public Optional<String> getCurrentSuggestion() {
        if (currentSuggestionIndex.isPresent() && isValidSuggestionIndex()) {
            return Optional.of(getSuggestedLabel());
        }
        return Optional.empty();
    }

    public List<String> getMatchedLabels() {
        return matchedLabels;
    }

    /*
     * Helper functions
     */

    private String getMatchedLabelName(Set<String> repoLabels, String keyword) {
        List<String> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByName(newMatchedLabels, getName(keyword));
        newMatchedLabels = filterByGroup(newMatchedLabels, getGroup(keyword));
        return newMatchedLabels.get(0);
    }

    private boolean hasExactlyOneMatchedLabel(Set<String> repoLabels, String keyword) {
        List<String> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByName(newMatchedLabels, getName(keyword));
        newMatchedLabels = filterByGroup(newMatchedLabels, getGroup(keyword));
        return newMatchedLabels.size() == 1;
    }

    private void removeConflictingLabels(String name) {
        if (hasExclusiveGroup(name)) {
            String group = getGroup(name);
            // Remove from addedLabels
            addedLabels = addedLabels.stream()
                    .filter(label -> {
                        String labelGroup = getGroup(label);
                        return !labelGroup.equals(group);
                    })
                    .collect(Collectors.toSet());

            // Add to removedLabels all initialLabels that have conflicting group
            removedLabels.addAll(initialLabels.stream()
                    .filter(label -> {
                        String labelGroup = getGroup(label);
                        return labelGroup.equals(group) && !removedLabels.contains(name);
                    })
                    .collect(Collectors.toSet()));
        }
    }

    private boolean hasExclusiveGroup(String name) {
        return TurboLabel.getDelimiter(name).isPresent() && TurboLabel.getDelimiter(name).get().equals(".");
    }

    private List<String> filterByName(List<String> repoLabels, String labelName) {
        return repoLabels
                .stream()
                .filter(name -> Utility.containsIgnoreCase(getName(name), labelName))
                .collect(Collectors.toList());
    }

    private List<String> filterByGroup(List<String> repoLabels, String labelGroup) {
        if (labelGroup.equals("")) {
            return repoLabels;
        } else {
            return repoLabels
                    .stream()
                    .filter(name -> {
                        if (hasGroup(name)) {
                            return Utility.containsIgnoreCase(getGroup(name), labelGroup);
                        } else {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    private String getGroup(String name) {
        if (hasGroup(name)) {
            return name.substring(0, name.indexOf(TurboLabel.getDelimiter(name).get()));
        } else {
            return "";
        }
    }

    private String getName(String name) {
        if (hasGroup(name)) {
            return name.substring(name.indexOf(TurboLabel.getDelimiter(name).get()) + 1);
        } else {
            return name;
        }
    }

    private boolean hasGroup(String name) {
        return TurboLabel.getDelimiter(name).isPresent();
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

    private boolean canIncreaseIndex() {
        return currentSuggestionIndex.isPresent() && currentSuggestionIndex.getAsInt() < matchedLabels.size() - 1;
    }

    private boolean canDecreaseIndex() {
        return currentSuggestionIndex.isPresent() && currentSuggestionIndex.getAsInt() > 0;
    }

    private List<String> convertToList(Set<String> labelSet){
        List<String> resultingList = new ArrayList<>();
        resultingList.addAll(labelSet);
        return resultingList;
    }

}