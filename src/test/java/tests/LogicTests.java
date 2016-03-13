package tests;

import backend.Logic;
import backend.RepoIO;
import backend.UIManager;
import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import prefs.Preferences;
import ui.UI;
import util.events.EventDispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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

        logic = new Logic(mock(UIManager.class), mockedPreferences,
                          Optional.of(mockedRepoIO), Optional.of(mockedMultiModel));
    }

    @Before
    public void resetMockedObjects() {
        reset(mockedRepoIO);
        reset(mockedMultiModel);
    }

    /**
     * Tests that replaceIssueLabelsOnServer succeed when both models and repoIO succeeded
     */
    @Test
    public void replaceIssueLabels_successful() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(1, Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(true);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.empty());

        assertTrue(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    /**
     * Tests that replaceIssueLabelsOnServer failed when models return empty result
     */
    @Test
    public void replaceIssueLabels_modelsEmpty() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(1, Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(true);
        mockMultiModelReplaceIssueLabels(Optional.empty(), Optional.empty());

        assertFalse(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    /**
     * Tests that replaceIssueLabelsOnServer failed when repoIO failed to update labels
     */
    @Test
    public void replaceIssueLabels_repoIOUnsuccessful() throws ExecutionException, InterruptedException {
        TurboIssue issue = createIssueWithLabels(1, Arrays.asList("label1", "label2"));
        mockRepoIOReplaceIssueLabelsResult(false);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.empty());

        assertFalse(logic.replaceIssueLabels(issue, new ArrayList<>()).get());
    }

    /**
     * Tests that {@link MultiModel#replaceIssueLabels(String, int, List)} is first called with the
     * new labels then revert back to original labels when repoIO failed to update labels
     */
    @Test
    public void replaceIssueLabels_repoIOUnsuccessful_revert() throws ExecutionException, InterruptedException {
        List<String> originalLabels = Arrays.asList("label1", "label2");
        List<String> newLabels = Arrays.asList("label3", "label4");

        TurboIssue issue = createIssueWithLabels(1, originalLabels);
        mockRepoIOReplaceIssueLabelsResult(false);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.empty());

        logic.replaceIssueLabels(issue, newLabels).get();

        InOrder inOrder = inOrder(mockedMultiModel);
        inOrder.verify(mockedMultiModel).replaceIssueLabels(issue.getRepoId(), issue.getId(), newLabels);
        inOrder.verify(mockedMultiModel).replaceIssueLabels(issue.getRepoId(), issue.getId(), originalLabels);
    }

    /**
     * Tests that no revert is taken place if the issue's labels are modified elsewhere after
     * {@link Logic#replaceIssueLabels(TurboIssue, List)} is called
     */
    @Test
    public void replaceIssueLabels_timeNotMatched_noRevert() throws ExecutionException, InterruptedException {
        List<String> originalLabels = Arrays.asList("label1", "label2");
        List<String> newLabels = Arrays.asList("label3", "label4");

        TurboIssue issue = createIssueWithLabels(1, originalLabels);
        TurboIssue modifiedIssue = TestUtils.delayThenGet(
                10, () -> createIssueWithLabels(1, originalLabels));

        Model mockedModel = mock(Model.class);
        when(mockedModel.replaceIssueLabels(issue.getId(), newLabels)).thenReturn(Optional.of(issue));
        when(mockedModel.getIssueById(issue.getId())).thenReturn(Optional.of(modifiedIssue));
        mockRepoIOReplaceIssueLabelsResult(false);
        mockMultiModelReplaceIssueLabels(Optional.of(issue), Optional.of(mockedModel));

        logic.replaceIssueLabels(issue, newLabels).get();

        verify(mockedMultiModel, atMost(1))
                .replaceIssueLabels(anyString(), anyInt(), anyListOf(String.class));
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

    public static TurboIssue createIssueWithLabels(int issueId, List<String> labels) {
        TurboIssue issue = new TurboIssue("testowner/testrepo", issueId, "Issue title");
        issue.setLabels(labels);
        return issue;
    }
}
