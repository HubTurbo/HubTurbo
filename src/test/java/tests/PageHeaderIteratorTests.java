package tests;

import github.GitHubClientEx;
import github.update.PageHeaderIterator;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PageHeaderIteratorTests {
    /**
     * Tests that PageHeaderIterator throws a NoSuchElement exception when its next
     * method is called with a initial request to an non-existent repository
     * @throws NoSuchElementException
     */
    @Test(expected = NoSuchElementException.class)
    public void testHeaderIteratorInvalidRepo() throws NoSuchElementException {
        GitHubClientEx client = new GitHubClientEx();

        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        PagedRequest<Milestone> request = new PagedRequest<>();
        String path = SEGMENT_REPOS + "/nonexistentrepo";
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        PageHeaderIterator iter = new PageHeaderIterator(request, client, "ETag");
        if (iter.hasNext()) {
            iter.next();
        }
    }

    /**
     * Tests that a PageHeaderIterator correctly retrieves ETag headers for 3 pages from
     * a mocked server that conform to GitHub API's pagination specifications and terminates afterwards.
     * @throws NoSuchElementException
     */
    @Test
    public void testHeaderIterator() throws NoSuchElementException, IOException {
        MockServerClient mockServer = ClientAndServer.startClientAndServer(8888);

        HttpRequest page1Request = createMockServerPagedHeaderRequest("1");
        String page1Etag = "aaf65fc6b10d5afbdc9cd0aa6e6ada4c";
        String page1Link =
             "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=2>; rel=\"next\", "
           + "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=3>; rel=\"last\"";

        HttpRequest page2Request = createMockServerPagedHeaderRequest("2");
        String page2Etag = "731501e0f7d9816305782bc4c3f70d9f";
        String page2Link =
              "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=3>; rel=\"next\", "
            + "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=3>; rel=\"last\", "
            + "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=1>; rel=\"first\", "
            + "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=1>; rel=\"prev\"";

        HttpRequest page3Request = createMockServerPagedHeaderRequest("3");
        String page3Etag = "a6f367d674155d6fbbacbc2fca04917b";
        String page3Link =
              "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=1>; rel=\"first\", "
            + "<https://localhost:8888/repositories/20919534/pulls?state=all&per_page=100&page=2>; rel=\"prev\"";

        setUpHeadRequestOnMockServer(mockServer, page1Request, page1Etag, page1Link);
        setUpHeadRequestOnMockServer(mockServer, page2Request, page2Etag, page2Link);
        setUpHeadRequestOnMockServer(mockServer, page3Request, page3Etag, page3Link);

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        GitHubClientEx client = new GitHubClientEx("localhost", 8888, "http");

        String path = SEGMENT_REPOS + "/hubturbo/hubturbo" + SEGMENT_PULLS;
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        PageHeaderIterator iter = new PageHeaderIterator(request, client, "ETag");
        assertEquals(page1Etag, iter.next());
        assertEquals(page2Etag, iter.next());
        assertEquals(page3Etag, iter.next());
        assertFalse(iter.hasNext());

        mockServer.stop();
    }

    private HttpRequest createMockServerPagedHeaderRequest(String page) {
        String path = page.equals("1") ?
                "/api/v3/repos/hubturbo/hubturbo/pulls" : "/api/v3/repositories/20919534/pulls";

        return request()
                .withMethod("HEAD")
                .withPath(path)
                .withQueryStringParameters(
                        new Parameter("state", "all"),
                        new Parameter("per_page", "100"),
                        new Parameter("page", page)
                );
    }

    private void setUpHeadRequestOnMockServer(MockServerClient mockServer, HttpRequest request,
                                              String eTagHeader, String linkHeader) {
        mockServer
                .when(request)
                .respond(
                        response()
                                .withHeaders(
                                        new Header("ETag", eTagHeader),
                                        new Header("Link", linkHeader)
                                )
                );
    }
}
