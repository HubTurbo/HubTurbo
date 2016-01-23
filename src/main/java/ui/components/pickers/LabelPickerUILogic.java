package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import util.Utility;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles the logic of the label picker UI.
 *
 * This class provides the following key methods:
 * 1. processTextFieldChange: this should be called after every change in the UI's text field
 * 2. toggleLabel
 * 3. toggleHighlightedLabel
 * 4. moveHighlightOnLabel
 */
public class LabelPickerUILogic {

    private final TurboIssue issue;
    private final LabelPickerDialog dialog;
    private List<TurboLabel> allLabels;
    private final List<PickerLabel> topLabels = new ArrayList<>();
    private List<PickerLabel> bottomLabels;
    private final Map<String, Boolean> groups = new HashMap<>();
    private final Map<String, Boolean> resultList = new HashMap<>();
    private Optional<String> targetLabel = Optional.empty();

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
        updateBottomLabels("");
        populatePanes();
    }

    public LabelPickerUILogic(TurboIssue issue, List<TurboLabel> repoLabels) {
        this.issue = issue;
        this.dialog = null;
        populateAllLabels(repoLabels);
        addExistingLabels();
        updateBottomLabels("");
        populatePanes();
    }

    private void populateAllLabels(List<TurboLabel> repoLabels) {
        this.allLabels = new ArrayList<>(repoLabels);
        Collections.sort(this.allLabels);
        // populate resultList by going through repoLabels and seeing which ones currently exist
        // in issue.getLabels()
        repoLabels.forEach(label -> {
            // matching with exact labels so no need to worry about capitalisation
            resultList.put(label.getActualName(), isExistingLabel(label.getActualName()));
            if (label.getGroup().isPresent() && !groups.containsKey(label.getGroup().get())) {
                groups.put(label.getGroup().get(), label.isExclusive());
            }
        });
    }

    private void populatePanes() {
        if (dialog != null) dialog.populatePanes(getExistingLabels(), getNewTopLabels(), bottomLabels, groups);
    }

    public ArrayList<String> toggleLabel(String name) {
        removePossibleLabel();
        ArrayList<String> removedLabels = processAndUpdateTopLabels(name);
        updateBottomLabels(""); // clears search query, removes faded-out overlay on bottom labels
        populatePanes();
        return removedLabels;
    }

    public ArrayList<String> toggleHighlightedLabel() {
        return toggleLabel(getHighlightedLabelName().get().getActualName());
    }

    @SuppressWarnings("unused")
    private void ______TEXT_FIELD______() {}

    public void processTextFieldChange(String text) {
        if (isEmpty(text)) {
            removePossibleLabel();
        } else {
            processChangeAndUpdateVariables(text);
        }
        populatePanes();
    }

    private void processChangeAndUpdateVariables(String text) {
        if (isLongerTextFieldThanPrevious(text)) {
            addCharHandler(text);
        } else {
            removeCharHandler(text);
        }

        updateVariables(text);
    }

    /**
     * This method will either toggle the highlighted label, or update both the bottom labels and the possible label
     * in the top pane, depending on the last character of text (added character)
     */
    private void addCharHandler(String text) {
        if (isLastCharSpace(text)) {
            // space-toggle
            spaceToggle();
        } else {
            queryHandler(getLastWord(text));
        }
    }

    private void spaceToggle() {
        if (!bottomLabels.isEmpty() && hasHighlightedLabel()) {
            stackOfSpecifiedLabels.push(getHighlightedLabelName().get().getActualName());
            stackOfRemovedExclusiveLabels.push(toggleHighlightedLabel());
        } else {
            stackOfSpecifiedLabels.push(null);
            stackOfRemovedExclusiveLabels.push(null);
        }
    }

    private void queryHandler(String lastWord) {
        updateBottomLabelsWithRawQuery(lastWord);
        updatePossibleLabel();
    }

    private void removeCharHandler(String text) {
        if (isWordRemoved(text)) {
            undoPreviousSpaceToggle();
        }

        if (isLastCharSpace(text)) {
            removePossibleLabel();
        } else {
            queryHandler(getLastWord(text));
        }
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
            updateTopLabels(lastSpecifiedLabel, !resultList.get(lastSpecifiedLabel));
        }
    }

    /**
     * Restores the most recent set of removed label(s) due to exclusive grouping
     */
    private void restoreRecentlyRemovedLabels() {
        ArrayList<String> listOfRemovedLabels = stackOfRemovedExclusiveLabels.pop();
        if (listOfRemovedLabels != null) {
            listOfRemovedLabels.stream()
                    .forEach(labelName -> updateTopLabels(labelName, true));
        }
    }

    private boolean isEmpty(String text) {
        return text.length() == 0;
    }

    private boolean isLongerTextFieldThanPrevious(String text) {
        return text.length() > previousTextFieldLength;
    }

    private void updateVariables(String text) {
        previousTextFieldLength = text.length();
        previousLastChar = text.charAt(text.length() - 1);
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
        // used once to populate topLabels at the start
        allLabels.stream()
                .filter(label -> isExistingLabel(label.getActualName()))
                .forEach(label -> topLabels.add(new PickerLabel(label, this, true)));
    }

    private ArrayList<String> processAndUpdateTopLabels(String name) {
        ArrayList<String> listOfRemovedNames = new ArrayList<>();
        Optional<TurboLabel> turboLabel =
                allLabels.stream().filter(label -> label.getActualName().equals(name)).findFirst();
        if (turboLabel.isPresent()) {
            if (turboLabel.get().isExclusive() && !resultList.get(name)) {
                removeConflictingExclusiveLabels(turboLabel, listOfRemovedNames);
            }
            updateTopLabels(name, !resultList.get(name));
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
        allLabels
                .stream()
                .filter(TurboLabel::isExclusive)
                .filter(label -> label.getGroup().get().equals(group))
                .forEach(label -> {
                    if (isInTopLabels(label.getActualName()) && !isARemovedTopLabel(label.getActualName())) {
                        listOfRemovedNames.add(label.getActualName());
                    }
                    updateTopLabels(label.getActualName(), false);
                });
    }

    private void updateTopLabels(String name, boolean isAdd) {
        // adds new labels to the end of the list
        resultList.put(name, isAdd); // update resultList first
        if (isAdd) {
            if (isExistingLabel(name)) {
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .forEach(label -> {
                            label.setIsRemoved(false);
                            label.setIsFaded(false);
                        });
            } else {
                allLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .filter(label -> resultList.get(label.getActualName()))
                        .filter(label -> !isInTopLabels(label.getActualName()))
                        .findFirst()
                        .ifPresent(label -> topLabels.add(new PickerLabel(label, this, true)));
            }
        } else {
            topLabels.stream()
                    .filter(label -> label.getActualName().equals(name))
                    .findFirst()
                    .ifPresent(label -> {
                        if (isExistingLabel(name)) {
                            label.setIsRemoved(true);
                        } else {
                            topLabels.remove(label);
                        }
                    });
        }
    }

    private boolean isInTopLabels(String name) {
        // used to prevent duplicates in topLabels
        return topLabels.stream()
                .filter(label -> label.getActualName().equals(name))
                .findAny()
                .isPresent();
    }

    private boolean isARemovedTopLabel(String name) {
        return topLabels.stream()
                .filter(label -> label.getActualName().equals(name) && label.isRemoved())
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
        if (targetLabel.isPresent()) {
            topLabels.stream()
                    .filter(label -> label.getActualName().equals(targetLabel.get()))
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
                                topLabels.remove(label);
                            }
                        }
                    });
            targetLabel = Optional.empty();
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
            TurboLabel highlightedLabel = getHighlightedLabelName().get();
            String highlightedLabelName = highlightedLabel.getActualName();
            if (isExistingLabel(highlightedLabelName)) {
                topLabels.stream()
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
                if (isInTopLabels(highlightedLabelName)) {
                    // find the label and remove it
                    topLabels.stream()
                            .filter(label -> label.getActualName().equals(highlightedLabelName))
                            .findFirst()
                            .ifPresent(label -> {
                                label.setIsRemoved(true);
                                label.setIsFaded(true);
                            });
                } else {
                    topLabels.add(new PickerLabel(highlightedLabel, this, false, true, false, true, true));
                }
            }

            targetLabel = Optional.of(highlightedLabelName);
        }
    }

    // Bottom box deals with possible matches so we usually ignore the case for these methods.
    @SuppressWarnings("unused")
    private void ______BOTTOM_BOX______() {}

    /**
     * Updates the bottom labels given a raw query entered by the user
     * This query should be taken from the last word of the user input (after a space)
     */
    private void updateBottomLabelsWithRawQuery(String query) {
        if (TurboLabel.getDelimiter(query).isPresent()) {
            String delimiter = TurboLabel.getDelimiter(query).get();
            String[] queryArray = query.split(Pattern.quote(delimiter));

            if (queryArray.length == 1) {
                updateBottomLabels(queryArray[0], "");
            } else if (queryArray.length == 2) {
                updateBottomLabels(queryArray[0], queryArray[1]);
            }
        } else {
            updateBottomLabels(query);
        }
    }

    private void updateBottomLabels(String group, String match) {
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
            bottomLabels = allLabels
                    .stream()
                    .map(label -> new PickerLabel(label, this, false))
                    .map(label -> {
                        if (resultList.get(label.getActualName())) {
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
            if (!bottomLabels.isEmpty()) highlightFirstMatchingItem(match);
        } else {
            updateBottomLabels(match);
        }
    }

    private void updateBottomLabels(String match) {
        // get all labels that contain search query
        // fade out labels which do not match
        bottomLabels = allLabels
                .stream()
                .map(label -> new PickerLabel(label, this, false))
                .map(label -> {
                    if (resultList.get(label.getActualName())) {
                        label.setIsSelected(true); // add tick if selected
                    }
                    if (!match.isEmpty() && !Utility.containsIgnoreCase(label.getActualName(), match)) {
                        label.setIsFaded(true); // fade out if does not match search query
                    }
                    return label;
                })
                .collect(Collectors.toList());

        if (!match.isEmpty() && !bottomLabels.isEmpty()) highlightFirstMatchingItem(match);
    }

    public void moveHighlightOnLabel(boolean isDown) {
        if (hasHighlightedLabel()) {
            // used to move the highlight on the bottom labels
            // find all matching labels
            List<PickerLabel> matchingLabels = bottomLabels.stream()
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
        List<PickerLabel> matches = bottomLabels.stream()
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
        return bottomLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny()
                .isPresent();
    }

    private Optional<PickerLabel> getHighlightedLabelName() {
        return bottomLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny();
    }

    @SuppressWarnings("unused")
    private void ______BOILERPLATE______() {}

    public Map<String, Boolean> getResultList() {
        return resultList;
    }

    private List<PickerLabel> getExistingLabels() {
        return topLabels.stream()
                .filter(label -> isExistingLabel(label.getActualName()))
                .collect(Collectors.toList());
    }

    private boolean isExistingLabel(PickerLabel targetLabel) {
        return isExistingLabel(targetLabel.getActualName());
    }

    private boolean isExistingLabel(String labelName) {
        return issue.getLabels().contains(labelName);
    }

    private List<PickerLabel> getNewTopLabels() {
        return topLabels.stream()
                .filter(label -> !isExistingLabel(label.getActualName()))
                .collect(Collectors.toList());
    }
}
