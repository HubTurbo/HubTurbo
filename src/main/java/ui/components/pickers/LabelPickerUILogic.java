package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import util.Utility;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles the logic of the label picker UI
 *
 * However, this logic assumes that the user only uses one form of input
 * i.e. only clicking or only typing. Combination of the two may lead to
 * unexpected behaviour. Typing should also not involve any actions other than
 * appending a character or backspacing.
 */
public class LabelPickerUILogic {

    private final TurboIssue issue;
    private final LabelPickerDialog dialog;
    private List<TurboLabel> repoLabels;
    private final List<PickerLabel> modificationLabels = new ArrayList<>(); // refers to the top pane labels
                                                                            // which reflects the modifications made
    private List<PickerLabel> matchingLabels;                               // refers to the bottom pane labels
    private final Map<String, Boolean> groups = new HashMap<>();
    private final Map<String, Boolean> activeLabels = new HashMap<>();
    private Optional<String> possibleLabel = Optional.empty();              // refers to the faded label in top pane

    // variables to handle text field changes
    private int previousTextFieldLength = 0;
    private Character previousLastChar = null;
    private final Stack<String> stackOfSpecifiedLabels = new Stack<>();
    private final Stack<ArrayList<String>> stackOfRemovedExclusiveLabels = new Stack<>();


    LabelPickerUILogic(TurboIssue issue, List<TurboLabel> repoLabels, LabelPickerDialog dialog) {
        this.issue = issue;
        this.dialog = dialog;
        populateAllLabels(repoLabels);
        addExistingLabels();
        resetMatchingLabels();
        populatePanes();
    }

    public LabelPickerUILogic(TurboIssue issue, List<TurboLabel> repoLabels) {
        this.issue = issue;
        this.dialog = null;
        populateAllLabels(repoLabels);
        addExistingLabels();
        resetMatchingLabels();
        populatePanes();
    }

    private void populateAllLabels(List<TurboLabel> repoLabels) {
        this.repoLabels = new ArrayList<>(repoLabels);
        Collections.sort(this.repoLabels);
        // populate activeLabels by going through repoLabels and seeing which ones currently exist
        // in issue.getLabels()
        repoLabels.forEach(label -> {
            // matching with exact labels so no need to worry about capitalisation
            activeLabels.put(label.getActualName(), isExistingLabel(label.getActualName()));
            if (label.getGroup().isPresent() && !groups.containsKey(label.getGroup().get())) {
                groups.put(label.getGroup().get(), label.isExclusive());
            }
        });
    }

    private void populatePanes() {
        if (dialog != null) dialog.populatePanes(getExistingLabels(), getAddedLabels(), matchingLabels, groups);
    }

    /**
     * Toggles the label of the specified name
     *
     * This should be called when the user clicks on a label
     *
     * @param name
     * @return list of label names that were removed due to exclusive grouping
     */
    public ArrayList<String> toggleLabel(String name) {
        removePossibleLabel();
        ArrayList<String> removedLabels = toggleModificationLabel(name);
        resetMatchingLabels();
        populatePanes();
        return removedLabels;
    }

    /**
     * Toggles the currently highlighted label
     *
     * This is currently used when the user types space in the text field
     * or the confirm button is clicked whilst there is a highlighted label
     *
     * @return list of label names that were removed due to exclusive grouping
     */
    public ArrayList<String> toggleHighlightedLabel() {
        return toggleLabel(getHighlightedLabelName());
    }

    @SuppressWarnings("unused")
    private void ______TEXT_FIELD______() {}

    /**
     * This method should be called after every change in the label picker text field.
     * May not function as intended if the user clicks and types in the same label picker session.
     *
     * @param text updated text field
     */
    public void processTextFieldChange(String text) {
        if (isEmpty(text)) {
            // since there is no toggling of labels involved
            // we just have to clear any UI changes due to query
            handleNoQuery();
        } else if (isCharAdded(text)) {
            handleCharAddition(text);
        } else {
            handleCharRemoval(text);
        }

        updatePreviousTextFieldInfo(text);
        populatePanes();
    }

    private void handleCharAddition(String text) {
         if (isLastCharSpace(text)) {
            toggleHighlightedLabelAndUpdateStacks();
        } else {
            handleQuery(getLastWord(text));
        }
    }

    private void toggleHighlightedLabelAndUpdateStacks() {
        if (hasHighlightedLabel()) {
            stackOfSpecifiedLabels.push(getHighlightedLabelName());
            stackOfRemovedExclusiveLabels.push(toggleHighlightedLabel());
        } else {
            stackOfSpecifiedLabels.push(null);
            stackOfRemovedExclusiveLabels.push(null);
        }
    }

