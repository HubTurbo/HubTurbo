package ui;

import backend.RepoIO;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.stub.DummySource;
import javafx.application.Application;

import java.util.HashMap;
import java.util.Optional;

/**
 * A collection of methods that deal with the UI class which are mainly used for testing.
 *
 * Test mode should only be run as a test task (Gradle / JUnit), as quit()
 * leaves the JVM alive during test mode (which is cleaned up by Gradle).
 * Manually feeding --test=true into the command line arguments will leave the JVM
 * running after the HT window has been closed, and thus will require the
 * process to be closed manually afterwards (Force Quit / End Process) unless
 * the --closeonquit=true argument is used.
 *
 * Other than the various arguments that can be used, this class also exposes the UI
 * instance which can be called from tests that need to access the UI class directly.
 */
public final class TestController {

    private static UI ui;
    private static HashMap<String, String> commandLineArgs;

    private TestController() {}

    public static void setUI(UI ui, Application.Parameters params) {
        TestController.ui = ui;
        TestController.commandLineArgs = initialiseCommandLineArguments(params);
    }

    private static HashMap<String, String> initialiseCommandLineArguments(Application.Parameters params) {
        return new HashMap<>(params.getNamed());
    }

    public static boolean hasUI() {
        return TestController.ui != null;
    }

    public static UI getUI() {
        return ui;
    }

    public static HashMap<String, String> getCommandLineArgs() {
        return commandLineArgs;
    }

    public static boolean isTestMode() {
        return hasUI() && (
                commandLineArgs.getOrDefault("test", "false").equalsIgnoreCase("true") ||
                        isBypassLogin() ||
                        isTestJSONEnabled() ||
                        isTestChromeDriver() ||
                        isTestGlobalConfig() ||
                        isCloseOnQuit());
    }

    public static boolean isTestGlobalConfig() {
        return hasUI() && commandLineArgs.getOrDefault("testconfig", "false").equalsIgnoreCase("true");
    }

    // When --bypasslogin=true is passed as an argument, the username and password
    // are empty strings.
    public static boolean isBypassLogin() {
        return hasUI() && commandLineArgs.getOrDefault("bypasslogin", "false").equalsIgnoreCase("true");
    }

    public static boolean isTestJSONEnabled() {
        return hasUI() && commandLineArgs.getOrDefault("testjson", "false").equalsIgnoreCase("true");
    }

    public static boolean isTestChromeDriver() {
        return hasUI() && commandLineArgs.getOrDefault("testchromedriver", "false").equalsIgnoreCase("true");
    }

    // Used for test mode to shutdown jvm on quit (not used for ci/tests because that will cause
    // tests to fail).
    public static boolean isCloseOnQuit() {
        return hasUI() && commandLineArgs.getOrDefault("closeonquit", "false").equalsIgnoreCase("true");
    }

    public static Optional<RepoSource> getStubbedRepoSource(boolean forceTestMode) {
        if (forceTestMode || isTestMode()) {
            return Optional.of(new DummySource());
        }

        return Optional.empty();
    }

    public static Optional<JSONStore> getStubbedRepoStore(boolean forceDisableJSONStore) {
        if (forceDisableJSONStore || !isTestJSONEnabled()) {
            return Optional.of(new JSONStoreStub());
        }

        return Optional.empty();
    }

    public static Optional<String> getTestDirectory(boolean forceTestMode) {
        if (forceTestMode || isTestMode()) {
            return Optional.of(RepoStore.TEST_DIRECTORY);
        }

        return Optional.empty();
    }

    public static RepoIO createTestingRepoIO(boolean enableJSONStore) {
        return new RepoIO(TestController.getStubbedRepoSource(true),
                          enableJSONStore ? Optional.empty() : TestController.getStubbedRepoStore(true),
                          TestController.getTestDirectory(true));
    }
}
