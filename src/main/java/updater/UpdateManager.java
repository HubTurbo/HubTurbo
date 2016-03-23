package updater;

import javafx.application.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UI;
import ui.UpdateProgressWindow;
import util.DialogMessage;
import prefs.Preferences;
import prefs.UpdateConfig;
import util.JsonFileConverter;
import util.Version;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The class that will handle updating of HubTurbo application
 */
public class UpdateManager {
    private static final Logger logger = LogManager.getLogger(UpdateManager.class.getName());
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    // Error messages
    private static final String ERROR_INIT_UPDATE = "Failed to initialize update";
    private static final String ERROR_DOWNLOAD_UPDATE_DATA = "Failed to download update data";
    private static final String ERROR_DOWNLOAD_UPDATE_APP = "Failed to download updated application";

    // Directories and file names
    private static final String UPDATE_DIRECTORY = "updates";
    private static final String UPDATE_DATA_SERVER_LINK =
            "https://raw.githubusercontent.com/HubTurbo/HubTurbo/master/HubTurboUpdate.json";
    private static final String UPDATE_DATA_LOCAL_PATH = UPDATE_DIRECTORY + File.separator + "HubTurbo.json";
    private static final String APP_NAME = "HubTurbo.jar";
    private static final String UPDATE_APP_PATH = UPDATE_DIRECTORY + File.separator + APP_NAME;
    public static final String UPDATE_CONFIG_FILENAME = "updateConfig.json";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIRECTORY + File.separator + "jarUpdater.jar";

    // Constants

    /**
     * The number of past HT version JARs to be kept
     */
    private static final int MAX_HT_BACKUP_JAR_KEPT = 3;
    private static final String HT_BACKUP_FILENAME_PATTERN_STRING =
            "HubTurbo_(" + Version.VERSION_PATTERN_STRING + ")\\.(jar|JAR)$";

    // Class member variables
    private UpdateConfig updateConfig;
    private final UpdateProgressWindow updateProgressWindow;
    private final UI ui;
    private boolean isUserWantsImmediateUpdate;
    private boolean hasToClearCache;

    public UpdateManager(UI ui, UpdateProgressWindow updateProgressWindow) {
        this.ui = ui;
        this.updateProgressWindow = updateProgressWindow;
        this.isUserWantsImmediateUpdate = false;
        loadUpdateConfig();
    }

    public void runMigration() {
        Version currentVersion = Version.getCurrentVersion();
        Version lastUsedVersion = updateConfig.getLastUsedHtVersion();

        hasToClearCache = false;

        if (currentVersion.getMajor() != lastUsedVersion.getMajor()) {
            hasToClearCache = true;
        }

        // TODO Here run preference migration

        updateConfig.setLastUsedHtVersion(currentVersion);
    }

    public boolean isCacheToBeCleared() {
        return hasToClearCache;
    }

    /**
     * Driver method to trigger UpdateManager to run. Update will be run on another thread.
     *
     * - Run is not automatic upon instancing the class in case there would like to be conditions on when to run update,
     *   e.g. only if user is logged in
     */
    public void run() {
        pool.execute(() -> runUpdate());
    }

    /**
     * Runs update sequence.
     */
    private void runUpdate() {
        // Fail if folder cannot be created
        if (!initUpdate()) {
            logger.error(ERROR_INIT_UPDATE);
            return;
        }

        cleanupHTBackupJars();

        if (!downloadUpdateData()) {
            logger.error(ERROR_DOWNLOAD_UPDATE_DATA);
            return;
        }

        // Checks if there is a new update since last update
        Optional<HtDownloadLink> htDownloadLink = getLatestHtDownloadLinkForCurrentVersion();

        if (!htDownloadLink.isPresent() || !isVersionDownloadableUpdate(htDownloadLink.get().getVersion())) {
            logger.info("No update to download");
            return;
        }

        if (!downloadUpdateForApplication(htDownloadLink.get().getDownloadLinkUrl())) {
            logger.error(ERROR_DOWNLOAD_UPDATE_APP);
            return;
        }

        markAppUpdateDownloadSuccess(htDownloadLink.get().getVersion());

        // Prompt user for restarting application to apply update
        promptUserToApplyUpdateImmediately();

    }

    /**
     * Initializes system for updates
     * - Creates directory(ies) for updates
     * - Extract jarUpdater
     */
    private boolean initUpdate() {
        logger.info("Initiating updater");
        File updateDir = new File(UPDATE_DIRECTORY);

        if (!updateDir.exists() && !updateDir.mkdirs()) {
            logger.error("Failed to create update directories");
            return false;
        }

        updateConfig.setLastAppUpdateDownloadSuccessful(false);
        saveUpdateConfig();

        try {
            extractJarUpdater();
        } catch (IOException e) {
            logger.error("Failed to extract Jar Updater", e);
            return false;
        }

        return true;
    }

