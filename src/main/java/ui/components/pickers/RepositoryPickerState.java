package ui.components.pickers;

import java.util.*;
import java.util.stream.IntStream;

/**
 * A class used to store the list of repositories, used by RepositoryPicker.
 */
public class RepositoryPickerState {

    private final List<PickerRepository> repositories = new ArrayList<>();
    private final List<PickerRepository> matchingRepositories = new ArrayList<>();
    private String selectedRepositoryId = "";

    public RepositoryPickerState(Set<String> storedRepositories) {
        storedRepositories.stream()
                .forEach(repo -> repositories.add(new PickerRepository(repo)));
        Collections.sort(repositories);
    }

    public void updateUserQuery(String query) {
        updateMatchingRepositories(query);
        updateSelectedRepository(query);
    }

    public String getSelectedRepositoryId() {
        return selectedRepositoryId;
    }

    /**
     * Selects a repository right after the current selected repository in the sorted
     * matching repository list. If the current selected repository is the last one,
     * it will then select the first matching repository instead.
     */
    public void selectNextMatchingRepository() {
        OptionalInt selectedPositionInMatching = getSelectedRepositoryPositionInMatching();
        if (!selectedPositionInMatching.isPresent()) {
            // if there is no previous selection, select the first matching repository if it exists
            if (!matchingRepositories.isEmpty()) {
                PickerRepository toSelect = matchingRepositories.get(0);
                toSelect.setSelected(true);
                setSelectedRepositoryId(toSelect.getRepositoryId());
            }
        } else {
            int currentPosition = selectedPositionInMatching.getAsInt();
            int nextPosition = currentPosition == matchingRepositories.size() - 1 ? 0 : currentPosition + 1;
            matchingRepositories.get(currentPosition).setSelected(false);
            matchingRepositories.get(nextPosition).setSelected(true);
            setSelectedRepositoryId(matchingRepositories.get(nextPosition).getRepositoryId());
        }
    }

    /**
     * Selects a repository right before the current selected repository in the sorted
     * matching repository list. If the current selected repository is the first one,
     * it will then select the last matching repository instead.
     */
    public void selectPreviousMatchingRepository() {
        OptionalInt selectedPositionInMatching = getSelectedRepositoryPositionInMatching();
        if (!selectedPositionInMatching.isPresent()) {
            // if there is no previous selection, select the last matching repository if it exists
            if (!matchingRepositories.isEmpty()) {
                PickerRepository toSelect = matchingRepositories.get(matchingRepositories.size() - 1);
                toSelect.setSelected(true);
                setSelectedRepositoryId(toSelect.getRepositoryId());
            }
        } else {
            int currentPosition = selectedPositionInMatching.getAsInt();
            int nextPosition = currentPosition == 0 ? matchingRepositories.size() - 1 : currentPosition - 1;
            matchingRepositories.get(currentPosition).setSelected(false);
            matchingRepositories.get(nextPosition).setSelected(true);
            setSelectedRepositoryId(matchingRepositories.get(nextPosition).getRepositoryId());
        }
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
    }

    /**
     * Clears previous selection and select user input as selected repository id since
     * there can only be at most one selected repository at any given time in the list
     * of stored repositories.
     */
    private void updateSelectedRepository(String query) {
        repositories.stream().forEach(repo -> repo.setSelected(false));
        setSelectedRepositoryId(query);
    }

    private void setSelectedRepositoryId(String query) {
        selectedRepositoryId = query;
    }

    /**
     * Returns a sorted list of PickerRepository which matches the current user input.
     */
    public List<PickerRepository> getMatchingRepositories() {
        return new ArrayList<>(matchingRepositories);
    }

    private boolean isMatching(PickerRepository repo, String query) {
        return repo.getRepositoryId().toLowerCase().contains(query.toLowerCase());
    }

}
