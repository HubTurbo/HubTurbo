package ui.components.pickers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Liu Xinan
 */
public class BoardPickerState {

    private Set<String> boards;
    private List<String> matchedBoards;
    private Optional<String> suggestion;
    private String keyword;

    public BoardPickerState(Set<String> boards, String userInput) {
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

    public List<String> getMatchedBoards() {
        return matchedBoards;
    }

    public Optional<String> getSuggestion() {
        return suggestion;
    }

    private final void update() {
        if (keyword.isEmpty()) {
            return;
        }
        updateMatchedBoards();
        updateSuggestion();
    }

    private final void updateMatchedBoards() {
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

    private final void updateSuggestion() {
        if (matchedBoards.size() == 1) {
            suggestion = Optional.of(matchedBoards.get(0));
        }
    }

}
