package tests;

import backend.Logic;
import backend.RepoIO;
import backend.UIManager;
import backend.control.RepoOpControl;
import backend.resource.TurboIssue;
import org.junit.Test;
import prefs.Preferences;
import ui.UI;
import util.events.EventDispatcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogicTests {
    private final Logic logic;
    private final RepoIO mockedRepoIO;

    public LogicTests() throws NoSuchFieldException, IllegalAccessException {
        Preferences mockedPreferences = mock(Preferences.class);
        when(mockedPreferences.getLastViewedRepository()).thenReturn(Optional.empty());
        UI.events = mock(EventDispatcher.class);

        mockedRepoIO = mock(RepoIO.class);

        logic = new Logic(mock(UIManager.class), mockedPreferences);
        Field repoOpControlField = logic.getClass().getDeclaredField("repoOpControl");
        repoOpControlField.setAccessible(true);
        repoOpControlField.set(logic, new RepoOpControl(mockedRepoIO));
    }

    /**
     * Tests that all issue's labels are removed if an empty list is passed into replaceIssueLabels
     */
    @Test
    public void testReplaceIssueLabelsEmptyList() throws ExecutionException, InterruptedException {
        TurboIssue issue = new TurboIssue("testowner/testrepo", 1, "Issue title");
        List<String> oldLabels = Arrays.asList("label1", "label2");
        issue.setLabels(oldLabels);

        CompletableFuture<List<String>> resultLabels = new CompletableFuture<>();
        resultLabels.complete(new ArrayList<>());
        when(mockedRepoIO.replaceIssueLabels(any(TurboIssue.class), anyListOf(String.class)))
                .thenReturn(resultLabels);

        boolean status = logic.replaceIssueLabels(issue, new ArrayList<>()).get();

        assertTrue(status);
        assertEquals(0, issue.getLabels().size());
    }

    /**
     * Tests that all issue's labels are replaced with new labels passed in
     */
    @Test
    public void  testReplaceIssueLabels() throws ExecutionException, InterruptedException {
        TurboIssue issue = new TurboIssue("testowner/testrepo", 1, "Issue title");
        List<String> oldLabels = Arrays.asList("label1", "label2");
        issue.setLabels(oldLabels);

        List<String> newLabels = Arrays.asList("label3", "label4");
        CompletableFuture<List<String>> resultLabels = new CompletableFuture<>();
        resultLabels.complete(newLabels);
        when(mockedRepoIO.replaceIssueLabels(any(TurboIssue.class), anyListOf(String.class)))
                .thenReturn(resultLabels);

        boolean status = logic.replaceIssueLabels(issue, newLabels).get();

        assertTrue(status);
        assertEquals(resultLabels.get(), issue.getLabels());
    }
}
