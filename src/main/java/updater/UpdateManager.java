package updater;

import javafx.application.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UI;
import ui.UpdateProgressWindow;
import util.DialogMessage;
import util.Utility;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final String UPDATE_SERVER_DATA_NAME =
            "https://raw.githubusercontent.com/HubTurbo/AutoUpdater/master/HubTurbo.xml";
    private static final String UPDATE_LOCAL_DATA_NAME = "HubTurbo.json";
    private static final String UPDATE_APP_NAME = "HubTurbo.jar";
    private static final String UPDATE_JAR_UPDATER_APP_PATH = UPDATE_DIRECTORY + File.separator + "jarUpdater.jar";

    // Class member variables
    private final UpdateProgressWindow updateProgressWindow;
    private final UI ui;

    public UpdateManager(UI ui, UpdateProgressWindow updateProgressWindow) {
        this.ui = ui;
        this.updateProgressWindow = updateProgressWindow;
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

        if (!downloadUpdateData()) {
            logger.error(ERROR_DOWNLOAD_UPDATE_DATA);
            return;
        }

        // TODO check if there is a new update since last update
        // - if there isn't, check if any update has not been applied
        // - if there is, download update according to user preference, i.e. auto or prompted


        if (!downloadUpdateForApplication()) {
            logger.error(ERROR_DOWNLOAD_UPDATE_APP);
            return;
        }

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

        return extractJarUpdater();
    }

    private boolean extractJarUpdater() {
        logger.info("Extracting jarUpdater");
        File jarUpdaterFile = new File(UPDATE_JAR_UPDATER_APP_PATH);

        try {
            jarUpdaterFile.createNewFile();
        } catch (IOException e) {
            logger.error("Can't create empty file for jarUpdater");
            return false;
        }

        try (InputStream in = UpdateManager.class.getClassLoader().getResourceAsStream("updater/jarUpdater");
             OutputStream out = new FileOutputStream(jarUpdaterFile)) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            logger.error("Can't extract jarUpdater", e);
            return false;
        }

        return true;
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
                    new URI(UPDATE_SERVER_DATA_NAME),
                    new File(UPDATE_DIRECTORY + File.separator + UPDATE_LOCAL_DATA_NAME),
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
    private boolean downloadUpdateForApplication() {
        logger.info("Downloading update for application");

        URI downloadUri;

        // TODO replace download source to use updater data
        try {
            downloadUri = new URI(
                    "https://github.com/HubTurbo/HubTurbo/releases/download/V3.18.0/resource-v3.18.0.jar");
        } catch (URISyntaxException e) {
            logger.error("Download URI is not correct", e);
            return false;
        }

        LabeledDownloadProgressBar downloadProgressBar = updateProgressWindow.createNewDownloadProgressBar(
                downloadUri, "Downloading HubTurbo Application...");

        FileDownloader fileDownloader = new FileDownloader(
                downloadUri,
                new File(UPDATE_DIRECTORY + File.separator + UPDATE_APP_NAME),
                downloadProgressBar::setProgress);
        boolean result = fileDownloader.download();

        updateProgressWindow.removeDownloadProgressBar(downloadUri);

        return result;
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

    private boolean runJarUpdater(boolean shouldExecuteJar) {
        String restarterAppPath = UPDATE_JAR_UPDATER_APP_PATH;
        String replaceSourcePath = UPDATE_DIRECTORY + File.separator + UPDATE_APP_NAME;
        String cmdArg = String.format("--source=%1$s --target=%2$s --execute-jar=%3$s",
                replaceSourcePath, UPDATE_APP_NAME, shouldExecuteJar ? "y" : "n");

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
            boolean applyUpdate = DialogMessage.showYesNoWarningDialog("Update application",
                    "Would you like to update HubTurbo now?",
                    "This will quit the application and restart it.", "Yes", "No");
            if (applyUpdate && runJarUpdaterWithExecute()) {
                logger.info("Quitting application to apply update");
                ui.quit();
            }
        });
    }
}
