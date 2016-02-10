package updater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UpdateProgressWindow;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The class that will handle updating of HubTurbo application
 */
public class UpdateManager {
    private static final Logger logger = LogManager.getLogger(UpdateManager.class.getName());

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

    // Class member variables
    private final UpdateProgressWindow updateProgressWindow;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public UpdateManager(UpdateProgressWindow updateProgressWindow) {
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

        // TODO prompt user for restarting application to apply update
        // If yes, quit application and run new process that will:
        // - replace JAR
        // - start the new JAR

    }

    /**
     * Initializes system for updates
     * - Creates directory(ies) for updates
     */
    private boolean initUpdate() {
        File updateDir = new File(UPDATE_DIRECTORY);

        if (!updateDir.exists() && !updateDir.mkdirs()) {
            logger.error("Failed to create update directories");
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
}
