package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import util.Utility;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LabelPickerUILogic {

    private final TurboIssue issue;
    private final LabelPickerDialog dialog;
    private List<TurboLabel> allLabels;
    private final List<PickerLabel> topLabels = new ArrayList<>();
    private List<PickerLabel> bottomLabels;
    private final Map<String, Boolean> groups = new HashMap<>();
    private final Map<String, Boolean> resultList = new HashMap<>();
    private Optional<String> targetLabel = Optional.empty();

    // Used for multiple spaces
    private String lastAction = "";
    private int previousNumberOfActions = 0;

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
        addRemovePossibleLabel("");
        preProcessAndUpdateTopLabels(name);
        updateBottomLabels(""); // clears search query, removes faded-out overlay on bottom labels
        populatePanes();
    }

    public void toggleSelectedLabel(String text) {
        if (!bottomLabels.isEmpty() && !text.isEmpty() && hasHighlightedLabel()) {
            toggleLabel(
                    bottomLabels.stream().filter(PickerLabel::isHighlighted).findFirst().get().getActualName());
        }
    }

    @SuppressWarnings("PMD")
    public void processTextFieldChange(String text) {
        String[] textArray = text.split(" ");
        if (textArray.length > 0) {
            String query = textArray[textArray.length - 1];
            if (previousNumberOfActions != textArray.length || !query.equals(lastAction)) {
                updateActionVariables(textArray, query);
                updateBottomLabelsByGroupIfPossible(query);
                updatePossibleLabel();
                populatePanes();
            }
        }
    }

    private void updatePossibleLabel() {
        if (hasHighlightedLabel()) {
            addRemovePossibleLabel(getHighlightedLabelName().get().getActualName());
        } else {
            addRemovePossibleLabel("");
        }
    }

    private void updateActionVariables(String[] textArray, String query) {
        previousNumberOfActions = textArray.length;
        lastAction = query;
    }

    private void updateBottomLabelsByGroupIfPossible(String query) {
        if (TurboLabel.getDelimiter(query).isPresent()) {
            String delimiter = TurboLabel.getDelimiter(query).get();
            String[] queryArray = query.split(Pattern.quote(delimiter));
            if (queryArray.length == 1) {
                updateBottomLabels(queryArray[0], "");
            } else if (queryArray.length == 2) {
                updateBottomLabels(queryArray[0], queryArray[1]);
            }
        } else{
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
            if (turboLabel.get().isExclusive() && !resultList.get(name)) {
                // exclusive label check
                String group = turboLabel.get().getGroup().get();
                allLabels
                        .stream()
                        .filter(TurboLabel::isExclusive)
                        .filter(label -> label.getGroup().get().equals(group))
                        .forEach(label -> updateTopLabels(label.getActualName(), false));
                updateTopLabels(name, true);
            } else {
                updateTopLabels(name, !resultList.get(name));
            }
        }
    }

    private void updateTopLabels(String name, boolean isAdd) {
        // adds new labels to the end of the list
        resultList.put(name, isAdd); // update resultList first
        if (isAdd) {
            addToTopLabel(name);
        } else {
            removeFromTopLabel(name);
        }
    }

    private void addToTopLabel(String name) {
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
    }

    private void removeFromTopLabel(String name) {
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

    private boolean isInTopLabels(String name) {
        // used to prevent duplicates in topLabels
        return topLabels.stream()
                .filter(label -> label.getActualName().equals(name))
                .findAny()
                .isPresent();
    }

    private void addRemovePossibleLabel(String name) {
        deletePreviousPossibleLabel();
        addToPossibleLabel(name);
    }

    private void deletePreviousPossibleLabel() {
        if (targetLabel.isPresent()) {
            // if there's a previous possible selection, delete it
            // targetLabel can be
            topLabels.stream()
                    .filter(label -> label.getActualName().equals(targetLabel.get()))
                    .findFirst()
                    .ifPresent(label -> {
                        if (issue.getLabels().contains(targetLabel.get()) || resultList.get(targetLabel.get())) {
                            // if it is an existing label toggle fade and strike through
                            label.setIsHighlighted(false);
                            label.setIsFaded(false);
                            if (resultList.get(label.getActualName())) {
                                label.setIsRemoved(false);
                            } else {
                                label.setIsRemoved(true);
                            }
                        } else {
                            // if not then remove it
                            topLabels.remove(label);
                        }
                    });
            targetLabel = Optional.empty();
        }
    }

    private void addToPossibleLabel(String name) {
        if (!name.isEmpty()) {
            // Try to add current selection
            if (isInTopLabels(name)) {
                // if it exists in the top pane
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(label -> {
                            label.setIsHighlighted(true);
                            if (issue.getLabels().contains(name)) {
                                // if it is an existing label toggle fade and strike through
                                label.setIsFaded(resultList.get(name));
                                label.setIsRemoved(resultList.get(name));
                            } else {
                                // else set fade and strike through
                                // if space is pressed afterwards, label is removed from topLabels altogether
                                label.setIsFaded(true);
                                label.setIsRemoved(true);
                            }
                        });
            } else {
                // add it to the top pane
                allLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(label -> topLabels.add(
                                new PickerLabel(label, this, false, true, false, true, true)));
            }
            targetLabel = Optional.of(name);
        }
    }

    // Bottom box deals with possible matches so we usually ignore the case for these methods.
    @SuppressWarnings("unused")
    private void ______BOTTOM_BOX______() {}

    private void updateBottomLabels(String group, String match) {
        List<String> groupNames = groups.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> validGroups = groupNames.stream()
                .filter(validGroup -> Utility.startsWithIgnoreCase(validGroup, group))
                .collect(Collectors.toList());

        if(validGroups.size() > 0){
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
            List<PickerLabel> matchingLabels = getMatchingLabels();

            // move highlight around
            for (int i = 0; i < matchingLabels.size(); i++) {
                if (matchingLabels.get(i).isHighlighted()) {
                    highlightNextLabel(isDown, matchingLabels, i);
                    populatePanes();
                    return;
                }
            }
        }
    }

    private List<PickerLabel> getMatchingLabels() {
        return bottomLabels.stream()
                .filter(label -> !label.isFaded())
                .collect(Collectors.toList());
    }

    private void highlightNextLabel(boolean isDown, List<PickerLabel> matchingLabels, int i) {
        if (isDown && i < matchingLabels.size() - 1) {
            matchingLabels.get(i).setIsHighlighted(false);
            matchingLabels.get(i + 1).setIsHighlighted(true);
            addRemovePossibleLabel(matchingLabels.get(i + 1).getActualName());
        } else if (!isDown && i > 0) {
            matchingLabels.get(i - 1).setIsHighlighted(true);
            matchingLabels.get(i).setIsHighlighted(false);
            addRemovePossibleLabel(matchingLabels.get(i - 1).getActualName());
        }
    }

    private void highlightFirstMatchingItem(String match) {
        List<PickerLabel> matchingLabels = getMatchingLabels();

        // try to highlight labels that begin with match first
        matchingLabels.stream()
                .filter(label -> Utility.startsWithIgnoreCase(label.getName(), match))
                .findFirst()
                .ifPresent(label -> label.setIsHighlighted(true));

        // if not then highlight first matching label
        if (!hasHighlightedLabel()) {
            matchingLabels.stream()
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

    private List<PickerLabel> getNewTopLabels() {
        return topLabels.stream()
                .filter(label -> !issue.getLabels().contains(label.getActualName()))
                .collect(Collectors.toList());
    }
}
