package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Pair;
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
        Optional<Pair<ButtonType, Integer>> milestoneDialogResponse = dialog.showAndWait();

        if (!milestoneDialogResponse.isPresent() ||
                milestoneDialogResponse.get().getKey().equals(ButtonType.CANCEL)) return;

        Optional<Integer> newlyAssignedMilestone = Optional.ofNullable(milestoneDialogResponse.get().getValue());

        if (!issue.getMilestone().equals(newlyAssignedMilestone)) {
            ui.undoController.addAction(issue, new ChangeMilestoneAction(ui.logic, issue.getMilestone(),
                    newlyAssignedMilestone));
        }
    }

}
