package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.DialogMessage;
import util.GitHubURL;
import util.events.ShowLabelPickerEventHandler;

import java.util.List;
import java.util.Optional;

public class LabelPicker {

    private UI ui;
    private Stage stage;

    // A LabelPicker is created by trigger a ShowLabelPickerEvent.
    public LabelPicker(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowLabelPickerEventHandler) e -> Platform.runLater(() -> showLabelPicker(e.issue)));
    }

    // TODO implement multiple dialogs, currently, only one dialog is allowed and it blocks the main UI when open

    private void showLabelPicker(TurboIssue issue) {
        // get all labels from issue's repo
        List<TurboLabel> allLabels = ui.logic.getRepo(issue.getRepoId()).getLabels();
        // create new LabelPickerDialog
        LabelPickerDialog labelPickerDialog = new LabelPickerDialog(issue, allLabels, stage);
        // show LabelPickerDialog and wait for result
        Optional<List<String>> result = labelPickerDialog.showAndWait();
        stage.show(); // ensures stage is showing after label picker is closed (mostly for tests)
        // if result is present (user did not cancel) then replace issue labels with result
        if (result.isPresent()) {
            ui.logic.replaceIssueLabels(issue, result.get())
                    .thenApply(success -> postReplaceLabelActions(success, issue));
        }
    }

    public boolean postReplaceLabelActions(Boolean success, TurboIssue issue) {
        if (success) {
            // if label replacement is successful, force refresh issue page only if already on that issue page
            if (ui.getBrowserComponent().getCurrentUrl().startsWith(
                    GitHubURL.getPathForPullRequest(issue.getRepoId(), issue.getId())) ||
                    ui.getBrowserComponent().getCurrentUrl().startsWith(
                            GitHubURL.getPathForIssue(issue.getRepoId(), issue.getId()))) {
                ui.getBrowserComponent().showIssue(issue.getRepoId(), issue.getId(), issue.isPullRequest(), true);
            }
        } else {
            // if not, show error dialog
            Platform.runLater(() -> DialogMessage.showErrorDialog(
                "GitHub Write Error",
                String.format(
                    "An error occurred while attempting to apply labels to:\n\n%s\n\n"
                            + "Please check if you have write permissions to %s.",
                    issue,
                    issue.getRepoId()
                )
            ));
        }
        return success;
    }

}
