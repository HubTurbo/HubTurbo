package tests;

import org.junit.Test;
import prefs.RepoInfo;
import ui.components.pickers.PickerRepository;
import ui.components.pickers.RepositoryPickerState;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RepositoryPickerStateTests {
    @Test
    public void constructor_constructRepoPickerState_oneRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo");
        assertEquals(Optional.of("atom/atom"), state.getSelectedRepositoryId());
    }

    @Test
    public void processUserQuery_queryIsEmpty_queryNotAddedToSuggestedRepos() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        PickerRepository repoA = new PickerRepository(new RepoInfo("atom/atom"));
        PickerRepository repoB = new PickerRepository(new RepoInfo("HubTurbo/HubTurbo"));
        PickerRepository repoC = new PickerRepository(new RepoInfo("org/repoId"));
        List<PickerRepository> expected = Arrays.asList(repoA, repoB, repoC);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryMatchesExistingRepo_queryNotAddedToSuggestedRepos() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("atom/atom");
        PickerRepository repoA = new PickerRepository(new RepoInfo("atom/atom"));
        List<PickerRepository> expected = Arrays.asList(repoA);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void processUserQuery_queryIsNotEmpty_matchingRepoAddedToSuggestedList() {
        //Set<String> existingRepositories = new HashSet<>(Arrays.asList("atom/atom", "atom/tree-view", "org/repoId"));
        List<RepoInfo> existingRepositories
                = Arrays.asList(new RepoInfo("atom/atom"), new RepoInfo("atom/tree-view"), new RepoInfo("org/repoId"));
        RepositoryPickerState state = new RepositoryPickerState(existingRepositories);
        state.processUserQuery("atom");
        PickerRepository repoA = new PickerRepository(new RepoInfo("atom/atom"));
        PickerRepository repoB = new PickerRepository(new RepoInfo("atom/tree-view"));
        List<PickerRepository> expected = Arrays.asList(repoA, repoB);
        assertEquals(expected, state.getSuggestedRepositories());
        state.processUserQuery("z");
        expected = new ArrayList<>();
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedNotAtTheEnd_nextRepositoryInSuggestedIsSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        assertEquals(Optional.of("atom/atom"), state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals(Optional.of("HubTurbo/HubTurbo"), state.getSelectedRepositoryId());
        state.selectNextSuggestedRepository();
        assertEquals(Optional.of("org/repoId"), state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedNotAtTheBeginning_prevRepositoryInSuggestedIsSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        assertEquals(Optional.of("org/repoId"), state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals(Optional.of("HubTurbo/HubTurbo"), state.getSelectedRepositoryId());
        state.selectPreviousSuggestedRepository();
        assertEquals(Optional.of("atom/atom"), state.getSelectedRepositoryId());
    }

    @Test
    public void selectNextSuggestedRepository_currentSelectedRepoAtTheEnd_firstRepoInSuggestedListSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        state.selectNextSuggestedRepository();
        assertEquals(Optional.of("atom/atom"), state.getSelectedRepositoryId());
    }

    @Test
    public void selectPrevSuggestedRepository_currentSelectedRepoAtTheBeginning_lastRepoInSuggestedListSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.processUserQuery("");
        state.setSelectedRepositoryInSuggestedList("atom/atom");
        state.selectPreviousSuggestedRepository();
        assertEquals(Optional.of("org/repoId"), state.getSelectedRepositoryId());
    }

    @Test
    public void setSelectedRepository_doesNotUpdatedSuggestedRepoList() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        List<String> expectedRepoIds = Arrays.asList("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        List<PickerRepository> expected = expectedRepoIds.stream()
                                          .map(repoId -> new PickerRepository(new RepoInfo(repoId)))
                                          .collect(Collectors.toList());
        assertEquals(expected, state.getSuggestedRepositories());
    }

    @Test
    public void setSelectedRepository_suggestedRepositoryDoesNotContainUserInput_correctRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "HubTurbo/HubTurbo", "org/repoId");
        assertEquals(Optional.of("atom/atom"), state.getSelectedRepositoryId());
        state.setSelectedRepositoryInSuggestedList("org/repoId");
        assertEquals(Optional.of("org/repoId"), state.getSelectedRepositoryId());
    }

    @Test
    public void setSelectedRepository_suggestedRepositoryContainsUserInput_correctRepoSelected() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "atom/tree-view", "org/repoId");
        state.processUserQuery("a");
        verifySelectedRepository(state, "atom/atom");
        state.setSelectedRepositoryInSuggestedList("atom/tree-view");
        verifySelectedRepository(state, "atom/tree-view");
    }

    @Test
    public void addRepository_suggestedRepositoriesCleared() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "atom/tree-view", "org/repoId");
        state.processUserQuery("a");
        PickerRepository repoA = new PickerRepository(new RepoInfo("atom/atom"));
        PickerRepository repoB = new PickerRepository(new RepoInfo("atom/tree-view"));
        List<PickerRepository> expected = Arrays.asList(repoA, repoB);
        assertEquals(expected, state.getSuggestedRepositories());
        state.addRepository(new RepoInfo("atom/status-bar"));
        assertEquals(0, state.getSuggestedRepositories().size());
    }

    @Test
    public void addRepository_processInputAfterAdding_newRepoAddedToSuggestedRepositories() {
        RepositoryPickerState state = createRepoPickerStateFromRepoIds("atom/atom", "atom/tree-view", "org/repoId");
        state.processUserQuery("a");
        PickerRepository repoA = new PickerRepository(new RepoInfo("atom/atom"));
        PickerRepository repoB = new PickerRepository(new RepoInfo("atom/tree-view"));
        PickerRepository repoD = new PickerRepository(new RepoInfo("atom/status-bar"));
        state.addRepository(new RepoInfo("atom/status-bar"));
        assertEquals(0, state.getSuggestedRepositories().size());
        state.processUserQuery("a");
        List<PickerRepository> expected = Arrays.asList(repoA, repoD, repoB);
        assertEquals(expected, state.getSuggestedRepositories());
    }

    private RepositoryPickerState createRepoPickerStateFromRepoIds(String... repoId) {
        //Set<String> existingRepositories = new HashSet<>(Arrays.asList(repoId));
        List<RepoInfo> existingRepositories
                = Arrays.asList(repoId).stream().map(e -> new RepoInfo(e)).collect(Collectors.toList());
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
