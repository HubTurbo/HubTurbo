package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import util.Utility;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * Handles the logic when typing in the text field of the label picker UI.
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

    private int previousTextFieldLength = 0;
    private Character previousLastChar = null;
    private final ArrayList<String> listOfSpecifiedLabels = new ArrayList<>();
    private final ArrayList<ArrayList<String>> listOfRemovedExclusiveLabels = new ArrayList<>();

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
            resultList.put(label.getActualName(), issue.getLabels().contains(label.getActualName()));
            if (label.getGroup().isPresent() && !groups.containsKey(label.getGroup().get())) {
                groups.put(label.getGroup().get(), label.isExclusive());
            }
        });
    }

    private void populatePanes() {
        if (dialog != null) dialog.populatePanes(getExistingLabels(), getNewTopLabels(), bottomLabels, groups);
    }

    public void toggleLabel(String name) {
        removePossibleLabel();
        preProcessAndUpdateTopLabels(name);
        updateBottomLabels(""); // clears search query, removes faded-out overlay on bottom labels
        populatePanes();
    }

    public void toggleHighlightedLabel() {
        if (!bottomLabels.isEmpty() && hasHighlightedLabel()) {
            toggleLabel(
                    bottomLabels.stream().filter(PickerLabel::isHighlighted).findFirst().get().getActualName());
        }
    }

    public void processTextFieldChange(String text) {
        int curTextFieldLength = text.length();
        if (curTextFieldLength > 0) {
            if (curTextFieldLength > previousTextFieldLength) {
                // char was added
                char lastChar = text.charAt(curTextFieldLength - 1);
                String[] textArray = text.trim().split(" ");
                String finalLabel = textArray[textArray.length - 1];

                if (lastChar == ' ') {
                    // toggle if the added character was space, and there is a highlighted label
                    if (hasHighlightedLabel()) {
                        listOfSpecifiedLabels.add(getHighlightedLabelName().get().getActualName());
                        toggleHighlightedLabel();
                    } else {
                        listOfSpecifiedLabels.add(null);
                        listOfRemovedExclusiveLabels.add(null);
                    }
                } else {
                    // fade/unfade bottom labels according to query, then update the possible label
                    updateBottomLabelsWithRawQuery(finalLabel);
                    updatePossibleLabel();
                }
            } else {
                // char was removed
                char lastChar = text.charAt(curTextFieldLength - 1);
                if (lastChar != ' ' && previousLastChar == ' ' && !listOfSpecifiedLabels.isEmpty()) {
                    // toggle the last label if removed character was space
                    // and is at the end of previous label
                    int lastIndex = listOfSpecifiedLabels.size() - 1;

                    String lastLabel = listOfSpecifiedLabels.get(lastIndex);
                    listOfSpecifiedLabels.remove(lastIndex);
                    if (lastLabel != null) {
                        updateTopLabels(lastLabel, !resultList.get(lastLabel));
                    }

                    ArrayList<String> listOfRemovedLabels = listOfRemovedExclusiveLabels.get(lastIndex);
                    listOfRemovedExclusiveLabels.remove(lastIndex);
                    if (listOfRemovedLabels != null) {
                        listOfRemovedLabels.stream()
                                .forEach(labelName -> {
                                    updateTopLabels(labelName, true);
                                });
                    }
                }

                if (lastChar == ' ') {
                    removePossibleLabel();
                } else {
                    String[] textArray = text.trim().split(" ");
                    String finalLabel = textArray[textArray.length - 1];
                    updateBottomLabelsWithRawQuery(finalLabel);
                    updatePossibleLabel();
                }
            }

            // update variables
            previousTextFieldLength = curTextFieldLength;
            previousLastChar = text.charAt(curTextFieldLength - 1);
        } else {
            // empty text, revert to existing labels
            removePossibleLabel();
        }
        populatePanes();
    }

    /*
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


    /*
    * Top pane methods do not need to worry about capitalisation because they
    * all deal with actual labels.
    */
    @SuppressWarnings("unused")
    private void ______TOP_PANE______() {}

    private void addExistingLabels() {
        // used once to populate topLabels at the start
        allLabels.stream()
                .filter(label -> issue.getLabels().contains(label.getActualName()))
                .forEach(label -> topLabels.add(new PickerLabel(label, this, true)));
    }

    private void preProcessAndUpdateTopLabels(String name) {
        Optional<TurboLabel> turboLabel =
                allLabels.stream().filter(label -> label.getActualName().equals(name)).findFirst();
        if (turboLabel.isPresent()) {
            ArrayList<String> listOfRemovedNames = new ArrayList<>();
            if (turboLabel.get().isExclusive() && !resultList.get(name)) {
                // exclusive label check
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
                updateTopLabels(name, true);
            } else {
                updateTopLabels(name, !resultList.get(name));
            }

            listOfRemovedExclusiveLabels.add(listOfRemovedNames);
        }
    }

    private void updateTopLabels(String name, boolean isAdd) {
        // adds new labels to the end of the list
        resultList.put(name, isAdd); // update resultList first
        if (isAdd) {
            if (issue.getLabels().contains(name)) {
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
                        if (issue.getLabels().contains(name)) {
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
        // used to prevent duplicates in topLabels
        return topLabels.stream()
                .filter(label -> label.getActualName().equals(name) && label.isRemoved())
                .findAny()
                .isPresent();
    }

    /*
     * Reverts any changes to the UI relating to the possible label
     *
     * 'possible label' here refers to the faded label (if there is)
     */
    private void removePossibleLabel() {
        // Deletes previous possible label
        if (targetLabel.isPresent()) {
            topLabels.stream()
                    .filter(label -> label.getActualName().equals(targetLabel.get()))
                    .findFirst()
                    .ifPresent(label -> {
                        if (issue.getLabels().contains(targetLabel.get())) {
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

    /*
     * This adds a faded label to topLabels if there is an ongoing query
     *
     * Assumptions: Any possible label has been cleared beforehand, if not
     * it might not work as intended
     */
    private void addPossibleLabel() {
        if (hasHighlightedLabel()) {
            //something is highlighted, try to add possible label
            TurboLabel highlightedLabel = getHighlightedLabelName().get();
            String highlightedLabelName = highlightedLabel.getActualName();
            if (issue.getLabels().contains(highlightedLabelName)) {
                // part of existing labels
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
                    //find the label and remove it
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

    private void updatePossibleLabel() {
        removePossibleLabel();
        addPossibleLabel();
    }

    // Bottom box deals with possible matches so we usually ignore the case for these methods.
    @SuppressWarnings("unused")
    private void ______BOTTOM_BOX______() {}

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
                .filter(label -> issue.getLabels().contains(label.getActualName()))
                .collect(Collectors.toList());
    }

    public boolean isExistingLabel(PickerLabel targetLabel) {
        return allLabels.stream()
                .filter(label -> issue.getLabels().contains(targetLabel.getActualName()))
                .count() > 0;
    }

    private List<PickerLabel> getNewTopLabels() {
        return topLabels.stream()
                .filter(label -> !issue.getLabels().contains(label.getActualName()))
                .collect(Collectors.toList());
    }

}
