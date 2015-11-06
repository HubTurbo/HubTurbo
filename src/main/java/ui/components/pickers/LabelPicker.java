package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UI;
import ui.components.Notification;
import undo.actions.ChangeLabelsAction;
import util.DialogMessage;
import util.events.ShowLabelPickerEventHandler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelPicker {

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
            List<String> originalLabels = issue.getLabels().stream().sorted().collect(Collectors.toList());
            List<String> newLabels = result.get().stream().sorted().collect(Collectors.toList());
            if (!newLabels.equals(originalLabels)) {
                ui.undoController.addAction(issue, ChangeLabelsAction.createChangeLabelsAction(issue, newLabels));
            }
        }
    }

}
