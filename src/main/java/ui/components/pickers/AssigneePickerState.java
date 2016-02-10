package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;

public class AssigneePickerState {
    private List<PickerAssignee> currentAssigneesList;

    public AssigneePickerState(List<PickerAssignee> assignees) {
        currentAssigneesList = new ArrayList<>(assignees);
    }

    public AssigneePickerState(List<PickerAssignee> assignees, String userInput) {
        this(assignees);
        processInput(userInput);
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
        String assigneeName = getAssigneeName(assigneeQuery);
        if (assigneeName == null) return;
        currentAssigneesList.stream()
                .forEach(assignee -> {
                    assignee.setSelected(assignee.getLoginName().equals(assigneeName)
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

    private String getAssigneeName(String query) {
        if (hasExactlyOneMatchingAssignee(currentAssigneesList, query)) {
            return getMatchingAssigneeName(currentAssigneesList, query);
        }
        return null;
    }

    private boolean hasExactlyOneMatchingAssignee(List<PickerAssignee> assigneeList, String query) {
        return assigneeList.stream()
                .filter(assignee -> Utility.containsIgnoreCase(assignee.getLoginName(), query))
                .count() == 1;
    }

    private String getMatchingAssigneeName(List<PickerAssignee> assigneeList, String query) {
        return assigneeList.stream()
                .filter(assignee -> Utility.containsIgnoreCase(assignee.getLoginName(), query))
                .findFirst()
                .get()
                .getLoginName();
    }
}
