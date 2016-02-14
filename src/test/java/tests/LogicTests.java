package tests;

import backend.Logic;
import backend.RepoIO;
import backend.UIManager;
import backend.control.RepoOpControl;
import backend.resource.Model;
import backend.resource.MultiModel;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogicTests {
    private final Logic logic;
    private final RepoIO mockedRepoIO;
    private final MultiModel mockedMultiModel;

    public LogicTests() throws NoSuchFieldException, IllegalAccessException {
        Preferences mockedPreferences = mock(Preferences.class);
        when(mockedPreferences.getLastViewedRepository()).thenReturn(Optional.empty());
        UI.events = mock(EventDispatcher.class);

        mockedRepoIO = mock(RepoIO.class);
        mockedMultiModel = mock(MultiModel.class);

        logic = new Logic(mock(UIManager.class), mockedPreferences, Optional.of(mockedMultiModel));
        Field repoOpControlField = logic.getClass().getDeclaredField("repoOpControl");
        repoOpControlField.setAccessible(true);
        repoOpControlField.set(logic, new RepoOpControl(mockedRepoIO));
    }

    /**
     * Tests that replaceIssueLabels succeed when both models and repoIO succeeded
     */
    @Test
    public void testReplaceIssueLabelsSuccessful() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(true);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.empty());

        assertTrue(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    /**
     * Tests that replaceIssueLabels failed when models return empty result
     */
    @Test
    public void testReplaceIssueLabelsRepoIOFailed() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(true);
        mockMultiModelReplaceIssueLabels(Optional.empty(), Optional.empty());

        assertFalse(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    /**
     * Tests that replaceIssueLabels failed when repoIO failed to update labels
     */
    @Test
    public void testReplaceIssueLabelsModelsFailed() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(false);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.empty());

        assertFalse(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    private void mockRepoIOReplaceIssueLabelsResult(boolean replaceResult) {
        when(mockedRepoIO.replaceIssueLabels(any(TurboIssue.class), anyListOf(String.class)))
                .thenReturn(CompletableFuture.completedFuture(replaceResult));
    }

    private void mockMultiModelReplaceIssueLabels(Optional<TurboIssue> replaceResult,
                                                  Optional<Model> modelLookUpResult) {
        when(mockedMultiModel.replaceIssueLabels(anyString(), anyInt(), anyListOf(String.class)))
                .thenReturn(replaceResult);
        when(mockedMultiModel.getModelById(anyString())).thenReturn(modelLookUpResult);
    }

    public static TurboIssue createIssueWithLabels(List<String> labels) {
        TurboIssue issue = new TurboIssue("testowner/testrepo", 1, "Issue title");
        issue.setLabels(labels);
        return issue;
    }
}
