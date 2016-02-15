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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
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
     * Tests that all issue's labels are replaced with new labels when
     * the new labels are different from the current ones
     */
    @Test
    public void testReplaceIssueLabelsNonOverlapping() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(Arrays.asList("label1", "label2"));
        List<String> newLabels = Arrays.asList("label3", "label4");
        mockRepoIOReplaceIssueLabelsResult(newLabels);

        boolean status = logic.replaceIssueLabels(issue, newLabels).get();

        assertTrue(status);
        assertEquals(newLabels, issue.getLabels());
    }

    /**
     * Tests that all issue's labels remain the same if the new labels are the same as the current ones
     */
    @Test
    public void testReplaceIssueLabelsNoChange() throws ExecutionException, InterruptedException {
        List<String> labels = Arrays.asList("label1", "label2");
        TurboIssue issue = createIssueWithLabels(labels);
        mockRepoIOReplaceIssueLabelsResult(labels);

        boolean status = logic.replaceIssueLabels(issue, labels).get();

        assertTrue(status);
        assertEquals(labels, issue.getLabels());
    }

    /**
     * Tests that the returned status is false when the returned result from RepoIO is inconsistent
     * with the given labels argument
     */
    @Test
    public void testReplaceIssueLabelsInconsistent() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(Arrays.asList("label1", "label2", "label3"));
        mockRepoIOReplaceIssueLabelsResult(Arrays.asList("label3", "label4"));

        boolean status = logic.replaceIssueLabels(issue, Arrays.asList("label1", "label2")).get();

        assertFalse(status);
    }

    private void mockRepoIOReplaceIssueLabelsResult(List<String> resultLabels) {
        CompletableFuture<List<String>> resultFuture = new CompletableFuture<>();
        resultFuture.complete(resultLabels);
        when(mockedRepoIO.replaceIssueLabels(any(TurboIssue.class), anyListOf(String.class)))
                .thenReturn(resultFuture);
    }

    public static TurboIssue createIssueWithLabels(List<String> labels) {
        TurboIssue issue = new TurboIssue("testowner/testrepo", 1, "Issue title");
        issue.setLabels(labels);
        return issue;
    }
}
