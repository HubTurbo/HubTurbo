package tests;

import backend.RepoIO;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.MultiModel;
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
     * Tests that replaceIssueLabelsOnServer returns Optional.empty() if the model for the
     * issue given in the argument can't be found
     */
    @Test
    public void replaceIssueLabels_modelNotFound() {
        MultiModel models = new MultiModel(mock(Preferences.class));
        assertEquals(Optional.empty(), models.replaceIssueLabels("nonexistentrepo", 1, new ArrayList<>()));
    }

    /**
     * Tests that replaceIssueLabelsOnServer called the Model with the same id as the argument
     * repoId and invoke replaceIssueLabelsOnServer on that Model
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
}
