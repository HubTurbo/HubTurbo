package updater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import prefs.SessionConfig;
import prefs.UserConfig;
import ui.TestController;
import util.FileHelper;
import util.Version;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles migration of preferences from different major version.
 */
public class PreferencesMigrator {
    private static final Logger logger = LogManager.getLogger(PreferencesMigrator.class.getName());

    private boolean isUpgrade;

    /**
     *
     * @param isUpgrade true if increase in major version detected, false otherwise indicating downgrade
     */
    public PreferencesMigrator(boolean isUpgrade) {
        this.isUpgrade = isUpgrade;
    }

    public void runMigration() {
        if (isUpgrade) {
            runForwardMigration();
        } else {
            runBackwardMigration();
        }
    }

    private void runForwardMigration() {
        // move settings folder to settings_old
        File oldPrefDir = new File(UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY);

        if (FileHelper.isDirectoryExists(UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY)) {
            try {
                FileHelper.clearDirectory(UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY);
            } catch (IOException e) {
                logger.error("Failed to clear old preferences directory", e);
            }
        } else {
            try {
                FileHelper.createDirectoryIfNonExistent(UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY);
            } catch (IOException e) {
                logger.error("Failed to create old preferences directory", e);
            }
        }

        List<String> filesFailedToBeMoved = FileHelper.moveContentsOfADirectoryToAnother(
                Preferences.DIRECTORY, UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY);

        // TODO alert user which files failed to be moved

        putOldConfigValuesToCurrentPref();
    }

    /**
     * Migrate Preferences if user uses older version of HT than last session.
     *
     * This simply is done by moving contents of settings_old back to settings.
     */
    private void runBackwardMigration() {
        // clear settings folder
        try {
            FileHelper.clearDirectory(Preferences.DIRECTORY);
        } catch (IOException e) {
            logger.error("Failed to clear Preferences of newer version.");
        }

        // move settings_old to settings
        List<String> filesFailedToBeMoved = FileHelper.moveContentsOfADirectoryToAnother(
                UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY, Preferences.DIRECTORY);


        // TODO alert user which files failed to be moved
    }

    /**
     * Puts config values to prefs.
     *
     * This needs to be done manually
     */
    private void putOldConfigValuesToCurrentPref() {
        Preferences pref = TestController.loadApplicationPreferences();

        SessionConfig sessionConfig;
        UserConfig userConfig;

        try {
            sessionConfig = loadOldConfig(pref, UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY,
                    "global.json", SessionConfig.class);
            userConfig = loadOldConfig(pref, UpdateManager.OLD_VERSION_PREFERENCES_DIRECTORY,
                    "user.json", UserConfig.class);
        } catch (Exception e) {
            return;
        }

        pref.setKeyboardShortcuts(sessionConfig.getKeyboardShortcuts());
        pref.setLastLoginCredentials(sessionConfig.getLastLoginUsername(), sessionConfig.getLastLoginPassword());
        pref.setLastOpenBoard(sessionConfig.getLastOpenBoard().get());
        pref.setLastViewedRepository(sessionConfig.getLastViewedRepository());
        pref.setPanelInfo(sessionConfig.getPanelInfo());
    }

    private <T> T loadOldConfig(Preferences pref, String directory, String filename, Class<T> configClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method loadConfigMethod = pref.getClass()
                .getDeclaredMethod("loadConfig", String.class, String.class, Class.class);
        loadConfigMethod.setAccessible(true);

        Object oldConfig = loadConfigMethod.invoke(pref, directory, filename, configClass);
        return (T) oldConfig;
    }
}
