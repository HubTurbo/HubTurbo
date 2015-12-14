package tests;

import backend.RepoIO;
import backend.resource.MultiModel;
import org.junit.BeforeClass;
import org.junit.Test;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MultiModelTest {

    @BeforeClass
    public static void setup() {
        UI.events = new EventDispatcherStub();
        UI.status = new StatusUIStub();
    }

    MultiModel multiModel = new MultiModel(mock(Preferences.class));

    @Test
    public void equality() {
        assertTrue(multiModel.equals(multiModel));
        assertFalse(multiModel.equals(null));
        assertFalse(multiModel.equals(""));
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
        RepoIO testIO = TestController.createTestingRepoIO(false);
        testIO.openRepository(repoId1).thenApply(models::addPending).get();
        testIO.openRepository(repoId2).thenApply(models::addPending).get();

        assertEquals(true, models.getModelById(repoId1).isPresent());
        assertEquals(true, models.getModelById(repoId2).isPresent());

        models.removeRepoModelById(repoId1);
        assertEquals(false, models.getModelById(repoId1).isPresent());

        models.removeRepoModelById(repoId2.toUpperCase()); // removal in different case should work
        assertEquals(false, models.getModelById(repoId2).isPresent());
    }

}
