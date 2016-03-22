package tests;

import backend.IssueMetadata;
import backend.RepoIO;
import backend.interfaces.RepoSource;
import backend.resource.TurboIssue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RepoIOTests {
    /**
     * Tests that RepoIO's getIssueMetadata calls RepoSource's downloadMetaData method and
     * receives a corresponding CompletableFuture response
     */
    @Test
    public void testGetIssueMetaData() {
        RepoSource source = mock(RepoSource.class);
        CompletableFuture<Map<Integer, IssueMetadata>> response = new CompletableFuture<>();
        doReturn(response).when(source).downloadMetadata(anyString(), anyListOf(TurboIssue.class));

        RepoIO repoIO = new RepoIO(Optional.of(source), Optional.empty(), Optional.empty());
        CompletableFuture result = repoIO.getIssueMetadata("test/test", new ArrayList<>());

        verify(source, times(1)).downloadMetadata(anyString(), anyListOf(TurboIssue.class));
        assertEquals(response, result);
    }

    /**
     * Tests that RepoIO's replaceIssueLabelsOnServer calls RepoSource's replaceIssueLabelsOnServer
     * and receives a corresponding CompletableFuture response
     */
    @Test
    public void testReplaceIssueLabels() {
        RepoSource source = mock(RepoSource.class);
        CompletableFuture<List<String>> response = new CompletableFuture<>();
        doReturn(response).when(source).replaceIssueLabels(any(TurboIssue.class),
                                                           anyListOf(String.class));

        RepoIO repoIO = new RepoIO(Optional.of(source), Optional.empty(), Optional.empty());
        CompletableFuture result = repoIO.replaceIssueLabels(mock(TurboIssue.class), new ArrayList<>());

        verify(source, times(1)).replaceIssueLabels(any(TurboIssue.class), anyListOf(String.class));
        assertEquals(response, result);
    }
}
