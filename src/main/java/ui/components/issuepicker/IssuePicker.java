package ui.components.issuepicker;

import java.util.Optional;

import backend.resource.MultiModel;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.events.ShowIssuePickerEventHandler;

public class IssuePicker {

    private final Stage stage;

    public IssuePicker(UI ui, Stage stage) {
        this.stage = stage;
        ui.registerEvent((ShowIssuePickerEventHandler) e ->
                Platform.runLater(() -> showIssuePicker(ui.logic.getModels())));
    }

    private Optional<String> showIssuePicker(MultiModel models) {
        IssuePickerDialog issuePickerDialog = new IssuePickerDialog(stage, models);
        return issuePickerDialog.showAndWait();

    }
}
