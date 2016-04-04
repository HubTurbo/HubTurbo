package ui.components.pickers;

import util.Utility;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssigneePickerState {
    private List<PickerAssignee> usersList;

    public AssigneePickerState(List<PickerAssignee> users) {
        usersList = cloneUsers(users);
    }

    public AssigneePickerState(List<PickerAssignee> users, String userInput) {
        this(users);
        processInput(userInput);
    }

    private static List<PickerAssignee> cloneUsers(List<PickerAssignee> sourceList) {
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
                .forEach(this::filterUsers);
        toggleFirstMatchingUser();
    }

    public List<PickerAssignee> getCurrentUsersList() {
        return this.usersList;
    }

    public List<PickerAssignee> getMatchingUsersList() {
        return usersList.stream()
                .filter(PickerAssignee::isMatching)
                .collect(Collectors.toList());
    }

    public void toggleExactMatchUser(String username) {
        Optional<PickerAssignee> exactMatchUser = getExactMatchUser(username);
        exactMatchUser.ifPresent(this::toggleUser);
    }

    private void toggleFirstMatchingUser() {
        Optional<PickerAssignee> firstMatchingUser = getFirstMatchingUser();
        firstMatchingUser.ifPresent(this::toggleUser);
    }

    private void toggleUser(PickerAssignee matchingUser) {
        usersList.forEach(user -> user.setSelected(matchingUser.equals(user)
                && !user.isSelected()));
    }

    private void filterUsers(String query) {
        usersList.forEach(user -> {
            boolean matchQuery = Utility.containsIgnoreCase(user.getLoginName(), query);
            user.setMatching(matchQuery);
        });
    }

    private Optional<PickerAssignee> getFirstMatchingUser() {
        return usersList.stream()
                .filter(PickerAssignee::isMatching)
                .findFirst();
    }

    private Optional<PickerAssignee> getExactMatchUser(String query) {
        return usersList.stream()
                .filter(user -> user.getLoginName().equals(query))
                .findFirst();
    }
}
