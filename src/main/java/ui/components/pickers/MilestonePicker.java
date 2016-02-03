package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import undo.actions.ChangeLabelsAction;
import undo.actions.ChangeMilestoneAction;
import util.events.ShowMilestonePickerEvent;
import util.events.ShowMilestonePickerEventHandler;

import java.util.List;
import java.util.Optional;

public class MilestonePicker {
    UI ui;
    Stage stage;
    public MilestonePicker(UI ui, Stage mainStage) {
        this.ui = ui;
        this.stage = mainStage;
        ui.registerEvent((ShowMilestonePickerEventHandler) e -> Platform.runLater(() -> showMilestonePicker(e.issue)));
    }

    private void showMilestonePicker(TurboIssue issue) {
        List<TurboMilestone> milestoneList = ui.logic.getRepo(issue.getRepoId()).getMilestones();
        MilestonePickerDialog dialog = new MilestonePickerDialog(stage, issue, milestoneList);
        Optional<TurboMilestone> assignedMilestone = dialog.showAndWait();
        if (assignedMilestone.isPresent()) {
            if (issue.getMilestone().isPresent()) {
                ui.undoController.addAction(issue, new ChangeMilestoneAction(ui.logic, issue.getMilestone().get(), assignedMilestone.get().getId()));
            } else {
                ui.undoController.addAction(issue, new ChangeMilestoneAction(ui.logic, null, assignedMilestone.get().getId()));
            }
        }
    }

}
