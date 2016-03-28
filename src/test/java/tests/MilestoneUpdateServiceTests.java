package tests;

import github.GitHubClientEx;
import github.update.MilestoneUpdateService;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import util.Utility;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpResponse.response;

public class MilestoneUpdateServiceTests {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(8888, this);

    private final MockServerClient mockServer = new MockServerClient("localhost", 8888);
    private static final String resourceDir = "tests/PagedMilestonesSample/";

    private final String page1Header;
    private final String page1;
    private final String page2Header;
    private final String page2;

    public MilestoneUpdateServiceTests() throws IOException {
        page1Header = TestUtils.readFileFromResource(this, resourceDir + "page1-header.txt");
        page1 = TestUtils.readFileFromResource(this, resourceDir + "page1.json");
        page2Header = TestUtils.readFileFromResource(this, resourceDir + "page2-header.txt");
        page2 = TestUtils.readFileFromResource(this, resourceDir + "page2.json");
    }

    @Before
    public void setUpMockServer() {
        List<Header> page1Headers = TestUtils.parseHeaderRecord(page1Header);
        List<Header> page2Headers = TestUtils.parseHeaderRecord(page2Header);

        mockServer
                .when(createMockServerRequest("HEAD", 1))
                .respond(response().withHeaders(page1Headers));

        mockServer
                .when(createMockServerRequest("GET", 1))
                .respond(response().withHeaders(page1Headers).withBody(page1));

        mockServer
                .when(createMockServerRequest("HEAD", 2))
                .respond(response().withHeaders(page2Headers));

        mockServer
                .when(createMockServerRequest("GET", 2))
                .respond(response().withHeaders(page2Headers).withBody(page2));
    }

    /**
     * Tests that getUpdatedItems returns empty result if the combination of ETags
     * from all pages returned by a MockServer is equal to the ETags passed into the constructor. The updated
     * ETags should then remain the same and the update check time should reflect first page' Date header
     */
    @Test
    public void testGetUpdatedMilestonesNoChanges() {
        GitHubClientEx client = new GitHubClientEx("localhost", 8888, "http");
        String previousETags = "4c0ad3c08dc706b76d8277a88a4c037e#4b56f029e953e9983344b9e0b60d9a71";
        MilestoneUpdateService service = new MilestoneUpdateService(client, previousETags);

        List<Milestone> milestones = service.getUpdatedItems(RepositoryId.createFromId("teammates/repo"));

        assertTrue(milestones.isEmpty());
        assertEquals(previousETags, service.getUpdatedETags());
        assertEquals(Utility.parseHTTPLastModifiedDate("Sun, 27 Dec 2015 15:28:46 GMT"),
                     service.getUpdatedCheckTime());
    }

    /**
     * Tests that getUpdatedItems returns all milestones recorded in resources/tests/PagedMilestonesSample
     * if last ETag of 2nd page is different from 2nd page' ETag in the responses. The updated ETags should
     * then be modified accordingly and the update check time should reflect the first page Date header
     */
    @Test
    public void testGetUpdatedMilestonesSample() {
        GitHubClientEx client = new GitHubClientEx("localhost", 8888, "http");
        String previousETags = "4c0ad3c08dc706b76d8277a88a4c037e#ffffff";
        String expectedETags = "4c0ad3c08dc706b76d8277a88a4c037e#4b56f029e953e9983344b9e0b60d9a71";
        MilestoneUpdateService service = new MilestoneUpdateService(client, previousETags);

        List<Milestone> milestones = service.getUpdatedItems(RepositoryId.createFromId("teammates/repo"));

        assertEquals(188, milestones.size());
        assertEquals(expectedETags, service.getUpdatedETags());
        assertEquals(Utility.parseHTTPLastModifiedDate("Sun, 27 Dec 2015 15:28:46 GMT"),
                     service.getUpdatedCheckTime());
    }

    private static HttpRequest createMockServerRequest(String method, int page) {
        return TestUtils.createMockServerRequest(method, page, "teammates/repo", "19369035", "/milestones");
    }
}
