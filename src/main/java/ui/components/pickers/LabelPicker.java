package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import ui.UI;
import util.events.ShowLabelPickerEventHandler;

import java.util.List;

public class LabelPicker {

    private UI ui;

    public LabelPicker(UI ui) {
        this.ui = ui;
        ui.registerEvent((ShowLabelPickerEventHandler) e -> Platform.runLater(() -> showLabelPicker(e.issue)));
    }

    private void showLabelPicker(TurboIssue issue) {
        List<TurboLabel> allLabels = ui.logic.getRepo(issue.getRepoId()).getLabels();
        ui.logic.replaceIssueLabels(issue, showLabelPickerDialog(issue, allLabels));
    }

    private List<String> showLabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels) {
        System.out.print("All Labels: ");
        allLabels.forEach(label -> System.out.print(label + " "));
        System.out.println();
        System.out.print("Current Labels: ");
        issue.getLabels().forEach(label -> System.out.print(label + " "));
        System.out.println();
        return issue.getLabels();
    }

}
