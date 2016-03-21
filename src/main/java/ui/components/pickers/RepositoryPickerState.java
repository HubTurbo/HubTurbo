package ui.components.pickers;

import org.controlsfx.control.spreadsheet.Picker;

import java.util.*;
import java.util.stream.IntStream;

/**
 * A class used to store the list of repositories, used by RepositoryPicker.
 */
public class RepositoryPickerState {

    private final List<PickerRepository> repositories = new ArrayList<>();
    private final List<PickerRepository> matchingRepositories = new ArrayList<>();

    public RepositoryPickerState(Set<String> storedRepositories) {
        storedRepositories.stream()
                .forEach(repo -> repositories.add(new PickerRepository(repo)));
        Collections.sort(repositories);
    }

    public void updateUserQuery(String query) {
        clearRepositorySelection();
        updateMatchingRepositories(query);
    }

    public String getSelectedRepositoryId() {
        Optional<PickerRepository> selectedRepository = matchingRepositories.stream()
                .filter(repo -> repo.isSelected())
                .findFirst();
        assert selectedRepository.isPresent();
        return selectedRepository.get().getRepositoryId();
    }

    /**
     * Selects a repository right after the current selected repository in the sorted
     * matching repository list. If the current selected repository is the last one,
     * it will then select the first matching repository instead.
     */
    public void selectNextMatchingRepository() {
        OptionalInt selectedPositionInMatching = getSelectedRepositoryPositionInMatching();
        assert selectedPositionInMatching.isPresent();
        int currentPosition = selectedPositionInMatching.getAsInt();
        int nextPosition = currentPosition == matchingRepositories.size() - 1 ? 0 : currentPosition + 1;
        matchingRepositories.get(currentPosition).setSelected(false);
        matchingRepositories.get(nextPosition).setSelected(true);
    }

    /**
     * Selects a repository right before the current selected repository in the sorted
     * matching repository list. If the current selected repository is the first one,
     * it will then select the last matching repository instead.
     */
    public void selectPreviousMatchingRepository() {
        OptionalInt selectedPositionInMatching = getSelectedRepositoryPositionInMatching();
        assert selectedPositionInMatching.isPresent();
        int currentPosition = selectedPositionInMatching.getAsInt();
        int nextPosition = currentPosition == 0 ? matchingRepositories.size() - 1 : currentPosition - 1;
        matchingRepositories.get(currentPosition).setSelected(false);
        matchingRepositories.get(nextPosition).setSelected(true);
    }

    private OptionalInt getSelectedRepositoryPositionInMatching() {
        return IntStream.range(0, matchingRepositories.size())
                .filter(index -> matchingRepositories.get(index).isSelected())
                .findFirst();
    }

    /**
     * Updates List<PickerRepository> so that it contains PickerRepositories that match
     * the user's input
     */
    private void updateMatchingRepositories(String query) {
        matchingRepositories.clear();
        repositories.stream().forEach(repo -> {
            if (isMatching(repo, query)) {
                matchingRepositories.add(repo);
            }
        });
        Optional<PickerRepository> possibleExactMatch = getPickerRepositoryById(matchingRepositories, query);
        if (!possibleExactMatch.isPresent() && !query.isEmpty()) {
            PickerRepository newRepository = new PickerRepository(query);
            newRepository.setSelected(true);
            matchingRepositories.add(0, newRepository);
        } else if (!query.isEmpty()) {
            PickerRepository exactMatch = possibleExactMatch.get();
            exactMatch.setSelected(true);
        } else {
            assert !matchingRepositories.isEmpty();
            PickerRepository firstMatching = matchingRepositories.get(0);
            firstMatching.setSelected(true);
        }
    }

    /**
     * Finds whether there is any PickerRepository in repositoryList
     * which has exactly same id as query, the matching is case-insensitive.
     */
    private Optional<PickerRepository> getPickerRepositoryById(List<PickerRepository> repositoryList, String repoId) {
        return repositoryList.stream()
                .filter(repo -> repo.getRepositoryId().equalsIgnoreCase(repoId))
                .findFirst();
    }

    private void clearRepositorySelection() {
        repositories.stream().forEach(repo -> repo.setSelected(false));
    }

    /**
     * Returns a list of PickerRepository which matches the current user input.
     * The first entry matchingRepository is always the user input.
     */
    public List<PickerRepository> getMatchingRepositories() {
        return new ArrayList<>(matchingRepositories);
    }

    private boolean isMatching(PickerRepository repo, String query) {
        return repo.getRepositoryId().toLowerCase().contains(query.toLowerCase());
    }

}
