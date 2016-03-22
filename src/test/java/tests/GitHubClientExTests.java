package tests;

import github.GitHubClientEx;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class GitHubClientExTests {
    /**
     * Tests that head request to nonexistent repo throws an exception
     *
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public void testInvalidHeadRequest() throws IOException {
        GitHubClientEx client = new GitHubClientEx();

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        String path = SEGMENT_REPOS + "/nonexistentrepo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        client.head(request);
    }

    /**
     * Tests that GitHubClientEx' head method makes a HTTP HEAD request and receive a corresponding
     * header response from a mocked server
     *
     * @throws IOException
     */
    @Test
    public void testValidHeadRequest() throws IOException {
        MockServerClient mockServer = ClientAndServer.startClientAndServer(8888);
        HttpRequest expectedRequest = request()
                .withMethod("HEAD")
                .withPath(TestUtils.API_PREFIX + "/repos/repo")
                .withQueryStringParameters(
                        new Parameter("state", "all"),
                        new Parameter("per_page", "100"),
                        new Parameter("page", "1")
                );
        String eTagValue = "aaf65fc6b10d5afbdc9cd0aa6e6ada4c";

        mockServer
                .when(expectedRequest)
                .respond(response().withHeader("ETag", eTagValue));

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        GitHubClientEx client = new GitHubClientEx("localhost", 8888, "http");

        String path = SEGMENT_REPOS + "/repo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);
        ImmutablePair<HttpURLConnection, GitHubResponse> result = client.head(request);

        mockServer.verify(expectedRequest, VerificationTimes.exactly(1));
        assertEquals(eTagValue, result.getRight().getHeader("ETag"));

        mockServer.stop();
    }
}
