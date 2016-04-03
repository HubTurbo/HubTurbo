package tests;

import org.junit.Test;
import ui.components.pickers.PickerRepository;
import ui.components.pickers.RepositoryPickerState;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RepositoryPickerStateTests {
    @Test
    public void constructor_constructRepoPickerState_oneRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo");
        assertEquals("atom/atom", state.getSelectedRepositoryId());
    }

    @Test
    public void processUserQuery_queryIsEmpty_queryNotAddedToSuggestedRepos() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        PickerRepository repoA = new PickerRepository("atom/atom");
        PickerRepository repoB = new PickerRepository("HubTurbo/HubTurbo");
        PickerRepository repoC = new PickerRepository("org/repoId");
        List<PickerRepository> expected = Arrays.asList(repoA, repoB, repoC);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryMatchesExistingRepo_queryNotAddedToSuggestedRepos() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("atom/atom");
        PickerRepository repoA = new PickerRepository("atom/atom");
        List<PickerRepository> expected = Arrays.asList(repoA);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryIsNotEmpty_queryAddedToSuggestedRepos() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "atom/tree-view", "org/repoId");
        state.processUserQuery("atom");
        PickerRepository userQueryRepo = new PickerRepository("atom");
        PickerRepository repoA = new PickerRepository("atom/atom");
        PickerRepository repoB = new PickerRepository("atom/tree-view");
        List<PickerRepository> expected = Arrays.asList(userQueryRepo, repoA, repoB);
        assertEquals(expected, state.getSuggestedRepositories());
        state.processUserQuery("z");
        userQueryRepo = new PickerRepository("z");
        expected = Arrays.asList(userQueryRepo);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedNotAtTheEnd_nextRepositoryInSuggestedIsSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        assertEquals("atom/atom", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("HubTurbo/HubTurbo", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("org/repoId", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedNotAtTheBeginning_prevRepositoryInSuggestedIsSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        assertEquals("org/repoId", state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals("HubTurbo/HubTurbo", state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals("atom/atom", state.getSelectedRepositoryId());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedRepoAtTheEnd_firstRepoInSuggestedListSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        state.selectNextSuggestedRepository();
        assertEquals("atom/atom", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedRepoAtTheBeginning_lastRepoInSuggestedListSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("atom/atom");
        state.selectPreviousSuggestedRepository();
        assertEquals("org/repoId", state.getSelectedRepositoryId());
    }

    @Test
    public void setSelectedRepository_doesNotUpdatedSuggestedRepoList() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        List<String> expectedRepoIds = Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        List<PickerRepository> expected = expectedRepoIds.stream()
                                          .map(repoId -> new PickerRepository(repoId))
                                          .collect(Collectors.toList());
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void setSelectedRepository_suggestedRepositoryDoesNotContainUserInput_correctRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        assertEquals("atom/atom", state.getSelectedRepositoryId());
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        assertEquals("org/repoId", state.getSelectedRepositoryId());
    }

    @Test
    public void setSelectedRepository_suggestedRepositoryContainsUserInput_correctRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "atom/tree-view", "org/repoId");
        state.processUserQuery("a");
        verifySelectedRepository(state, "a");
        state.setSelectedRepositoryInSuggestedList("atom/tree-view");
        verifySelectedRepository(state, "atom/tree-view");
    }

    private RepositoryPickerState createRepoPickerStateFromRepoIds(String... repoId) {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList(repoId));
        return new RepositoryPickerState(existingRepositories);
    }

    private void verifySelectedRepository(RepositoryPickerState state, String expectedSelectedRepository) {
        List<PickerRepository> suggestedRepos = state.getSuggestedRepositories();
        PickerRepository selectedRepo = suggestedRepos.stream()
                .filter(repo -> repo.isSelected())
                .findFirst()
                .get();
        assertEquals(expectedSelectedRepository, selectedRepo.getRepositoryId());
    }
}
