package tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import github.PullRequestServiceEx;
import github.ReviewComment;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PullRequestServiceExTests {
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentsToGetReviewComments1() throws IOException {
        GitHubClientEx client = new GitHubClientEx();
        PullRequestServiceEx service = new PullRequestServiceEx(client);
        service.getReviewComments(null, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentsToGetReviewComments2() throws IOException {
        GitHubClientEx client = new GitHubClientEx();
        PullRequestServiceEx service = new PullRequestServiceEx(client);
        service.getReviewComments(RepositoryId.createFromId("testrepo/testrepo"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentsToGetReviewComments3() throws IOException {
        GitHubClientEx client = new GitHubClientEx();
        PullRequestServiceEx service = new PullRequestServiceEx(client);
        service.getReviewComments(RepositoryId.createFromId("testrepo/testrepo"), "");
    }

    @Test(expected = IOException.class)
    public void testInvalidRepositoryInGetReviewComments() throws IOException {
        GitHubClientEx client = new GitHubClientEx();
        PullRequestServiceEx service = new PullRequestServiceEx(client);
        service.getReviewComments(RepositoryId.createFromId("fakeowner/bogusrepo"), 1);
    }

    /**
     * Tests that getReviewComments method correctly receives 1 page of review comments
     * returned from a MockServer that emulates GitHub API service.
     */
    @Test
    public void testGetReviewComments() throws IOException {
        MockServerClient mockServer = ClientAndServer.startClientAndServer(8888);
        String sampleComments = TestUtils.readFileFromResource(this, "tests/ReviewCommentsSample.json");

        mockServer.when(
                request()
                        .withPath(TestUtils.API_PREFIX + "/repos/hubturbo/hubturbo/pulls/1125/comments")
                        .withQueryStringParameters(
                                new Parameter("per_page", "100"),
                                new Parameter("page", "1")
                        )
        ).respond(response().withBody(sampleComments));

        GitHubClient client = new GitHubClient("localhost", 8888, "http");
        PullRequestServiceEx service = new PullRequestServiceEx(client);

        Type listOfComments = new TypeToken<List<ReviewComment>>() {
        }.getType();
        List<ReviewComment> expectedComments = new Gson().fromJson(sampleComments, listOfComments);
        List<ReviewComment> actualComments = service.getReviewComments(
                RepositoryId.createFromId("hubturbo/hubturbo"), 1125);

        assertEquals(expectedComments.size(), actualComments.size());

        Comparator<ReviewComment> comparator = (a, b) -> (int) (a.getId() - b.getId());
        Collections.sort(expectedComments, comparator);
        Collections.sort(actualComments, comparator);

        for (int i = 0; i < expectedComments.size(); i++) {
            assertEquals(expectedComments.get(i).getId(), actualComments.get(i).getId());
        }

        mockServer.stop();

    }
}
