package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UI;
import ui.components.Notification;
import undo.ChangeLabels;
import util.DialogMessage;
import util.events.ShowLabelPickerEventHandler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelPicker {

    private static final String OCTICON_INFO = "\uf059";

    private final UI ui;
    private final Stage stage;

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
            replaceLabels(issue, result.get().stream().sorted().collect(Collectors.toList()));
        }
    }

    private void replaceLabels(TurboIssue issue, List<String> labels) {
        List<String> originalLabels = issue.getLabels().stream().sorted().collect(Collectors.toList());
        if (!labels.equals(originalLabels)) {
            List<String> resultingLabels = createResultingLabelList(issue, labels);
            assert labels.equals(resultingLabels);
            ui.logic.replaceIssueLabelsUI(issue, resultingLabels);
            Notification undoNotification = new Notification(createInfoOcticon(),
                    "Undo label change(s) for #" + issue.getId() + ": " + issue.getTitle(),
                    "Undo",
                    () -> ui.logic.replaceIssueLabelsRepo(issue, resultingLabels, originalLabels)
                            .thenApply(success -> showErrorDialogOnFailure(success, issue)),
                    () -> ui.logic.replaceIssueLabelsUI(issue, originalLabels));
            ui.showNotification(undoNotification);
        }
    }

    private boolean showErrorDialogOnFailure(Boolean success, TurboIssue issue) {
        if (!success) {
            // if not successful, show error dialog
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

    private Label createInfoOcticon() {
        Label label = new Label(OCTICON_INFO);
        label.setPadding(new Insets(0, 0, 5, 0));
        label.getStyleClass().addAll("octicon");
        return label;
    }

    private List<String> createResultingLabelList(TurboIssue issue, List<String> newLabels) {
        return new ChangeLabels(
            newLabels.stream()
                .filter(newLabel -> !issue.getLabels().contains(newLabel))
                .collect(Collectors.toList()),
            issue.getLabels().stream()
                .filter(originalLabel -> !newLabels.contains(originalLabel))
                .collect(Collectors.toList()))
            .act(issue).getLabels();
    }
}
