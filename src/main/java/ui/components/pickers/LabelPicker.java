package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import ui.UI;
import util.events.ShowLabelPickerEventHandler;

import java.util.*;

public class LabelPicker {

    private UI ui;
    private Set<Pair<String, Integer>> openDialogs;

    public LabelPicker(UI ui) {
        this.ui = ui;
        openDialogs = new HashSet<>();
        ui.registerEvent((ShowLabelPickerEventHandler) e -> Platform.runLater(() -> showLabelPicker(e.issue)));
    }

    private void showLabelPicker(TurboIssue issue) {
        if (!openDialogs.contains(new Pair<>(issue.getRepoId(), issue.getId()))) {
            openDialogs.add(new Pair<>(issue.getRepoId(), issue.getId()));
            List<TurboLabel> allLabels = ui.logic.getRepo(issue.getRepoId()).getLabels();
            ui.logic.replaceIssueLabels(issue, showLabelPickerDialog(issue, allLabels));
            openDialogs.remove(new Pair<>(issue.getRepoId(), issue.getId()));
        } else {
            // focus on existing dialog
        }
    }

    private List<String> showLabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels) {
        System.out.print("All Labels: ");
        allLabels.forEach(label -> System.out.print(label + " "));
        System.out.println();
        System.out.print("Current Labels: ");
        issue.getLabels().forEach(label -> System.out.print(label + " "));
        System.out.println();

        Dialog labelPickerDialog = new Dialog();
        labelPickerDialog.initStyle(StageStyle.UNIFIED);
        labelPickerDialog.initModality(Modality.NONE);
        labelPickerDialog.showAndWait();

        return issue.getLabels();
    }

}