    private void handleQuery(String lastWord) {
        updateMatchingLabelsWithRawQuery(lastWord);
        updatePossibleLabel();
    }


    private void handleCharRemoval(String text) {
        if (isWordRemoved(text)) {
            undoPreviousSpaceToggle();
        }

        if (isLastCharSpace(text)) {
            handleNoQuery();
        } else {
            handleQuery(getLastWord(text));
        }
    }

    private void handleNoQuery() {
        removePossibleLabel();
        resetMatchingLabels();
    }

    private void resetMatchingLabels() {
        updateMatchingLabels("");
    }

    private void undoPreviousSpaceToggle() {
        toggleRecentlySpecifiedLabel();
        restoreRecentlyRemovedLabels();
    }

    /**
     * Untoggles last label that was specified
     * This may do nothing if the last label added was null (i.e. added without any highlighted label)
     */
    private void toggleRecentlySpecifiedLabel() {
        String lastSpecifiedLabel = stackOfSpecifiedLabels.pop();
        if (lastSpecifiedLabel != null) {
            // if the last label is mapped to an actual label
            updateModificationLabels(lastSpecifiedLabel, !activeLabels.get(lastSpecifiedLabel));
        }
    }

    /**
     * Restores the most recent set of removed label(s) due to exclusive grouping
     */
    private void restoreRecentlyRemovedLabels() {
        ArrayList<String> listOfRemovedLabels = stackOfRemovedExclusiveLabels.pop();
        if (listOfRemovedLabels != null) {
            listOfRemovedLabels.stream()
                    .forEach(labelName -> updateModificationLabels(labelName, true));
        }
    }

    private boolean isEmpty(String text) {
        return text.length() == 0;
    }

    private boolean isCharAdded(String text) {
        return text.length() > previousTextFieldLength;
    }

    private void updatePreviousTextFieldInfo(String text) {
        previousTextFieldLength = text.length();
        previousLastChar = previousTextFieldLength > 0 ? text.charAt(previousTextFieldLength - 1) : null;
    }

    private String getLastWord(String text) {
        String[] textArray = text.trim().split(" ");
        return textArray[textArray.length - 1];
    }

    private boolean isLastCharSpace(String text) {
        char lastChar = text.charAt(text.length() - 1);
        return isSpace(lastChar);
    }

    private boolean isSpace(char lastChar) {
        return lastChar == ' ';
    }

    // Assumptions: text is not empty or null
    private boolean isWordRemoved(String text) {
        return !isLastCharSpace(text) && isSpace(previousLastChar) && !stackOfSpecifiedLabels.isEmpty();
    }

    /*
    * Top pane methods do not need to worry about capitalisation because they
    * all deal with actual labels.
    */
    @SuppressWarnings("unused")
    private void ______TOP_PANE______() {}

    private void addExistingLabels() {
        // used once to populate modificationLabels at the start
        repoLabels.stream()
                .filter(label -> isExistingLabel(label.getActualName()))
                .forEach(label -> modificationLabels.add(new PickerLabel(label, this, true)));
    }

    /**
     * Toggles modification label that matches 'name'
     *
     * @param name
     * @return list of removed label names due to exclusive grouping
     */
    private ArrayList<String> toggleModificationLabel(String name) {
        ArrayList<String> listOfRemovedNames = new ArrayList<>();
        Optional<TurboLabel> turboLabel =
                repoLabels.stream().filter(label -> label.getActualName().equals(name)).findFirst();
        if (turboLabel.isPresent()) {
            if (turboLabel.get().isExclusive() && !isActiveLabel(name)) {
                removeConflictingExclusiveLabels(turboLabel, listOfRemovedNames);
            }
            updateModificationLabels(name, !isActiveLabel(name));
        }
        return listOfRemovedNames;
    }

    /**
     * Removes any conflicting exclusive labels from top pane and adds them to listOfRemovedNames
     * @param turboLabel
     * @param listOfRemovedNames
     */
    private void removeConflictingExclusiveLabels(Optional<TurboLabel> turboLabel,
                                                  ArrayList<String> listOfRemovedNames) {
        String group = turboLabel.get().getGroup().get();
        repoLabels
                .stream()
                .filter(TurboLabel::isExclusive)
                .filter(label -> label.getGroup().get().equals(group))
                .forEach(label -> {
                    if (isActiveLabel(label.getActualName())) {
                        listOfRemovedNames.add(label.getActualName());
                    }
                    updateModificationLabels(label.getActualName(), false);
                });
    }

