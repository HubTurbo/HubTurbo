package tests;

import backend.interfaces.RepoStore;
import guitests.UITest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ui.UI;
import util.Utility;
import util.events.EventDispatcherStub;
import util.events.ShowErrorDialogEventHandler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ErrorJsonTests {

    private int eventCount = 0;

    @BeforeClass
    public static void setup() {
        UI.events = new EventDispatcherStub();
    }

    @Before
    public void enableTestDirectory() {
        RepoStore.enableTestDirectory();
    }

    @Test
    public void testJsonExplosionDetection() throws ExecutionException, InterruptedException {
        // Expect 0KB file is issueCount is 0. A non-empty file will fail this check.
        UI.events.registerEvent((ShowErrorDialogEventHandler) e -> eventCount++);
        boolean corruptedJson = Utility.writeFile("store/test/dummy1-dummy1.json", "abcde", 0);
        assertEquals(true, corruptedJson);
        assertEquals(1, eventCount);

        // Then, we check that the json-err file exists.
        assertEquals(true, Files.exists(Paths.get("store/test/dummy1-dummy1.json-err")));
    }

    @After
    public void cleanup() {
        UITest.clearTestFolder();
    }

}
