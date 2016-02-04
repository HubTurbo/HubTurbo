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
        Optional<Integer> assignedMilestone = dialog.showAndWait();;

        if (issue.getMilestone() != assignedMilestone ) {
            ui.undoController.addAction(issue, new ChangeMilestoneAction(ui.logic, issue.getMilestone(), assignedMilestone));
        }
    }

}
