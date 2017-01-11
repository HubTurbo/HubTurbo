package ui.components.pickers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the state of a BoardPicker. It is determined by the set of boards and the user input.
 */
class BoardPickerState {

    private Set<String> boards;
    private List<String> matchedBoards;
    private Optional<String> suggestion;
    private String userInput;

    BoardPickerState(Set<String> boards, String userInput) {
        this(boards, new ArrayList<>(), userInput.trim(), Optional.empty());
        update();
    }

    private BoardPickerState(Set<String> boards, List<String> matchedBoards, String userInput,
                             Optional<String> suggestion) {
        this.boards = boards;
        this.matchedBoards = matchedBoards;
        this.suggestion = suggestion;
        this.userInput = userInput;
    }

    List<String> getMatchedBoards() {
        return matchedBoards;
    }

    Optional<String> getSuggestion() {
        return suggestion;
    }

    private void update() {
        if (userInput.isEmpty()) {
            matchAllBoards();
            return;
        }

        updateMatchedBoards();
        updateSuggestion();
    }

    private void matchAllBoards() {
        matchedBoards = boards.stream().collect(Collectors.toList());
    }

    private void updateMatchedBoards() {
        String[] keywords = userInput.split("\\s+");
        matchedBoards = boards.stream()
                    .filter(board -> {
                        String[] parts = board.trim().toLowerCase().split("\\s+");
                        if (keywords.length > parts.length) {
                            return false;
                        }
                        for (String keyword : keywords) {
                            if (!board.matches(String.format("(?i:.*\\b%s.*)", keyword))) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
    }

    private void updateSuggestion() {
        if (matchedBoards.isEmpty() || userInput.isEmpty()) {
            suggestion = Optional.empty();
        } else {
            suggestion = matchedBoards.stream().min(String::compareToIgnoreCase);
        }
    }

}
