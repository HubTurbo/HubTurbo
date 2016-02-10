package updater;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.net.URI;

/**
 * This class is used to show download progress. It consists of a progress bar with its
 * label (e.g. "Downloading file.txt") as well as the URL of the download for identification.
 */
public class LabeledDownloadProgressBar {
    private final URI uriIdentifier;
    private final String downloadLabel;
    private final ProgressBar progressBar;

    public LabeledDownloadProgressBar(URI uriIdentifier, String downloadLabel, ProgressBar progressBar) {
        this.uriIdentifier = uriIdentifier;
        this.downloadLabel = downloadLabel;
        this.progressBar = progressBar;
    }

    /**
     * Gets URL identifier of a download progress bar
     * @return URL identifier of download progress bar
     */
    public URI getUriIdentifier() {
        return this.uriIdentifier;
    }

    /**
     * Gets the download label to be associated with the progress bar
     * @return download label of progress bar in string
     */
    public String getDownloadLabel() {
        return this.downloadLabel;
    }

    /**
     * Gets the progress bar object
     * @return progress bar object
     */
    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    /**
     * Sets the value of progress bar
     * @param progressVal value of progress bar
     */
    public void setProgress(double progressVal) {
        Platform.runLater(() -> this.progressBar.setProgress(progressVal));
    }
}