    private void updateModificationLabels(String name, boolean isAdd) {
        // adds new labels to the end of the list
        activeLabels.put(name, isAdd); // update activeLabels first
        if (isAdd) {
            if (isExistingLabel(name)) {
                modificationLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .forEach(label -> {
                            label.setIsRemoved(false);
                            label.setIsFaded(false);
                        });
            } else {
                repoLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .filter(label -> activeLabels.get(label.getActualName()))
                        .filter(label -> !isInModificationLabels(label.getActualName()))
                        .findFirst()
                        .ifPresent(label -> modificationLabels.add(new PickerLabel(label, this, true)));
            }
        } else {
            modificationLabels.stream()
                    .filter(label -> label.getActualName().equals(name))
                    .findFirst()
                    .ifPresent(label -> {
                        if (isExistingLabel(name)) {
                            label.setIsRemoved(true);
                        } else {
                            modificationLabels.remove(label);
                        }
                    });
        }
    }

    private boolean isInModificationLabels(String name) {
        // used to prevent duplicates in modificationLabels
        return modificationLabels.stream()
                .filter(label -> label.getActualName().equals(name))
                .findAny()
                .isPresent();
    }

    /**
     * Updates the possible label based on the current highlighted label
     */
    private void updatePossibleLabel() {
        removePossibleLabel();
        addPossibleLabel();
    }

    /**
     * Reverts any changes to the UI due to existing query
     */
    private void removePossibleLabel() {
        // Deletes previous possible label
        if (possibleLabel.isPresent()) {
            modificationLabels.stream()
                    .filter(label -> label.getActualName().equals(possibleLabel.get()))
                    .findFirst()
                    .ifPresent(label -> {
                        if (isExistingLabel(label)) {
                            if (label.isRemoved()) {
                                label.setIsFaded(false);
                                label.setIsRemoved(false);
                            } else {
                                label.setIsFaded(false);
                                label.setIsRemoved(true);
                            }
                        } else {
                            if (label.isRemoved()) {
                                label.setIsFaded(false);
                                label.setIsRemoved(false);
                            } else {
                                modificationLabels.remove(label);
                            }
                        }
                    });
            possibleLabel = Optional.empty();
        }
    }

    /**
     * This adds a faded label to top pane if there is an ongoing query
     *
     * Assumptions: Any possible label has been cleared beforehand, if not
     * it might not work as intended
     */
    private void addPossibleLabel() {
        if (hasHighlightedLabel()) {
            //something is highlighted, try to add possible label
            TurboLabel highlightedLabel = getHighlightedLabel().get();
            String highlightedLabelName = highlightedLabel.getActualName();
            if (isExistingLabel(highlightedLabelName)) {
                modificationLabels.stream()
                        .filter(label -> label.getActualName().equals(highlightedLabelName))
                        .findFirst()
                        .ifPresent(label -> {
                            if (label.isRemoved()) {
                                label.setIsRemoved(false);
                                label.setIsFaded(true);
                            } else {
                                label.setIsRemoved(true);
                                label.setIsFaded(true);
                            }
                        });
            } else {
                if (isInModificationLabels(highlightedLabelName)) {
                    // find the label and remove it
                    modificationLabels.stream()
                            .filter(label -> label.getActualName().equals(highlightedLabelName))
                            .findFirst()
                            .ifPresent(label -> {
                                label.setIsRemoved(true);
                                label.setIsFaded(true);
                            });
                } else {
                    modificationLabels.add(new PickerLabel(highlightedLabel, this, false, true, false, true, true));
                }
            }

            possibleLabel = Optional.of(highlightedLabelName);
        }
    }

    // Bottom box deals with possible matches so we usually ignore the case for these methods.
    @SuppressWarnings("unused")
    private void ______BOTTOM_BOX______() {}

    /**
     * Updates the bottom labels given a raw query entered by the user
     * This query should be taken from the last word of the user input (after a space)
     */
    private void updateMatchingLabelsWithRawQuery(String query) {
        if (TurboLabel.getDelimiter(query).isPresent()) {
            String delimiter = TurboLabel.getDelimiter(query).get();
            String[] queryArray = query.split(Pattern.quote(delimiter));

            if (queryArray.length == 1) {
                updateMatchingLabels(queryArray[0], "");
            } else if (queryArray.length == 2) {
                updateMatchingLabels(queryArray[0], queryArray[1]);
            }
        } else {
            updateMatchingLabels(query);
        }
    }

