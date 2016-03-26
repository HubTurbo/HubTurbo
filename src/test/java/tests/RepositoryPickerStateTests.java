package tests;

import org.junit.Test;
import ui.components.pickers.PickerRepository;
import ui.components.pickers.RepositoryPickerState;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RepositoryPickerStateTests {
    @Test
    public void constructor_constructRepoPickerState_oneRepoSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        assertEquals("A/A", state.getSelectedRepositoryId());
    }

    @Test
    public void processUserQuery_queryIsEmpty_queryNotAddedToSuggestedRepos() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        PickerRepository repoA = new PickerRepository("A/A");
        PickerRepository repoB = new PickerRepository("B/B");
        PickerRepository repoC = new PickerRepository("C/d");
        List<PickerRepository> expected = Arrays.asList(repoA, repoB, repoC);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryIsNotEmpty_queryAddedToSuggestedRepos() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/AB", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("a/a");
        PickerRepository userQueryRepo = new PickerRepository("a/a");
        PickerRepository repoA = new PickerRepository("A/AB");
        List<PickerRepository> expected = Arrays.asList(userQueryRepo, repoA);
        assertEquals(expected, state.getSuggestedRepositories());
        state.processUserQuery("z");
        userQueryRepo = new PickerRepository("z");
        expected = Arrays.asList(userQueryRepo);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedNotAtTheEnd_nextRepositoryInSuggestedIsSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        assertEquals("A/A", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("B/B", state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals("C/d", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedNotAtTheBeginning_prevRepositoryInSuggestedIsSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("C/d");
        assertEquals("C/d", state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals("B/B", state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals("A/A", state.getSelectedRepositoryId());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedRepoAtTheEnd_firstRepoInSuggestedListSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("C/d");
        state.selectNextSuggestedRepository();
        assertEquals("A/A", state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedRepoAtTheBeginning_lastRepoInSuggestedListSelected() {
        Set<String> existingRepositories = new HashSet<>(Arrays.asList("A/A", "B/B", "C/d"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("A/A");
        state.selectPreviousSuggestedRepository();
        assertEquals("C/d", state.getSelectedRepositoryId());
    }
}
