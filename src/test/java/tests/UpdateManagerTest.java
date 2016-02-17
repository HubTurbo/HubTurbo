package tests;

import org.junit.Test;
import ui.TestController;
import updater.UpdateManager;
import util.Version;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UpdateManagerTest {
    @Test
    public void updateManagerHtBackupFileRegex_validNames_parsedCorrectly()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        UpdateManager updateManager = new UpdateManager(TestController.getUI(), null);

        Method getVersionOfHtBackupFileFromFilenameMethod = updateManager.getClass()
                .getDeclaredMethod("getVersionOfHtBackupFileFromFilename", String.class);
        getVersionOfHtBackupFileFromFilenameMethod.setAccessible(true);

        Version version = new Version(0, 0, 0);
        Object versionObj = getVersionOfHtBackupFileFromFilenameMethod
                .invoke(updateManager, String.format("HubTurbo_%s.jar", version.toString()));
        assertEquals(version, versionObj);

        version = new Version(10, 10, 10);
        versionObj = getVersionOfHtBackupFileFromFilenameMethod
                .invoke(updateManager, String.format("HubTurbo_%s.jar", version.toString()));
        assertEquals(version, versionObj);
    }

    /**
     * Expecting an AssertionError here from `assert false`, but due to reflection,
     * will cause InvocationTargetException instead.
     */
    @Test
    public void updateManagerHtBackupFileRegex_invalidNames_expectAssertionError()
            throws NoSuchMethodException, IllegalAccessException {
        UpdateManager updateManager = new UpdateManager(TestController.getUI(), null);

        Method getVersionOfHtBackupFileFromFilenameMethod = updateManager.getClass()
                .getDeclaredMethod("getVersionOfHtBackupFileFromFilename", String.class);
        getVersionOfHtBackupFileFromFilenameMethod.setAccessible(true);

        try {
            getVersionOfHtBackupFileFromFilenameMethod.invoke(updateManager, "Non-valid name.jar");
            fail("should have thrown InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertEquals(AssertionError.class, e.getCause().getClass());
        }
    }
}
