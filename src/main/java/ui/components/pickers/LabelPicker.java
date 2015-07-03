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
        System.out.println("All Labels: ");
        allLabels.forEach(label -> System.out.println("Label: " + label));
        List<String> currentLabels = ui.logic.getRepo(issue.getRepoId()).getIssueById(issue.getId()).get().getLabels();
        System.out.println("Current Labels: ");
        currentLabels.forEach(label -> System.out.println("Label: " + label));
        ui.logic.replaceIssueLabels(issue.getRepoId(), issue.getId(), currentLabels);
    }

}
