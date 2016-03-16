package ui;

import backend.RepoIO;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.stub.DummySource;
import javafx.application.Application;
import prefs.Preferences;
import updater.UpdateManager;
import updater.UpdateManagerStub;

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
                        shouldTestStartupBoard() ||
                        isCloseOnQuit());
    }

    public static boolean isTestGlobalConfig() {
        return hasUI() && commandLineArgs.getOrDefault("testconfig", "false").equalsIgnoreCase("true");
    }

    /**
     * Determines whether the startup board should be tested using command line arguments.
     */
    public static boolean shouldTestStartupBoard() {
        return hasUI() && commandLineArgs.getOrDefault("startupboard", "false").equalsIgnoreCase("true");
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

    /**
     * Returns true if HubTurbo is not being run on Test mode or if startup board creation is being tested.
     * As for the other tests, they start on a clean state with dummy repos established
     * so don't need boards created on startup.
     */
    public static boolean shouldOpenSampleBoard() {
        return !isTestMode() || shouldTestStartupBoard();
    }

    /**
     * Creates a Preferences instance that stores data in test config file if run in
     * test mode, or in a default config file specified in the Preferences class
     * @return
     */
    public static Preferences loadApplicationPreferences() {
        if (isTestMode()) {
            return loadTestPreferences();
        }

        return Preferences.load(Preferences.GLOBAL_CONFIG_FILE);
    }

    /**
     * Creates a Preferences instance that stores data in a config file for testing, loading
     * from it if it already exists.
     * @return
     */
    public static Preferences loadTestPreferences() {
        return Preferences.load(Preferences.TEST_CONFIG_FILE);
    }

    /**
     * Creates a Preferences instance that stores data in a config file for testing, unconditionally
     * initialising it beforehand.
     * @return
     */
    public static Preferences createTestPreferences() {
        return Preferences.create(Preferences.TEST_CONFIG_FILE);
    }

    /**
     * Creates a RepoIO for the application that uses different components
     * depending on various test options: --test, --testjson etc.
     * @return
     */
    public static RepoIO createApplicationRepoIO() {
        if (isTestMode()) {
            return createTestingRepoIO(isTestJSONEnabled() ? Optional.of(new JSONStoreStub()) : Optional.empty());
        } else {
            return new RepoIO(Optional.empty(), Optional.empty(), Optional.empty());
        }
    }

    /**
     * Creates a partially stubbed RepoIO used for testing.
     * @param jsonStoreToBeUsed store to be used with RepoIO,
     *                          defaults to a new instance of JSONStore if this value is empty
     * @return
     */
    public static RepoIO createTestingRepoIO(Optional<JSONStore> jsonStoreToBeUsed) {
        return new RepoIO(Optional.of(new DummySource()), jsonStoreToBeUsed,
                          Optional.of(RepoStore.TEST_DIRECTORY));
    }

    /**
     * Creates update manager if not in test mode, its stub otherwise.
     * @return update manager to be used
     */
    public static UpdateManager createUpdateManager() {
        if (isTestMode()) {
            return new UpdateManagerStub();
        } else {
            UpdateProgressWindow updateProgressWindow = new UpdateProgressWindow();
            return new UpdateManager(ui, updateProgressWindow);
        }
    }
}
