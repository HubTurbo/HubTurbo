package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssigneePickerState {
    private List<PickerAssignee> currentAssigneesList;

    public AssigneePickerState(List<PickerAssignee> assignees) {
        currentAssigneesList = getResetList(assignees);
    }

    public AssigneePickerState(List<PickerAssignee> assignees, String userInput) {
        this(assignees);
        processInput(userInput);
    }

    // returns list with all boolean attributes of picker assignee set to false
    private List<PickerAssignee> getResetList(List<PickerAssignee> sourceList) {
        List<PickerAssignee> resetList = new ArrayList<>();
        sourceList.stream()
                .forEach(milestone -> resetList.add(new PickerAssignee(milestone)));
        return resetList;
    }

    private void processInput(String userInput) {
        if (userInput.isEmpty()) {
            return;
        }

        String[] userInputWords = userInput.split(" ");
        for (int i = 0; i < userInputWords.length; i++) {
            String currentWord = userInputWords[i];
            if (i < userInputWords.length - 1 || userInput.endsWith(" ")) {
                toggleAssignee(currentWord);
            } else {
                filterAssignee(currentWord);
            }
        }
    }

    public final void toggleAssignee(String assigneeQuery) {
        Optional<PickerAssignee> onlyMatchingAssignee = getOnlyMatchingAssignee(assigneeQuery);
        if (!onlyMatchingAssignee.isPresent()) return;
        currentAssigneesList.stream()
                .forEach(assignee -> {
                    assignee.setSelected(assignee.equals(onlyMatchingAssignee.get())
                        && !assignee.isSelected());
                });
    }

    private void filterAssignee(String query) {
        currentAssigneesList.stream()
                .forEach(assignee -> {
                    boolean matchQuery = Utility.containsIgnoreCase(assignee.getLoginName(), query);
                    assignee.setFaded(!matchQuery);
                });
        highlightFirstMatchingAssignee();
    }

    public List<PickerAssignee> getCurrentAssigneesList() {
        return this.currentAssigneesList;
    }

    public List<PickerAssignee> getMatchingAssigneeList() {
        return currentAssigneesList.stream()
                .filter(assignee -> !assignee.isFaded())
                .collect(Collectors.toList());
    }

    private void highlightFirstMatchingAssignee() {
        if (hasMatchingAssignee(currentAssigneesList)) {
            currentAssigneesList.stream()
                    .filter(assignee -> !assignee.isFaded())
                    .findAny()
                    .get()
                    .setHighlighted(true);
        }
    }

    private boolean hasMatchingAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> !assignee.isFaded())
                .findAny()
                .isPresent();
    }

    private Optional<PickerAssignee> getOnlyMatchingAssignee(String query) {
        if (!hasExactlyOneMatchingAssignee(currentAssigneesList, query)) {
            return Optional.empty();
        }
        return Optional.of(getMatchingAssigneeName(currentAssigneesList, query));
    }

    private boolean hasExactlyOneMatchingAssignee(List<PickerAssignee> assigneeList, String query) {
        return assigneeList.stream()
                .filter(assignee -> Utility.containsIgnoreCase(assignee.getLoginName(), query))
                .count() == 1;
    }

    private PickerAssignee getMatchingAssigneeName(List<PickerAssignee> assigneeList, String query) {
        return assigneeList.stream()
                .filter(assignee -> Utility.containsIgnoreCase(assignee.getLoginName(), query))
                .findFirst()
                .get();
    }
}
