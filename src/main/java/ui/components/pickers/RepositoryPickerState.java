package ui.components.pickers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class used to store the list of repositories, used by RepositoryPicker.
 */
public class RepositoryPickerState {

    private final List<PickerRepository> repositories = new ArrayList<>();

    public RepositoryPickerState(Set<String> storedRepositories) {
        storedRepositories.stream()
                .forEach(repo -> repositories.add(new PickerRepository(repo)));
    }

    public List<String> getMatchingRepositories(String query, MatchingMode matchingMode) {
        return repositories.stream()
                .filter(repo -> isMatching(repo, query, matchingMode))
                .sorted((a, b) -> a.getRepositoryId().compareTo(b.getRepositoryId()))
                .map(repo -> repo.getRepositoryId())
                .collect(Collectors.toList());
    }

    private boolean isMatching(PickerRepository repo, String query, MatchingMode matchingMode) {
        if (matchingMode == MatchingMode.PREFIX_MATCHING) {
            return repo.getRepositoryId().toLowerCase().startsWith(query.toLowerCase());
        } else if (matchingMode == MatchingMode.SUBSTRING_MATCHING) {
            return repo.getRepositoryId().toLowerCase().contains(query.toLowerCase());
        }
        assert false : "MatchingMode not supported";
        return false;
    }

}
