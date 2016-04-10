package tests;

import org.junit.Test;
import prefs.UpdateConfig;
import util.Version;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class UpdateConfigTest {
    @Test
    public void updateConfigSetLastUpdateDownloadStatus_getValueByReflection_correctValue()
            throws NoSuchFieldException, IllegalAccessException {
        UpdateConfig updateConfig = new UpdateConfig();

        Class<?> updateConfigClass = updateConfig.getClass();

        Field isLastAppUpdateDownloadSuccessfulField =
                updateConfigClass.getDeclaredField("isLastAppUpdateDownloadSuccessful");
        isLastAppUpdateDownloadSuccessfulField.setAccessible(true);

        updateConfig.setLastAppUpdateDownloadSuccessful(true);
        assertTrue((boolean) isLastAppUpdateDownloadSuccessfulField.get(updateConfig));

        updateConfig.setLastAppUpdateDownloadSuccessful(false);
        assertFalse((boolean) isLastAppUpdateDownloadSuccessfulField.get(updateConfig));
    }

    @Test
    public void updateConfigGetLastUpdateDownloadStatus_setValueByReflection_correctValue()
            throws NoSuchFieldException, IllegalAccessException {
        UpdateConfig updateConfig = new UpdateConfig();

        Class<?> updateConfigClass = updateConfig.getClass();

        Field isLastAppUpdateDownloadSuccessfulField =
                updateConfigClass.getDeclaredField("isLastAppUpdateDownloadSuccessful");
        isLastAppUpdateDownloadSuccessfulField.setAccessible(true);

        isLastAppUpdateDownloadSuccessfulField.set(updateConfig, true);
        assertTrue(updateConfig.isLastAppUpdateDownloadSuccessful());

        isLastAppUpdateDownloadSuccessfulField.set(updateConfig, false);
        assertFalse(updateConfig.isLastAppUpdateDownloadSuccessful());
    }

    @Test
    public void updateConfigAddToVersionPreviouslyDownloaded_getListByReflection_correctList()
            throws NoSuchFieldException, IllegalAccessException {
        UpdateConfig updateConfig = new UpdateConfig();

        Version insideOfList = new Version(1, 0, 0);
        Version outsideOfList = new Version(0, 0, 0);

        updateConfig.addToVersionPreviouslyDownloaded(insideOfList);

        Class<?> updateConfigClass = updateConfig.getClass();

        Field versionsPreviouslyDownloadedField =
                updateConfigClass.getDeclaredField("versionsPreviouslyDownloaded");
        versionsPreviouslyDownloadedField.setAccessible(true);

        List<Version> reflectedList = (List) versionsPreviouslyDownloadedField.get(updateConfig);

        assertTrue(reflectedList.contains(insideOfList));
        assertFalse(reflectedList.contains(outsideOfList));
    }

    @Test
    public void updateConfigCheckIfVersionWasPreviouslyDownloaded_setListByReflection_correctResult()
            throws NoSuchFieldException, IllegalAccessException {
        UpdateConfig updateConfig = new UpdateConfig();

        Version versionHaveBeenDownloaded = new Version(1, 0, 0);
        Version versionNeverDownloaded = new Version(0, 0, 0);

        List<Version> manualListOfVersions = Arrays.asList(versionHaveBeenDownloaded);

        updateConfig.addToVersionPreviouslyDownloaded(versionHaveBeenDownloaded);

        Class<?> updateConfigClass = updateConfig.getClass();

        Field versionsPreviouslyDownloadedField =
                updateConfigClass.getDeclaredField("versionsPreviouslyDownloaded");
        versionsPreviouslyDownloadedField.setAccessible(true);

        versionsPreviouslyDownloadedField.set(updateConfig, manualListOfVersions);

        assertTrue(updateConfig.wasPreviouslyDownloaded(versionHaveBeenDownloaded));
        assertFalse(updateConfig.wasPreviouslyDownloaded(versionNeverDownloaded));

    }
}
