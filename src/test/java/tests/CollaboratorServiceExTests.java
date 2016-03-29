package tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import github.CollaboratorServiceEx;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class CollaboratorServiceExTests {

    private static GitHubClient client;
    private static CollaboratorServiceEx service;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initialize() {
        client = new GitHubClient("localhost", 8888, "http");
        service = new CollaboratorServiceEx(client);
    }

    @Test
    public void getCollaborators_nullRepoId_exceptionThrown() throws IOException {
        thrown.expect(IllegalArgumentException.class);
        service.getCollaborators(null);
    }

    @Test
    public void getCollaborators_emptyRepoId_exceptionThrown() throws IOException {
        thrown.expect(IllegalArgumentException.class);
        service.getCollaborators(RepositoryId.createFromId(""));
    }

    @Test
    public void getCollaborators_nonExistentRepoId_exceptionThrown() throws IOException {
        thrown.expect(IOException.class);
        service.getCollaborators(RepositoryId.createFromId("fakeowner/bogusrepo"));
    }
    
    @Test
    public void getCollaborators_validRepoId_successful() throws IOException {
        MockServerClient mockServer = ClientAndServer.startClientAndServer(8888);
        String sampleCollaborators = TestUtils.readFileFromResource(this, "tests/CollaboratorsSample.json");

        mockServer.when(
                request()
                    .withPath(TestUtils.API_PREFIX + "/repos/hubturbo/tests/collaborators")
        ).respond(response().withBody(sampleCollaborators));

        Type listOfUsers = new TypeToken<List<User>>() {}.getType();
        List<User> expectedCollaborators = new Gson().fromJson(sampleCollaborators, listOfUsers);
        List<User> actualCollaborators = service.getCollaborators(RepositoryId.createFromId("hubturbo/tests"));

        assertEquals(expectedCollaborators.size(), actualCollaborators.size());

        for (int i = 0; i < expectedCollaborators.size(); i++) {
            assertEquals(expectedCollaborators.get(i).getLogin(), actualCollaborators.get(i).getLogin());
            assertEquals(expectedCollaborators.get(i).getName(), actualCollaborators.get(i).getName());
            assertEquals(true, actualCollaborators.get(i).getName() != null);
        }

        mockServer.stop();
    }
}
