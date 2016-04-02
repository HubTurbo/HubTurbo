package ui.components.pickers;

import org.controlsfx.control.spreadsheet.Picker;

import java.util.*;
import java.util.stream.IntStream;

/**
 * This class represents a state in RepositoryPicker which stores the list of existing repositories and currently
 * selected repository and handles all logic related to RepositoryPickerDialog.
 *
 * It is guaranteed that at all time, there will be exactly one selected repository.
 */
public class RepositoryPickerState {

    private final List<PickerRepository> repositories = new ArrayList<>();
    private final List<PickerRepository> suggestedRepositories = new ArrayList<>();

    public RepositoryPickerState(Set<String> storedRepositories) {
        assert !storedRepositories.isEmpty() : "There should be at least one existing repository";
        storedRepositories.stream()
                .forEach(repo -> repositories.add(new PickerRepository(repo)));
        Collections.sort(repositories);
        initialiseDefaultValues();
    }

    /**
     * Adds all repositories to the list of suggested repositories.
     * This also selects the first repository in suggestedRepository.
     */
    private void initialiseDefaultValues() {
        suggestedRepositories.addAll(repositories);
        suggestedRepositories.get(0).setSelected(true);
    }

    public void processUserQuery(String query) {
        clearRepositorySelection();
        updateSuggestedRepositories(query);
    }

    public String getSelectedRepositoryId() {
        Optional<PickerRepository> selectedRepository = suggestedRepositories.stream()
                .filter(repo -> repo.isSelected())
                .findFirst();
        assert selectedRepository.isPresent();
        return selectedRepository.get().getRepositoryId();
    }

    /**
     * Selects a repository right after the current selected repository in the sorted
     * suggested repository list. If the current selected repository is the last one,
     * it will then select the first suggested repository instead.
     */
    public void selectNextSuggestedRepository() {
        OptionalInt selectedPositionInSuggested = getSelectedRepositoryPositionInSuggested();
        assert selectedPositionInSuggested.isPresent();
        int currentPosition = selectedPositionInSuggested.getAsInt();
        int nextPosition = currentPosition == suggestedRepositories.size() - 1 ? 0 : currentPosition + 1;
        suggestedRepositories.get(currentPosition).setSelected(false);
        suggestedRepositories.get(nextPosition).setSelected(true);
    }

    /**
     * Selects a repository right before the current selected repository in the sorted
     * suggested repository list. If the current selected repository is the first one,
     * it will then select the last suggested repository instead.
     */
    public void selectPreviousSuggestedRepository() {
        OptionalInt selectedPositionInSuggested = getSelectedRepositoryPositionInSuggested();
        assert selectedPositionInSuggested.isPresent();
        int currentPosition = selectedPositionInSuggested.getAsInt();
        int nextPosition = currentPosition == 0 ? suggestedRepositories.size() - 1 : currentPosition - 1;
        suggestedRepositories.get(currentPosition).setSelected(false);
        suggestedRepositories.get(nextPosition).setSelected(true);
    }

    /**
     * Selects a repository in the suggested list. The selected repositoryId must exist
     * in suggestedRepositories list.
     */
    public void setSelectedRepositoryInSuggestedList(String repositoryId) {
        Optional<PickerRepository> pickerRepository = getPickerRepositoryById(suggestedRepositories, repositoryId);
        assert pickerRepository.isPresent();
        clearRepositorySelection();
        pickerRepository.get().setSelected(true);
    }

    private OptionalInt getSelectedRepositoryPositionInSuggested() {
        return IntStream.range(0, suggestedRepositories.size())
                .filter(index -> suggestedRepositories.get(index).isSelected())
                .findFirst();
    }

    /**
     * Updates List<PickerRepository> suggestedRepositories so that it contains PickerRepositories that match
     * the user input.
     *
     * If there is no existing repository that matches the user's query exactly, it will be added to the list of
     * suggested repositories so that the user can pick the new repository.
     */
    private void updateSuggestedRepositories(String query) {
        suggestedRepositories.clear();
        repositories.stream().forEach(repo -> {
            if (isMatching(repo, query)) {
                suggestedRepositories.add(repo);
            }
        });
        Optional<PickerRepository> possibleExactMatch = getPickerRepositoryById(suggestedRepositories, query);
        if (query.isEmpty()) {
            assert !suggestedRepositories.isEmpty();
            PickerRepository firstMatching = suggestedRepositories.get(0);
            firstMatching.setSelected(true);
            return;
        }
        if (!possibleExactMatch.isPresent()) {
            PickerRepository newRepository = new PickerRepository(query);
            newRepository.setSelected(true);
            suggestedRepositories.add(0, newRepository);
            return;
        }
        PickerRepository exactMatch = possibleExactMatch.get();
        exactMatch.setSelected(true);
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
     * The first entry suggestedRepository is always the user input.
     */
    public List<PickerRepository> getSuggestedRepositories() {
        return new ArrayList<>(suggestedRepositories);
    }

    private boolean isMatching(PickerRepository repo, String query) {
        return repo.getRepositoryId().toLowerCase().contains(query.toLowerCase());
    }

}
