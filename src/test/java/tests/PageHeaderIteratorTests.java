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
import util.Utility;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockserver.model.HttpResponse.response;

public class PageHeaderIteratorTests {
    /**
     * Tests that PageHeaderIterator throws a NoSuchElement exception when its next
     * method is called with a initial request to an non-existent repository
     *
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
     *
     * @throws NoSuchElementException
     */
    @Test
    public void testHeaderIterator() throws NoSuchElementException, IOException {
        MockServerClient mockServer = ClientAndServer.startClientAndServer(8888);

        HttpRequest page1Request = createMockServerPagedHeaderRequest(1);
        List<Header> page1Headers = TestUtils.parseHeaderRecord(
                TestUtils.readFileFromResource(this, "tests/PagedHeadersSample/page1-header.txt"));
        String page1Etag = "aaf65fc6b10d5afbdc9cd0aa6e6ada4c";

        HttpRequest page2Request = createMockServerPagedHeaderRequest(2);
        List<Header> page2Headers = TestUtils.parseHeaderRecord(
                TestUtils.readFileFromResource(this, "tests/PagedHeadersSample/page2-header.txt"));
        String page2Etag = "731501e0f7d9816305782bc4c3f70d9f";

        HttpRequest page3Request = createMockServerPagedHeaderRequest(3);
        List<Header> page3Headers = TestUtils.parseHeaderRecord(
                TestUtils.readFileFromResource(this, "tests/PagedHeadersSample/page3-header.txt")
        );
        String page3Etag = "a6f367d674155d6fbbacbc2fca04917b";

        setUpHeadRequestOnMockServer(mockServer, page1Request, page1Headers);
        setUpHeadRequestOnMockServer(mockServer, page2Request, page2Headers);
        setUpHeadRequestOnMockServer(mockServer, page3Request, page3Headers);

        PagedRequest<Milestone> request = new PagedRequest<>();
        Map<String, String> params = new HashMap<>();
        params.put("state", "all");

        GitHubClientEx client = new GitHubClientEx("localhost", 8888, "http");

        String path = SEGMENT_REPOS + "/hubturbo/hubturbo" + SEGMENT_PULLS;
        request.setUri(path);
        request.setResponseContentType(CONTENT_TYPE_JSON);
        request.setParams(params);

        PageHeaderIterator iter = new PageHeaderIterator(request, client, "ETag");
        assertEquals(page1Etag, Utility.stripQuotes(iter.next()));
        assertEquals(page2Etag, Utility.stripQuotes(iter.next()));
        assertEquals(page3Etag, Utility.stripQuotes(iter.next()));
        assertFalse(iter.hasNext());

        mockServer.stop();
    }

    private HttpRequest createMockServerPagedHeaderRequest(int page) {
        return TestUtils.createMockServerRequest("HEAD", page, "hubturbo/hubturbo", "20919534", SEGMENT_PULLS);
    }

    private void setUpHeadRequestOnMockServer(MockServerClient mockServer, HttpRequest request, List<Header> headers) {
        mockServer
                .when(request)
                .respond(response().withHeaders(headers));
    }
}
