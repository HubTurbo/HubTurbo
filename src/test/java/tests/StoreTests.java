package tests;

import backend.RepoIO;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.stub.DummyRepoState;
import guitests.UITest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ui.TestController;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;
import util.events.testevents.UpdateDummyRepoEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
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
        RepoStore.changeDirectory(RepoStore.TEST_DIRECTORY);
    }

    @Test
    public void testStoreStub() throws ExecutionException, InterruptedException {
        // DummyRepo constructor gets called, together with the testing handlers to update repo state
        RepoIO testIO = TestController.createTestingRepoIO(Optional.of(new JSONStoreStub()));
        testIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(testIO));

        // Repo not stored, "download" from DummySource
        Model dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, dummy1.getIssues().size());

        // Spawn new issue
        UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy1/dummy1"));

        dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, dummy1.getIssues().size());

        // A new file should not have been created as we are using a stub
        if (new File("store/test/dummy1-dummy1.json").isFile()) {
            fail();
        }
    }

    @Test
    public void testStore() throws ExecutionException, InterruptedException {
        // Now we enable JSON store. RepoIO is thus connected with an actual JSONStore object.
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        testIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(testIO));

        // Repo currently not stored, "download" from DummySource
        Model dummy1 = testIO.openRepository("dummy1/dummy1").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, dummy1.getIssues().size());

        // Spawn new issue (to be stored in JSON)
        UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy1/dummy1"));
        // Trigger store
        dummy1 = testIO.updateModel(dummy1, false).get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, dummy1.getIssues().size());

        TestUtils.delay(2); // Wait 2 seconds for Gson to convert model to JSON and write

        // Now we create a new RepoIO object. If we didn't load from the test JSON file, we would have to
        // re-"download" the whole repository from the DummySource. This means that we would end up with
        // only 10 issues.
        RepoIO alternateIO = TestController.createTestingRepoIO(Optional.empty());
        alternateIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(alternateIO));

        // But since we are indeed loading from the test JSON store, we would end up with 11 issues.
        Model dummy2 = alternateIO.openRepository("dummy1/dummy1").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, dummy2.getIssues().size());

        // It even works if we enter the repo name in different case
        RepoIO repoIO = TestController.createTestingRepoIO(Optional.empty());
        repoIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(repoIO));
        Model dummy3 = repoIO.openRepository("DUMMY1/DUMMY1").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES + 1, dummy3.getIssues().size());

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

        RepoIO repoIO = TestController.createTestingRepoIO(Optional.empty());
        repoIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(repoIO));
        Model model = repoIO.openRepository("testrepo/testrepo").get();

        TestUtils.delay(1); // allow for file to be written
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, model.getIssues().size());
    }

    @Test
    public void testLoadNonExistentRepo() throws InterruptedException, ExecutionException {
        RepoIO repoIO = TestController.createTestingRepoIO(Optional.empty());
        repoIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(repoIO));
        Model model = repoIO.openRepository("nonexistent/nonexistent").get();
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES, model.getIssues().size());
    }

    @Test
    public void testRemoveRepo() throws InterruptedException, ExecutionException {
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        testIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(testIO));

        Model dummy1 = testIO.openRepository("dummy1/dummy1").get();
        UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy1/dummy1"));
        testIO.updateModel(dummy1, false).get();

        assertEquals(true, Files.exists(Paths.get("store/test/dummy1-dummy1.json")));

        testIO.removeRepository("dummy1/dummy1").get();

        assertEquals(false, Files.exists(Paths.get("store/test/dummy1-dummy1.json")));
    }

    @After
    public void cleanup() {
        UITest.clearTestFolder();
    }

}
