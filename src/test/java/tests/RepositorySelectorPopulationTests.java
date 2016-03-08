package tests;

import backend.RepoIO;
import backend.interfaces.RepoStore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ui.TestController;
import ui.UI;
import ui.components.StatusUIStub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.io.Files.getFileExtension;
import static org.junit.Assert.assertEquals;

public class RepositorySelectorPopulationTests {

    @BeforeClass
    public static void setup() {
        UI.status = new StatusUIStub();
    }

    @Before
    public void clearTestDirectory() {
        try {
            Files.walk(Paths.get(RepoStore.TEST_DIRECTORY), 1)
                    .filter(Files::isRegularFile)
                    .filter(p -> getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json"))
                    .forEach(p -> new File(p.toAbsolutePath().toString()).delete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noJsonFiles() {
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        assertEquals(0, testIO.getStoredRepos().size());
    }

    @Test
    public void oneInvalidJsonFile() throws IOException {
        File invalidJson = new File("store/test/dummy-dummy.json");
        assert invalidJson.createNewFile();
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        assertEquals(0, testIO.getStoredRepos().size());
    }

    @Test
    public void oneValidJsonFile() throws ExecutionException, InterruptedException {
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        testIO.openRepository("dummy/dummy").get();
        TestUtils.delay(2); // Wait 2 seconds for Gson to convert model to JSON and write

        RepoIO alternateIO = TestController.createTestingRepoIO(Optional.empty());
        TestUtils.createTestRepoOpControl(alternateIO);
        assertEquals(1, alternateIO.getStoredRepos().size());
    }

    @Test
    public void mixedJsonFiles() throws IOException, ExecutionException, InterruptedException {
        File invalidJson1 = new File("store/test/dummy1-dummy1.json");
        assert invalidJson1.createNewFile();
        File invalidJson2 = new File("store/test/dummy2-dummy2.json");
        assert invalidJson2.createNewFile();
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        TestUtils.createTestRepoOpControl(testIO);
        testIO.openRepository("dummy3/dummy3").get();
        testIO.openRepository("dummy4/dummy4").get();
        TestUtils.delay(2); // Wait 2 seconds for Gson to convert model to JSON and write

        RepoIO alternateIO = TestController.createTestingRepoIO(Optional.empty());
        TestUtils.createTestRepoOpControl(alternateIO);
        assertEquals(2, alternateIO.getStoredRepos().size());
    }

}
