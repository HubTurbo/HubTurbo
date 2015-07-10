package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.DialogMessage;
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
        List<TurboLabel> allLabels = ui.logic.getRepo(issue.getRepoId()).getLabels();
        LabelPickerDialog labelPickerDialog = new LabelPickerDialog(issue, allLabels, stage);
        Optional<List<String>> result = labelPickerDialog.showAndWait();
        if (result.isPresent()) {
            ui.logic.replaceIssueLabels(issue, result.get())
                    .thenApply(success -> postLabelApplication(success, issue));
        }
    }

    public boolean postLabelApplication(Boolean success, TurboIssue issue) {
        if (success) {
            ui.getBrowserComponent().showIssue(
                    issue.getRepoId(), issue.getId(), issue.isPullRequest(), true
            );
        } else {
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