    private void updateMatchingLabels(String group, String match) {
        List<String> groupNames = groups.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        boolean isValidGroup = groupNames.stream()
                .filter(validGroup -> Utility.startsWithIgnoreCase(validGroup, group))
                .findAny()
                .isPresent();

        if (isValidGroup) {
            List<String> validGroups = groupNames.stream()
                    .filter(validGroup -> Utility.startsWithIgnoreCase(validGroup, group))
                    .collect(Collectors.toList());
            // get all labels that contain search query
            // fade out labels which do not match
            matchingLabels = repoLabels
                    .stream()
                    .map(label -> new PickerLabel(label, this, false))
                    .map(label -> {
                        if (activeLabels.get(label.getActualName())) {
                            label.setIsSelected(true); // add tick if selected
                        }
                        if (!label.getGroup().isPresent() ||
                                !validGroups.contains(label.getGroup().get()) ||
                                !Utility.containsIgnoreCase(label.getName(), match)) {
                            label.setIsFaded(true); // fade out if does not match search query
                        }
                        return label;
                    })
                    .collect(Collectors.toList());
            if (!matchingLabels.isEmpty()) highlightFirstMatchingItem(match);
        } else {
            updateMatchingLabels(match);
        }
    }

    private void updateMatchingLabels(String match) {
        // get all labels that contain search query
        // fade out labels which do not match
        matchingLabels = repoLabels
                .stream()
                .map(label -> new PickerLabel(label, this, false))
                .map(label -> {
                    if (activeLabels.get(label.getActualName())) {
                        label.setIsSelected(true); // add tick if selected
                    }
                    if (!match.isEmpty() && !Utility.containsIgnoreCase(label.getActualName(), match)) {
                        label.setIsFaded(true); // fade out if does not match search query
                    }
                    return label;
                })
                .collect(Collectors.toList());

        if (!match.isEmpty() && !matchingLabels.isEmpty()) highlightFirstMatchingItem(match);
    }

    /**
     * Moves the highlight to the next label that fits the current query
     *
     * @param isDown
     */
    public void moveHighlightOnLabel(boolean isDown) {
        if (hasHighlightedLabel()) {
            // used to move the highlight on the matching labels
            // find all matching labels
            List<PickerLabel> matchingLabels = this.matchingLabels.stream()
                    .filter(label -> !label.isFaded())
                    .collect(Collectors.toList());

            // move highlight around
            for (int i = 0; i < matchingLabels.size(); i++) {
                if (matchingLabels.get(i).isHighlighted()) {
                    if (isDown && i < matchingLabels.size() - 1) {
                        matchingLabels.get(i).setIsHighlighted(false);
                        matchingLabels.get(i + 1).setIsHighlighted(true);
                        updatePossibleLabel();
                    } else if (!isDown && i > 0) {
                        matchingLabels.get(i - 1).setIsHighlighted(true);
                        matchingLabels.get(i).setIsHighlighted(false);
                        updatePossibleLabel();
                    }
                    populatePanes();
                    return;
                }
            }
        }
    }

    private void highlightFirstMatchingItem(String match) {
        List<PickerLabel> matches = matchingLabels.stream()
                .filter(label -> !label.isFaded())
                .collect(Collectors.toList());

        // try to highlight labels that begin with match first
        matches.stream()
                .filter(label -> Utility.startsWithIgnoreCase(label.getName(), match))
                .findFirst()
                .ifPresent(label -> label.setIsHighlighted(true));

        // if not then highlight first matching label
        if (!hasHighlightedLabel()) {
            matches.stream()
                    .findFirst()
                    .ifPresent(label -> label.setIsHighlighted(true));
        }
    }

    public boolean hasHighlightedLabel() {
        return matchingLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny()
                .isPresent();
    }

    private Optional<PickerLabel> getHighlightedLabel() {
        return matchingLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny();
    }

    private String getHighlightedLabelName() {
        return getHighlightedLabel().get().getActualName();
    }

    @SuppressWarnings("unused")
    private void ______BOILERPLATE______() {}

    public Map<String, Boolean> getActiveLabels() {
        return activeLabels;
    }

    private List<PickerLabel> getExistingLabels() {
        return modificationLabels.stream()
                .filter(label -> isExistingLabel(label.getActualName()))
                .collect(Collectors.toList());
    }

    private boolean isExistingLabel(PickerLabel targetLabel) {
        return isExistingLabel(targetLabel.getActualName());
    }

    private boolean isExistingLabel(String labelName) {
        return issue.getLabels().contains(labelName);
    }

    private boolean isActiveLabel(String labelName) {
        return activeLabels.getOrDefault(labelName, false);
    }

    /**
     * Used to get the list of labels to be put after | in the top pane
     *
     * @return
     */
    private List<PickerLabel> getAddedLabels() {
        return modificationLabels.stream()
                .filter(label -> !isExistingLabel(label.getActualName()))
                .collect(Collectors.toList());
    }
}