    private void extractJarUpdater() throws IOException {
        logger.info("Extracting jarUpdater");
        File jarUpdaterFile = new File(JAR_UPDATER_APP_PATH);

        if (!jarUpdaterFile.exists() && !jarUpdaterFile.createNewFile()) {
            logger.error("Failed to create Jar Updater empty file");
            throw new IOException("Failed to create Jar Updater empty file");
        }

        try (InputStream in = UpdateManager.class.getClassLoader().getResourceAsStream("updater/jarUpdater");
             OutputStream out = new FileOutputStream(jarUpdaterFile)) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Keeps the number of HT Jar in the folder used as backup to specified amount.
     */
    private void cleanupHTBackupJars() {
        logger.info("Cleaning up backup JAR");

        File currDirectory = new File(".");

        File[] filesInCurrentDirectory = currDirectory.listFiles();

        if (filesInCurrentDirectory == null) {
            // current directory always exists
            assert false;
            return;
        }
        assert filesInCurrentDirectory != null; // to prevent findBugs warning

        List<File> listOfFilesInCurrDirectory = Arrays.asList(filesInCurrentDirectory);

        // Exclude current version in case user is running backup Jar
        List<File> allHtBackupFiles = listOfFilesInCurrDirectory.stream()
                .filter(f -> !f.getName().equals(String.format("HubTurbo_%s.jar", Version.getCurrentVersion())) &&
                             f.getName().matches(HT_BACKUP_FILENAME_PATTERN_STRING))
                .sorted(getHtBackupFileComparatorByVersion())
                .collect(Collectors.toList());

        if (allHtBackupFiles.isEmpty()) {
            return;
        }

        for (int i = 0; i < (allHtBackupFiles.size() - MAX_HT_BACKUP_JAR_KEPT); i++) {
            logger.info("Deleting " + allHtBackupFiles.get(i).getName());
            if (!allHtBackupFiles.get(i).delete()) {
                logger.warn("Failed to delete old HT backup file " + allHtBackupFiles.get(i).getName());
            }
        }
    }

    private Comparator<File> getHtBackupFileComparatorByVersion() {
        return (a, b) -> getVersionOfHtBackupFileFromFilename(a.getName())
                .compareTo(getVersionOfHtBackupFileFromFilename(b.getName()));
    }

    /**
     * Gets version of HubTurbo from Jar backup file.
     * Expects filename in format "HubTurbo_V[major].[minor].[patch].jar".
     * @param filename filename of HT backup JAR, in format "HubTurbo_V[major].[minor].[patch].jar"
     * @return version of HT of backup JAR
     */
    private Version getVersionOfHtBackupFileFromFilename(String filename) {
        Pattern htJarBackupFilenamePattern = Pattern.compile(HT_BACKUP_FILENAME_PATTERN_STRING);
        Matcher htJarBackupFilenameMatcher = htJarBackupFilenamePattern.matcher(filename);
        if (!htJarBackupFilenameMatcher.find()) {
            assert false;
        }

        return Version.fromString(htJarBackupFilenameMatcher.group(1));
    }

    /**
     * Downloads update data to check if update is present.
     *
     * @return true if download successful, false otherwise
     */
    private boolean downloadUpdateData() {
        logger.info("Downloading update data");
        try {
            FileDownloader fileDownloader = new FileDownloader(
                new URI(UPDATE_DATA_SERVER_LINK),
                new File(UPDATE_DATA_LOCAL_PATH),
                a -> {});
            return fileDownloader.download();
        } catch (URISyntaxException e) {
            logger.error(ERROR_DOWNLOAD_UPDATE_DATA, e);
            return false;
        }
    }

    /**
     * Downloads application update based on update data.
     *
     * @return true if download successful, false otherwise
     */
    private boolean downloadUpdateForApplication(URL downloadURL) {
        logger.info("Downloading update for application");
        URI downloadUri;

        try {
            downloadUri = downloadURL.toURI();
        } catch (URISyntaxException e) {
            logger.error("Download URI is not correct", e);
            return false;
        }

        logger.info("Downloading from " + downloadUri.toString());

        LabeledDownloadProgressBar downloadProgressBar = updateProgressWindow.createNewDownloadProgressBar(
                downloadUri, "Downloading HubTurbo Application...");

        FileDownloader fileDownloader = new FileDownloader(
                downloadUri,
                new File(UPDATE_APP_PATH),
                downloadProgressBar::setProgress);
        boolean result = fileDownloader.download();

        updateProgressWindow.removeDownloadProgressBar(downloadUri);

        return result;
    }

    /**
     * Checks if a given version is an updated version that can be downloaded.
     * If that version was previously downloaded (even if newer than current), we will not download it again.
     *
     * Scenario: on V0.0.0, V1.0.0 was downloaded. However, user is still using V0.0.0 and there is no newer update
     *           than V1.0.0. HT won't download V1.0.0 again because the fact that user is still in V0.0.0 means he
     *           does not want to use V0.0.0 (either V1.0.0 is broken or due to other reasons).
     *
     * @param version version to be checked if it is an update
     * @return true if the given version can be downloaded, false otherwise
     */
    private boolean isVersionDownloadableUpdate(Version version) {
        return Version.getCurrentVersion().compareTo(version) < 0 && !updateConfig.wasPreviouslyDownloaded(version);
    }

    /**
     * Gets latest HT version available to download from update data
     * @return download link to a HT version
     */
    private Optional<HtDownloadLink> getLatestHtDownloadLinkForCurrentVersion() {
        File updateDataFile = new File(UPDATE_DATA_LOCAL_PATH);
        JsonFileConverter jsonUpdateDataConverter = new JsonFileConverter(updateDataFile);
        UpdateData updateData = jsonUpdateDataConverter.loadFromFile(UpdateData.class).orElse(new UpdateData());

        return updateData.getLatestUpdateDownloadLinkForCurrentVersion();
    }

    private void markAppUpdateDownloadSuccess(Version versionDownloaded) {
        updateConfig.setLastAppUpdateDownloadSuccessful(true);
        updateConfig.addToVersionPreviouslyDownloaded(versionDownloaded);
        saveUpdateConfig();
    }

    private void loadUpdateConfig() {
        File updateConfigFile = new File(Preferences.DIRECTORY + File.separator + UPDATE_CONFIG_FILENAME);
        JsonFileConverter jsonConverter = new JsonFileConverter(updateConfigFile);
        this.updateConfig = jsonConverter.loadFromFile(UpdateConfig.class).orElse(new UpdateConfig());
    }

    private void saveUpdateConfig() {
        File updateConfigFile = new File(Preferences.DIRECTORY + File.separator + UPDATE_CONFIG_FILENAME);
        JsonFileConverter jsonConverter = new JsonFileConverter(updateConfigFile);
        try {
            jsonConverter.saveToFile(updateConfig);
        } catch (IOException e) {
            logger.warn("Failed to save Update Config", e);
        }
    }

    /**
     * Runs updating clean up on quitting HubTurbo
     */
    public void onAppQuit() {
        if (!isUserWantsImmediateUpdate && updateConfig.isLastAppUpdateDownloadSuccessful()) {
            updateConfig.setLastAppUpdateDownloadSuccessful(false);
            saveUpdateConfig();

            runJarUpdaterWithoutExecute();
        }
    }

    public void showUpdateProgressWindow() {
        updateProgressWindow.showWindow();
    }

    public void hideUpdateProgressWindow() {
        updateProgressWindow.hideWindow();
    }

    private boolean runJarUpdaterWithExecute() {
        return runJarUpdater(true);
    }

    private boolean runJarUpdaterWithoutExecute() {
        return runJarUpdater(false);
    }

    private boolean runJarUpdater(boolean shouldExecuteJar) {
        String restarterAppPath = JAR_UPDATER_APP_PATH;
        String cmdArg = String.format("--source=%s --target=%s --execute-jar=%s --backup-suffix=_%s",
                UPDATE_APP_PATH, APP_NAME, shouldExecuteJar ? "y" : "n", Version.getCurrentVersion().toString());

        String command = String.format("java -jar %1$s %2$s", restarterAppPath, cmdArg);
        logger.info("Executing JAR of restarter with command: " + command);

        Process process = null;

        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            logger.error("Failed to run restarter.", e);
            return false;
        }

        if (!process.isAlive()) {
            logger.error("JAR restarter is not running.");
            return false;
        }
        return true;
    }

    private void promptUserToApplyUpdateImmediately() {
        Platform.runLater(() -> {
            String message = String.format("This will quit the application and restart it.%n" +
                    "Otherwise, update will be applied when you exit HubTurbo.");
            isUserWantsImmediateUpdate = DialogMessage.showYesNoWarningDialog("Update application",
                    "Would you like to update HubTurbo now?",
                    message,
                    "Yes", "No");
            if (isUserWantsImmediateUpdate && runJarUpdaterWithExecute()) {
                logger.info("Quitting application to apply update");
                ui.quit();
            }
        });
    }
}
