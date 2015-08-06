package tests;

import backend.RepoIO;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.resource.Model;
import guitests.UITest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;
import util.events.testevents.UpdateDummyRepoEvent;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StoreTests {

    /**
     * Needed to avoid NullPointerExceptions
     */
    @BeforeClass
    public static void setup() {
        UI.events = new EventDispatcherStub();
        UI.status = new StatusUIStub();
    }

    @Before
    public void enableTestDirectory() {
        RepoStore.enableTestDirectory();
    }

    @Test
    public void testStoreStub() throws ExecutionException, InterruptedException {
        // DummyRepo constructor gets called, together with the testing handlers to update repo state
        RepoIO testIO = new RepoIO(true, false);

        // Repo not stored, "download" from DummySource
        Model dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(10, dummy1.getIssues().size());

        // Spawn new issue
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy1/dummy1"));

        dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(11, dummy1.getIssues().size());

        // A new file should not have been created as we are using a stub
        if (new File("store/test/dummy1-dummy1.json").isFile()) fail();
    }

    @Test
    public void testStore() throws ExecutionException, InterruptedException {
        // Now we enable JSON store. RepoIO is thus connected with an actual JSONStore object.
        RepoIO testIO = new RepoIO(true, true);

        // Repo currently not stored, "download" from DummySource
        Model dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(10, dummy1.getIssues().size());

        // Spawn new issue (to be stored in JSON)
        UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE, "dummy1/dummy1"));
        // Trigger store
        dummy1 = testIO.updateModel(dummy1).get();
        assertEquals(11, dummy1.getIssues().size());

        TestUtils.delay(2); // Wait 2 seconds for Gson to convert model to JSON and write

        // Now we create a new RepoIO object. If we didn't load from the test JSON file, we would have to
        // re-"download" the whole repository from the DummySource. This means that we would end up with
        // only 10 issues.
        RepoIO alternateIO = new RepoIO(true, true);

        // But since we are indeed loading from the test JSON store, we would end up with 11 issues.
        Model dummy2 = alternateIO.openRepository("dummy1/dummy1").get();
        assertEquals(11, dummy2.getIssues().size());

        // It even works if we enter the repo name in different case
        Model dummy3 = new RepoIO(true, true).openRepository("DUMMY1/DUMMY1").get();
        assertEquals(11, dummy3.getIssues().size());

        UI.status.clear();
    }

    @Test(expected = ExecutionException.class)
    public void testCorruptedJSON() throws InterruptedException, ExecutionException {
        RepoStore.write("testrepo/testrepo", "abcde", 10);

        JSONStore jsonStore = new JSONStore();
        jsonStore.loadRepository("testrepo/testrepo").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNonExistentJSON() throws InterruptedException, ExecutionException {
        JSONStore jsonStore = new JSONStore();
        jsonStore.loadRepository("nonexist/nonexist").get();
    }

    @Test
    public void testLoadCorruptedRepository() throws InterruptedException, ExecutionException {
        RepoStore.write("testrepo/testrepo", "abcde", 10);

        RepoIO repoIO = new RepoIO(true, true);
        Model model = repoIO.openRepository("testrepo/testrepo").get();

        TestUtils.delay(1); // allow for file to be written
        assertEquals(10, model.getIssues().size());
    }

    @Test
    public void testLoadNonExistentRepo() throws InterruptedException, ExecutionException {
        RepoIO repoIO = new RepoIO(true, true);
        Model model = repoIO.openRepository("nonexist/nonexist").get();
        TestUtils.delay(1); // allow for file to be written
        assertEquals(10, model.getIssues().size());
    }

    @After
    public void cleanup() {
        UITest.clearTestFolder();
    }

}
