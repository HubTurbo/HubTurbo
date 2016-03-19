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
        updateSelection(query);
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
        OptionalInt selectedPositionInMatching = getSelectedPositionInMatching();
        if (!selectedPositionInMatching.isPresent()) {
            // if there is no previous selection, select the first matching repository if it exists
            if (!matchingRepositories.isEmpty()) {
                PickerRepository toSelect = matchingRepositories.get(0);
                toSelect.setSelected(true);
                setSelectedRepositoryId(toSelect.getRepositoryId());
            }
        } else {
            int currentSelectedPosition = selectedPositionInMatching.getAsInt();
            int nextSelectedPosition = (currentSelectedPosition + 1) % matchingRepositories.size();
            matchingRepositories.get(currentSelectedPosition).setSelected(false);
            matchingRepositories.get(nextSelectedPosition).setSelected(true);
            setSelectedRepositoryId(matchingRepositories.get(nextSelectedPosition).getRepositoryId());
        }
    }

    /**
     * Selects a repository right before the current selected repository in the sorted
     * matching repository list. If the current selected repository is the first one,
     * it will then select the last matching repository instead.
     */
    public void selectPreviousMatchingRepository() {
        OptionalInt selectedPositionInMatching = getSelectedPositionInMatching();
        if (!selectedPositionInMatching.isPresent()) {
            // if there is no previous selection, select the last matching repository if it exists
            if (!matchingRepositories.isEmpty()) {
                PickerRepository toSelect = matchingRepositories.get(matchingRepositories.size() - 1);
                toSelect.setSelected(true);
                setSelectedRepositoryId(toSelect.getRepositoryId());
            }
        } else {
            int currentSelectedPosition = selectedPositionInMatching.getAsInt();
            int nextSelectedPosition = currentSelectedPosition == 0 ? matchingRepositories.size() - 1
                                                                    : currentSelectedPosition - 1;
            matchingRepositories.get(currentSelectedPosition).setSelected(false);
            matchingRepositories.get(nextSelectedPosition).setSelected(true);
            setSelectedRepositoryId(matchingRepositories.get(nextSelectedPosition).getRepositoryId());
        }
    }

    private OptionalInt getSelectedPositionInMatching() {
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
     * Clears previous selection and select user input as selected repository id.
     */
    private void updateSelection(String query) {
        repositories.stream().forEach(repo -> {
            repo.setSelected(false);
        });
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
