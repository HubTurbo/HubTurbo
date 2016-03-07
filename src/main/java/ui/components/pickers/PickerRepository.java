package ui.components.pickers;

import java.util.ArrayList;
import java.util.List;

public class PickerRepository implements Comparable<PickerRepository> {

    private final String repositoryId;
    private List<Integer> matchingPositions = new ArrayList<>();

    public PickerRepository(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void updateMatchingPositions(String query) {
        matchingPositions.clear();
        int nextIndexToMatch = 0; // next index to match at query
        for (int i = 0; i < repositoryId.length(); i++) {
            if (isValidIndex(nextIndexToMatch, query) && repositoryId.charAt(i) == query.charAt(nextIndexToMatch)) {
                matchingPositions.add(i);
                nextIndexToMatch++;
            }
        }
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public List<Integer> getMatchingPositions() {
        return matchingPositions;
    }

    private boolean isValidIndex(int index, String string) {
        return index >= 0 && index < string.length();
    }

    @Override
    public int compareTo(PickerRepository o) {
        // prioritize the number of matching characters
        if (matchingPositions.size() != o.getMatchingPositions().size()) {
            return matchingPositions.size() - o.getMatchingPositions().size();
        }
        // then, prioritize earliest matching
        for (int i = 0; i < matchingPositions.size(); i++) {
            if (matchingPositions.get(i) != o.getMatchingPositions().get(i)) {
                return matchingPositions.get(i) - o.getMatchingPositions().get(i);
            }
        }
        // use the repo itself as a tie breaker
        return repositoryId.compareTo(o.getRepositoryId());
    }
}
