package ui.components.pickers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Liu Xinan
 */
class BoardPickerState {

    private Set<String> boards;
    private List<String> matchedBoards;
    private Optional<String> suggestion;
    private String keyword;

    BoardPickerState(Set<String> boards, String userInput) {
        this(boards, new ArrayList<>(), userInput.trim(), Optional.empty());
        update();
    }

    private BoardPickerState(Set<String> boards, List<String> matchedBoards, String keyword,
                             Optional<String> suggestion) {
        this.boards = boards;
        this.matchedBoards = matchedBoards;
        this.suggestion = suggestion;
        this.keyword = keyword;
    }

    List<String> getMatchedBoards() {
        return matchedBoards;
    }

    Optional<String> getSuggestion() {
        return suggestion;
    }

    private void update() {
        if (keyword.isEmpty()) {
            matchAllBoards();
        }
        updateMatchedBoards();
        updateSuggestion();
    }

    private void matchAllBoards() {
        matchedBoards = boards.stream().collect(Collectors.toList());
    }

    private void updateMatchedBoards() {
        String[] prefixes = keyword.split("\\s+");
        matchedBoards = boards.stream()
                    .filter(board -> {
                        String[] parts = board.trim().toLowerCase().split("\\s+");
                        if (prefixes.length > parts.length) {
                            return false;
                        }
                        for (int i = 0; i < prefixes.length; i++) {
                            if (!parts[i].startsWith(prefixes[i])) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
    }

    private void updateSuggestion() {
        if (matchedBoards.isEmpty() || matchedBoards.size() == boards.size()) {
            suggestion = Optional.empty();
        } else {
            suggestion = matchedBoards.stream().min(String::compareToIgnoreCase);
        }
    }

}
