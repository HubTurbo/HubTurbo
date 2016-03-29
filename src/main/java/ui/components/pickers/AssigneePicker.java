package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
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
        List<TurboUser> assignees = ui.logic.getRepo(issue.getRepoId()).getUsers();
        Optional<AssigneePickerDialog.AssigneePickerDialogResponse> assigneeDialogResponse =
                new AssigneePickerDialog(stage, issue, assignees).showAndWait();

        if (wasCancelled(assigneeDialogResponse)) {
            return;
        }

        Optional<String> newlyAssignedAssignee = assigneeDialogResponse.get().getAssigneeLoginName();
        changeAssignee(issue, newlyAssignedAssignee);
    }

    private boolean wasCancelled(Optional<AssigneePickerDialog.AssigneePickerDialogResponse> assigneeDialogResponse) {
        return !assigneeDialogResponse.isPresent() ||
                assigneeDialogResponse.get().getButtonClicked().equals(ButtonType.CANCEL);
    }

    private void changeAssignee(TurboIssue issue, Optional<String> newlyAssignedAssignee) {
        if (!issue.getAssignee().equals(newlyAssignedAssignee)) {
            ui.undoController.addAction(issue,
                    new ChangeAssigneeAction(ui.logic, issue.getAssignee(), newlyAssignedAssignee));
        }
    }
}
