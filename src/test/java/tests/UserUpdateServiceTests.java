package tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import github.update.UserUpdateService;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class UserUpdateServiceTests {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(8888, this);

    private final MockServerClient mockServer = new MockServerClient("localhost", 8888);
    private static final String usersResourceDir = "tests/UsersSample/";

    private final String hubTurboCollaborators;
    private final String hubTurboCollaboratorsHeader;
    private final String user1;
    private final String user2;
    private final String user3;

    public UserUpdateServiceTests() throws IOException {
        hubTurboCollaborators = TestUtils.readFileFromResource(this, "tests/CollaboratorsSample.json");
        hubTurboCollaboratorsHeader = TestUtils.readFileFromResource(this, "tests/collaborators-header.txt");
        user1 = TestUtils.readFileFromResource(this, usersResourceDir + "acruis.json");
        user2 = TestUtils.readFileFromResource(this, usersResourceDir + "dariusf.json");
        user3 = TestUtils.readFileFromResource(this, usersResourceDir + "ndt93.json");
    }

    @Before
    public void setupMockServer() {
        List<Header> collaboratorsHeaders = TestUtils.parseHeaderRecord(hubTurboCollaboratorsHeader);

        mockServer
                .when(request().withPath(TestUtils.API_PREFIX + "/repos/HubTurbo/tests/collaborators"))
                .respond(response().withHeaders(collaboratorsHeaders).withBody(hubTurboCollaborators));

        mockServer
                .when(request().withPath(TestUtils.API_PREFIX + "/users/acruis"))
                .respond(response().withBody(user1));

        mockServer
                .when(request().withPath(TestUtils.API_PREFIX + "/users/dariusf"))
                .respond(response().withBody(user2));

        mockServer
                .when(request().withPath(TestUtils.API_PREFIX + "/users/ndt93"))
                .respond(response().withBody(user3));
    }

    @Test
    public void updateCollaborators_outdatedETag_collaboratorsCompleteDataRetrieved() {
        GitHubClientEx gitHubClient = new GitHubClientEx("localhost", 8888, "http");
        UserUpdateService service = new UserUpdateService(gitHubClient, "9332ee96a4e41dfeebfd36845e861096");

        List<User> expected = new ArrayList<>();
        Type userType = new TypeToken<User>() {}.getType();
        expected.addAll(Arrays.asList(new Gson().fromJson(user1, userType),
                                      new Gson().fromJson(user2, userType),
                                      new Gson().fromJson(user3, userType)));
        List<User> actual = service.getUpdatedItems(RepositoryId.createFromId("HubTurbo/tests"));

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getLogin(), actual.get(i).getLogin());
            assertEquals(expected.get(i).getName(), actual.get(i).getName());
        }
    }

    @Test
    public void updateCollaborators_outdatedETag_eTagUpdated() {
        GitHubClientEx gitHubClient = new GitHubClientEx("localhost", 8888, "http");
        UserUpdateService service = new UserUpdateService(gitHubClient, "9332ee96a4e41dfeebfd36845e861096");

        List<User> collaborators = service.getUpdatedItems(RepositoryId.createFromId("HubTurbo/tests"));

        assertEquals(3, collaborators.size());
        assertEquals("9332ee96a4e41dfeebfd36845e861095", service.getUpdatedETags());
    }

    @Test
    public void updateCollaborators_hasLatestETag_noUpdate() {
        GitHubClientEx gitHubClient = new GitHubClientEx("localhost", 8888, "http");
        UserUpdateService service = new UserUpdateService(gitHubClient, "9332ee96a4e41dfeebfd36845e861095");

        List<User> collaborators = service.getUpdatedItems(RepositoryId.createFromId("HubTurbo/tests"));

        assertEquals(0, collaborators.size());
    }
}
