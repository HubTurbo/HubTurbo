package tests;

import backend.RepoIO;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboUser;
import org.junit.BeforeClass;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultiModelTest {

    MultiModel multiModel = new MultiModel(mock(Preferences.class));

    @BeforeClass
    public static void setup() {
        UI.events = new EventDispatcherStub();
        UI.status = new StatusUIStub();
    }

    @Test
    public void equality() {
        assertTrue(multiModel.equals(multiModel));
        assertFalse(multiModel.equals(null));
        assertFalse(multiModel.equals("")); // NOPMD
    }

    @Test
    public void multiModelTest() {
        assertEquals(new MultiModel(mock(Preferences.class)), multiModel);
        assertEquals(new MultiModel(mock(Preferences.class)).hashCode(), multiModel.hashCode());
    }

    @Test
    public void testRemoveModel() throws ExecutionException, InterruptedException {
        final String repoId1 = "dummy1/dummy1";
        final String repoId2 = "dummy2/dummy2";
        MultiModel models = new MultiModel(mock(Preferences.class));
        models.queuePendingRepository(repoId1);
        models.queuePendingRepository(repoId2);
        RepoIO testIO = TestController.createTestingRepoIO(Optional.of(new JSONStoreStub()));
        testIO.openRepository(repoId1).thenApply(models::addPending).get();
        testIO.openRepository(repoId2).thenApply(models::addPending).get();

        assertEquals(true, models.getModelById(repoId1).isPresent());
        assertEquals(true, models.getModelById(repoId2).isPresent());

        models.removeRepoModelById(repoId1);
        assertEquals(false, models.getModelById(repoId1).isPresent());

        models.removeRepoModelById(repoId2.toUpperCase()); // removal in different case should work
        assertEquals(false, models.getModelById(repoId2).isPresent());
    }

    /**
     * Tests that replaceIssueLabels returns Optional.empty() if the model for the
     * issue given in the argument can't be found
     */
    @Test
    public void replaceIssueLabels_modelNotFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));
        assertEquals(Optional.empty(), models.replaceIssueLabels("nonexistentrepo", 1, new ArrayList<>()));
    }

    /**
     * Tests that {@code editIssueState} returns Optional.empty() if the model for the
     * issue given in the argument can't be found
     */
    @Test
    public void editIssueState_modelNotFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));
        assertEquals(Optional.empty(), models.editIssueState("nonexistentrepo", 1, true));
        assertEquals(Optional.empty(), models.editIssueState("nonexistentrepo", 1, false));
    }

    @Test
    public void isUserInRepo_queryExistingUser_userFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));

        final String REPO = "dummy/dummy";

        TurboUser user1 = new TurboUser(REPO, "alice", "Alice");
        TurboUser user2 = new TurboUser(REPO, "bob", "Fox");
        List<TurboUser> users = Arrays.asList(user1, user2);

        Model mockedModel = mock(Model.class);
        when(mockedModel.getRepoId()).thenReturn(REPO);
        when(mockedModel.getUsers()).thenReturn(users);

        models.queuePendingRepository(REPO);
        models.addPending(mockedModel);

        assertTrue(models.isUserInRepo(REPO, "aLICE"));
        assertTrue(models.isUserInRepo(REPO, "bob"));
        assertTrue(models.isUserInRepo(REPO, "fO"));
        assertTrue(models.isUserInRepo(REPO, ""));
    }

    @Test
    public void isUserInRepo_queryNonExistingUser_userNotFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));

        final String REPO = "dummy/dummy";

        TurboUser user1 = new TurboUser(REPO, "alice", "Alice");
        TurboUser user2 = new TurboUser(REPO, "bob", "Fox");
        List<TurboUser> users = Arrays.asList(user1, user2);

        Model mockedModel = mock(Model.class);
        when(mockedModel.getRepoId()).thenReturn(REPO);
        when(mockedModel.getUsers()).thenReturn(users);

        models.queuePendingRepository(REPO);
        models.addPending(mockedModel);

        assertFalse(models.isUserInRepo(REPO, "bot"));
        assertFalse(models.isUserInRepo(REPO, "alices"));
    }

    /**
     * Tests that replaceIssueMilestone returns Optional.empty() if the model for the
     * issue given in the argument can't be found
     */
    @Test
    public void replaceIssueMilestone_modelNotFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));
        assertEquals(Optional.empty(), models.replaceIssueMilestone("nonexistentrepo", 1, Optional.of(1)));
    }

    /**
     * Tests that replaceIssueLabels called the Model with the same id as the argument
     * repoId and invoke replaceIssueLabels on that Model
     */
    @Test
    public void replaceIssueLabels_successful() {
        String repoId = "testowner/testrepo";
        int issueId = 1;
        List<String> labels = Arrays.asList("label1", "label2");

        Model mockedModel = mock(Model.class);
        when(mockedModel.getRepoId()).thenReturn(repoId);
        when(mockedModel.getIssues()).thenReturn(new ArrayList<>());

        MultiModel models = new MultiModel(mock(Preferences.class));
        models.queuePendingRepository(repoId);
        models.addPending(mockedModel);

        models.replaceIssueLabels(repoId, issueId, labels);
        verify(mockedModel).replaceIssueLabels(issueId, labels);
    }

    /**
     * Tests that replaceIssueMilestone called the Model with the same id as the argument
     * repoId and invoke replaceIssueMilestone on that Model
     */
    @Test
    public void replaceIssueMilestone_successful() {
        String repoId = "testowner/testrepo";
        int issueId = 1;
        Optional<Integer> milestoneId = Optional.of(1);

        Model mockedModel = mock(Model.class);
        when(mockedModel.getRepoId()).thenReturn(repoId);
        when(mockedModel.getIssues()).thenReturn(new ArrayList<>());

        MultiModel models = new MultiModel(mock(Preferences.class));
        models.queuePendingRepository(repoId);
        models.addPending(mockedModel);

        models.replaceIssueMilestone(repoId, issueId, milestoneId);
        verify(mockedModel).replaceIssueMilestone(issueId, milestoneId);
    }

    /**
     * Tests that {@code editIssueState} called the Model with the same id as the argument
     * repoId and invoke editIssueState on that Model
     */
    @Test
    public void editIssueState_successful() {
        String repoId = "testowner/testrepo";
        int issueId = 1;

        Model mockedModel = mock(Model.class);
        when(mockedModel.getRepoId()).thenReturn(repoId);
        when(mockedModel.getIssues()).thenReturn(new ArrayList<>());

        MultiModel models = new MultiModel(mock(Preferences.class));
        models.queuePendingRepository(repoId);
        models.addPending(mockedModel);

        models.editIssueState(repoId, issueId, true);
        verify(mockedModel).editIssueState(issueId, true);

        models.editIssueState(repoId, issueId, false);
        verify(mockedModel).editIssueState(issueId, false);
    }
}
