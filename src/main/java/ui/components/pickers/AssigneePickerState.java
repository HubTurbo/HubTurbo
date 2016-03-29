package ui.components.pickers;

import util.Utility;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssigneePickerState {
    private List<PickerAssignee> currentAssigneesList;

    public AssigneePickerState(List<PickerAssignee> assignees) {
        currentAssigneesList = cloneAssignees(assignees);
    }

    public AssigneePickerState(List<PickerAssignee> assignees, String userInput) {
        this(assignees);
        processInput(userInput);
    }

    private static List<PickerAssignee> cloneAssignees(List<PickerAssignee> sourceList) {
        return sourceList.stream()
                .map(PickerAssignee::new)
                .collect(Collectors.toList());
    }

    private void processInput(String userInput) {
        if (userInput.isEmpty()) {
            return;
        }

        String[] userInputWords = userInput.split(" ");
        Stream.of(userInputWords)
                .forEach(this::filterAssignees);
        toggleFirstMatchingAssignee();
    }

    public List<PickerAssignee> getCurrentAssigneesList() {
        return this.currentAssigneesList;
    }

    public List<PickerAssignee> getMatchingAssigneesList() {
        return currentAssigneesList.stream()
                .filter(PickerAssignee::isMatching)
                .collect(Collectors.toList());
    }

    public void toggleExactMatchAssignee(String assigneeLoginName) {
        Optional<PickerAssignee> exactMatchAssignee = getExactMatchAssignee(assigneeLoginName);
        exactMatchAssignee.ifPresent(this::toggleAssignee);
    }

    private void toggleFirstMatchingAssignee() {
        Optional<PickerAssignee> firstMatchingAssignee = getFirstMatchingAssignee();
        firstMatchingAssignee.ifPresent(this::toggleAssignee);
    }

    private void toggleAssignee(PickerAssignee matchingAssignee) {
        currentAssigneesList.forEach(assignee -> assignee.setSelected(matchingAssignee.equals(assignee)
                && !assignee.isSelected()));
    }

    private void filterAssignees(String query) {
        currentAssigneesList.forEach(assignee -> {
            boolean matchQuery = Utility.containsIgnoreCase(assignee.getLoginName(), query);
            assignee.setMatching(matchQuery);
        });
    }

    private Optional<PickerAssignee> getFirstMatchingAssignee() {
        return currentAssigneesList.stream()
                .filter(PickerAssignee::isMatching)
                .findFirst();
    }

    private Optional<PickerAssignee> getExactMatchAssignee(String query) {
        return currentAssigneesList.stream()
                .filter(assignee -> assignee.getLoginName().equals(query))
                .findFirst();
    }
}
