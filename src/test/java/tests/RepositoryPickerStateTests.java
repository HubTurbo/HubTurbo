package tests;

import org.junit.Test;
import ui.components.pickers.PickerRepository;
import ui.components.pickers.RepositoryPickerState;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RepositoryPickerStateTests {
    @Test
    public void constructor_constructRepoPickerState_oneRepoSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        assertEquals("atom/atom", state.getSelectedRepositoryId());
    }

    @Test
    public void processUserQuery_queryIsEmpty_queryNotAddedToSuggestedRepos() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        PickerRepository repoA = new PickerRepository("atom/atom");
        PickerRepository repoB = new PickerRepository("HubTurbo/HubTurbo");
        PickerRepository repoC = new PickerRepository("org/repoId");
        List<PickerRepository> expected = Arrays.asList(repoA, repoB, repoC);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryMatchesExistingRepo_queryNotAddedToSuggestedRepos() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("atom/atom");
        PickerRepository repoA = new PickerRepository("atom/atom");
        List<PickerRepository> expected = Arrays.asList(repoA);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryIsNotEmpty_queryAddedToSuggestedRepos() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "atom/tree-view", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
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
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        assertEquals("atom/atom", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("HubTurbo/HubTurbo", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("org/repoId", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedNotAtTheBeginning_prevRepositoryInSuggestedIsSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
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
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        state.selectNextSuggestedRepository();
        assertEquals("atom/atom", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedRepoAtTheBeginning_lastRepoInSuggestedListSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("atom/atom");
        state.selectPreviousSuggestedRepository();
        assertEquals("org/repoId", state.getSelectedRepositoryId());
    }
}
