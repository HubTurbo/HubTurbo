package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Pair;
import ui.UI;
import undo.actions.ChangeAssigneeAction;
import util.events.ShowAssigneePickerEventHandler;

import java.util.List;
import java.util.Optional;

public class AssigneePicker {

    private final UI ui;
    private final Stage stage;

    public AssigneePicker(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowAssigneePickerEventHandler) e -> Platform.runLater(() -> showAssigneePicker(e.issue)));
    }

    private void showAssigneePicker(TurboIssue issue) {

        List<TurboUser> assigneeList = ui.logic.getRepo(issue.getRepoId()).getUsers();
        AssigneePickerDialog assigneePickerDialog = new AssigneePickerDialog(stage, issue, assigneeList);
        Optional<Pair<ButtonType, String>> assigneeDialogResponse = assigneePickerDialog.showAndWait();

        if (!assigneeDialogResponse.isPresent() ||
                assigneeDialogResponse.get().getKey().equals(ButtonType.CANCEL)) {
            return;
        }

        Optional<String> newlyAssignedAssignee = Optional.ofNullable(assigneeDialogResponse.get().getValue());
        if (!issue.getAssignee().equals(newlyAssignedAssignee)) {
            ui.undoController.addAction(issue, new ChangeAssigneeAction(ui.logic, issue.getAssignee(),
                    newlyAssignedAssignee));
        }
    }
}
