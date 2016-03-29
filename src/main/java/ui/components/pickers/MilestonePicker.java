package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import ui.UI;
import undo.actions.ChangeMilestoneAction;
import util.events.ShowMilestonePickerEventHandler;

import java.util.List;
import java.util.Optional;

/**
 * This class facilitates the handling of a MilestonePickerDialog
 */
public class MilestonePicker {
    private final UI ui;
    private final Stage stage;

    public MilestonePicker(UI ui, Stage mainStage) {
        this.ui = ui;
        this.stage = mainStage;
        ui.registerEvent((ShowMilestonePickerEventHandler) e -> Platform.runLater(() -> showMilestonePicker(e.issue)));
    }

    /**
     * Shows a MilestonePickerDialog, then waits for its result
     * <p>
     * If the resulting milestone obtained from the dialog is different from the original,
     * it will automatically trigger a milestone change both locally and on the server
     *
     * @param issue
     */
    private void showMilestonePicker(TurboIssue issue) {
        List<TurboMilestone> milestones = ui.logic.getRepo(issue.getRepoId()).getMilestones();
        Optional<MilestonePickerDialogResponse> milestoneDialogResponse =
                new MilestonePickerDialog(stage, issue, milestones).showAndWait();

        if (wasCancelled(milestoneDialogResponse)) return;

        Optional<Integer> newlyAssignedMilestone = milestoneDialogResponse.get().getMilestoneId();
        if (!issue.getMilestone().equals(newlyAssignedMilestone)) {
            addActionIfMilestoneChanged(issue, newlyAssignedMilestone);
        }
    }

    private boolean wasCancelled(Optional<MilestonePickerDialogResponse> milestoneDialogResponse) {
        return !milestoneDialogResponse.isPresent() ||
                milestoneDialogResponse.get().getButtonClicked().equals(ButtonType.CANCEL);
    }

    private void addActionIfMilestoneChanged(TurboIssue issue, Optional<Integer> newlyAssignedMilestone) {
        ui.undoController.addAction(issue,
                                    new ChangeMilestoneAction(ui.logic, issue.getMilestone(), newlyAssignedMilestone));
    }

}
