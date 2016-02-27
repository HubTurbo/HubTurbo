package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import updater.LabeledDownloadProgressBar;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the window showing the progress of the downloading of the new update
 */
public class UpdateProgressWindow {
    private static final String WINDOW_TITLE = "Update Progress";
    private static final String LABEL_NO_DOWNLOAD = "There is no update being downloaded.";

    private Stage window;
    private VBox windowMainLayout;
    private final List<LabeledDownloadProgressBar> downloads;

    public UpdateProgressWindow() {
        this.downloads = new ArrayList<>();
        this.initProgressWindow();
    }

    public void showWindow() {
        Platform.runLater(() -> {
                    window.show();
                    window.toFront();
                }
        );
    }

    public void hideWindow() {
        Platform.runLater(window::hide);
    }

    private void initProgressWindow() {
        Stage stage = new Stage();
        stage.setTitle(WINDOW_TITLE);

        windowMainLayout = new VBox();

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        scene.setRoot(windowMainLayout);

        window = stage;

        reloadWindowLayout();
    }

    /**
     * Creates a new download progress bar
     *
     * @param downloadUri uri of download as identifier to the progress bar
     * @param downloadLabel label of progress bar
     * @return the download progress bar created
     */
    public LabeledDownloadProgressBar createNewDownloadProgressBar(URI downloadUri, String downloadLabel) {
        ProgressBar progressBar = new ProgressBar(-1.0);
        progressBar.setPrefWidth(400);
        LabeledDownloadProgressBar downloadProgressBar =
                new LabeledDownloadProgressBar(downloadUri, downloadLabel, progressBar);

        downloads.add(downloadProgressBar);

        reloadWindowLayout();

        return downloadProgressBar;
    }

    /**
     * Removes a download progress bar from update progress window
     *
     * This should be called after a download is completed
     *
     * @param downloadUri download URI to be used as download identifier
     */
    public void removeDownloadProgressBar(URI downloadUri) {
        downloads.removeIf(dpt -> dpt.getUriIdentifier().equals(downloadUri));

        reloadWindowLayout();
    }

    /**
     * Updates the update progress window's layout if there is any changes to download progress to be shown
     */
    private void reloadWindowLayout() {
        assert window != null && windowMainLayout != null;

        Platform.runLater(() -> {
            windowMainLayout.getChildren().clear();

            if (downloads.isEmpty()) {
                Label noDownloadLabel = createNoDownloadLabel();

                windowMainLayout.getChildren().add(noDownloadLabel);
            } else {
                for (LabeledDownloadProgressBar progressTracker : downloads) {
                    VBox downloadProgressItem = createDownloadProgressItem(progressTracker);

                    windowMainLayout.getChildren().add(downloadProgressItem);
                }
            }
        });
    }

    private VBox createDownloadProgressItem(LabeledDownloadProgressBar progressTracker) {
        Label downloadLabel = new Label();
        downloadLabel.setText(progressTracker.getDownloadLabel());

        ProgressBar progressBar = progressTracker.getProgressBar();

        VBox downloadProgressItem = new VBox();
        downloadProgressItem.setSpacing(20);
        downloadProgressItem.setPadding(new Insets(20));
        downloadProgressItem.setAlignment(Pos.CENTER_LEFT);

        downloadProgressItem.getChildren().addAll(downloadLabel, progressBar);

        return downloadProgressItem;
    }

    private Label createNoDownloadLabel() {
        Label noDownloadLabel = new Label();
        noDownloadLabel.setText(LABEL_NO_DOWNLOAD);
        noDownloadLabel.setPadding(new Insets(50));

        return noDownloadLabel;
    }
}
