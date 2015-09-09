package tests;

import backend.RepoIO;
import backend.resource.MultiModel;
import org.junit.BeforeClass;
import org.junit.Test;
import prefs.Preferences;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiModelTest {

    @BeforeClass
    public static void setup() {
        UI.events = new EventDispatcherStub();
        UI.status = new StatusUIStub();
    }

    MultiModel multiModel = new MultiModel(new Preferences(true));

    @Test
    public void equality() {
        assertTrue(multiModel.equals(multiModel));
        assertFalse(multiModel.equals(null));
        assertFalse(multiModel.equals(""));
    }

    @Test
    public void multiModelTest() {
        assertEquals(new MultiModel(new Preferences(true)), multiModel);
        assertEquals(new MultiModel(new Preferences(true)).hashCode(), multiModel.hashCode());
    }

    @Test
    public void testRemoveModel() throws ExecutionException, InterruptedException {
        final String repoId = "dummy1/dummy1";
        MultiModel models = new MultiModel(new Preferences(true));
        models.queuePendingRepository(repoId);
        RepoIO testIO = new RepoIO(true, false);
        testIO.openRepository(repoId).thenApply(models::addPending).get();

        assertEquals(true, models.getModelById(repoId).isPresent());

        models.removeRepoModelById(repoId);

        assertEquals(false, models.getModelById(repoId).isPresent());
    }

}
