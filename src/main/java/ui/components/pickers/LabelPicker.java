package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import ui.UI;
import util.DialogMessage;
import util.GitHubURL;
import util.events.ShowLabelPickerEventHandler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelPicker {

    private static final String OCTICON_INFO = "\uf059";

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
        // get original labels for undo
        List<String> originalLabels = issue.getLabels();
        // create new LabelPickerDialog
        LabelPickerDialog labelPickerDialog = new LabelPickerDialog(issue, allLabels, stage);
        // show LabelPickerDialog and wait for result
        Optional<List<String>> result = labelPickerDialog.showAndWait();
        stage.show(); // ensures stage is showing after label picker is closed (mostly for tests)
        // if result is present (user did not cancel) then replace issue labels with result
        if (result.isPresent()) {
            ui.logic.replaceIssueLabels(issue, result.get().stream().sorted().collect(Collectors.toList()))
                    .thenApply(success -> postReplaceLabelActions(success, issue, originalLabels));
        }
    }

    public boolean postReplaceLabelActions(Boolean success, TurboIssue issue, List<String> originalLabels) {
        if (success) {
            refreshIssuePage(issue);
            ui.showNotificationPane(createInfoOcticon(),
                    "Undo label change(s) for #" + issue.getId() + ": " + issue.getTitle(),
                    new Action("Undo", actionEvent ->
                            ui.logic.replaceIssueLabels(issue, originalLabels)
                                    .thenRun(() -> {
                                        refreshIssuePage(issue);
                                        Platform.runLater(ui::hideNotificationPane);
                                    })));
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

    private void refreshIssuePage(TurboIssue issue) {
        // if label replacement is successful, force refresh issue page only if already on that issue page
        if (GitHubURL.isOnSpecificIssuePage(issue, ui.getBrowserComponent().getCurrentUrl())) {
            ui.getBrowserComponent().showIssue(issue.getRepoId(), issue.getId(), issue.isPullRequest(), true);
        }
    }

    private Label createInfoOcticon() {
        Label label = new Label(OCTICON_INFO);
        label.setPadding(new Insets(0, 0, 5, 0));
        label.getStyleClass().addAll("octicon");
        return label;
    }

}
