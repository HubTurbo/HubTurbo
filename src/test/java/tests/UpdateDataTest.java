package tests;

import org.junit.Test;
import updater.HtDownloadLink;
import updater.UpdateData;
import util.JsonFileConverter;
import util.Version;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class UpdateDataTest {
    @Test
    public void updateDataConstructor_noArgsConstructor_expectNoNullPointerExceptionOnMethodCall() {
        UpdateData updateData = new UpdateData();
        updateData.getLatestUpdateDownloadLinkForCurrentVersion();
    }

    @Test
    public void updateDataServerFile_readFromServerFile_ensureAllDataCanBeFound() {
        File serverUpdataDataJsonFile = new File("HubTurboUpdate.json");
        JsonFileConverter serverUpdataDataJsonConverter = new JsonFileConverter(serverUpdataDataJsonFile);

        Optional<UpdateData> serverUpdateData = serverUpdataDataJsonConverter.loadFromFile(UpdateData.class);
        assertTrue(serverUpdateData.isPresent());

        Optional<HtDownloadLink> downloadLink = serverUpdateData.get().getLatestUpdateDownloadLinkForCurrentVersion();
        assertTrue(downloadLink.isPresent());
        assertTrue(downloadLink.get().getDownloadLinkUrl() != null);
        assertTrue(downloadLink.get().getVersion() != null);

    }

    /**
     * UpdateData will return download link of versions which major is greater by 1 or equal to current version,
     * regardless of minor and patch version. This is to allow migration by 1 major version difference
     */
    @Test
    public void updateDataGetDownloadLink_manuallySetListOfDownloadLink_getCorrectLink()
            throws NoSuchFieldException, IllegalAccessException {
        UpdateData updateData = new UpdateData();

        int currentVersionMajor = Version.getCurrentVersion().getMajor();

        // only 1 link lesser by major, will not get download link
        Version version = new Version(currentVersionMajor - 1, 10, 10);
        HtDownloadLink htDownloadLink = new HtDownloadLink();
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertFalse(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());


        // only 1 link same major lesser by minor, will get download link
        version = new Version(currentVersionMajor, Version.getCurrentVersion().getMinor() - 2, 10);
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertTrue(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());

        // only 1 link same major lesser by patch, will get download link
        version = new Version(currentVersionMajor, Version.getCurrentVersion().getMinor(),
                Version.getCurrentVersion().getPatch() - 5);
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertTrue(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());

        // only 1 link same major greater by minor, will get download link
        version = new Version(currentVersionMajor, Version.getCurrentVersion().getMinor() + 2, 0);
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertTrue(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());

        // only 1 link greater by major, major differ by 1, will get download link
        version = new Version(currentVersionMajor + 1, 10, 10);
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertTrue(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());

        // only 1 link greater by major, major differ by 2, will not get download link
        version = new Version(currentVersionMajor + 2, 10, 10);
        htDownloadLink.setVersion(version);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink));
        assertFalse(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());

        // 2 links, 1 same major 1 greater major differ by 2
        version = new Version(currentVersionMajor, Version.getCurrentVersion().getMinor() - 2, 10);
        htDownloadLink.setVersion(version);
        Version anotherVersion = new Version(currentVersionMajor + 2, 10, 10);
        HtDownloadLink anotherHtDownloadLink = new HtDownloadLink();
        anotherHtDownloadLink.setVersion(anotherVersion);
        setlistOfHTVersionsDownloadLink(updateData, Arrays.asList(htDownloadLink, anotherHtDownloadLink));
        assertTrue(updateData.getLatestUpdateDownloadLinkForCurrentVersion().isPresent());
        assertEquals(htDownloadLink, updateData.getLatestUpdateDownloadLinkForCurrentVersion().get());
    }

    private void setlistOfHTVersionsDownloadLink(UpdateData updateData, List<HtDownloadLink> givenList)
            throws NoSuchFieldException, IllegalAccessException {
        Class<?> updateDataClass = updateData.getClass();

        Field downloadLinksField = updateDataClass.getDeclaredField("downloadLinks");
        downloadLinksField.setAccessible(true);

        downloadLinksField.set(updateData, givenList);
    }
}
