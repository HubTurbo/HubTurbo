package ui.components.pickers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RepositoryPickerState {

    List<PickerRepository> repositories = new ArrayList<>();

    public RepositoryPickerState(Set<String> storedRepositories) {
        storedRepositories.stream()
                .forEach(repo -> repositories.add(new PickerRepository(repo)));
    }

    public void updateQuery(String query) {
        updateMatchingRepo(query);
        sortBasedOnRelevance();
    }

    public List<PickerRepository> getRepositories() {
        return repositories;
    }

    private void updateMatchingRepo(String query) {
        repositories.stream()
                .forEach(repo -> repo.updateMatchingPositions(query));
    }

    private void sortBasedOnRelevance() {
        Collections.sort(repositories);
    }

}
