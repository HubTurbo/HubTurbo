package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import javafx.util.Pair;
import ui.UI;
import util.events.ShowLabelPickerEventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LabelPicker {

    private UI ui;
    private Stage stage;
    private Map<Pair<String, Integer>, LabelPickerDialog> openDialogs;

    public LabelPicker(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        openDialogs = new HashMap<>();
        ui.registerEvent((ShowLabelPickerEventHandler) e -> Platform.runLater(() -> showLabelPicker(e.issue)));
    }

    // TODO implement multiple dialogs, currently, only one dialog is allowed and it blocks the main UI when open

    private void showLabelPicker(TurboIssue issue) {
        if (!openDialogs.containsKey(new Pair<>(issue.getRepoId(), issue.getId()))) {
            List<TurboLabel> allLabels = ui.logic.getRepo(issue.getRepoId()).getLabels();
            LabelPickerDialog labelPickerDialog = new LabelPickerDialog(issue, allLabels, stage);
            openDialogs.put(new Pair<>(issue.getRepoId(), issue.getId()), labelPickerDialog);
            Optional<List<String>> result = labelPickerDialog.showAndWait();
            if (result.isPresent()) {
                ui.logic.replaceIssueLabels(issue, result.get());
            }
            openDialogs.remove(new Pair<>(issue.getRepoId(), issue.getId()));
        } else {
            // TODO focus on the already open dialog when having multiple dialogs
            openDialogs.get(new Pair<>(issue.getRepoId(), issue.getId())).requestFocus();
        }
    }

    public static class Label {

        private ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
        private ReadOnlyStringWrapper style = new ReadOnlyStringWrapper();
        private BooleanProperty selected = new SimpleBooleanProperty();

        public Label(String name, String style, boolean selected) {
            this.name.set(name);
            this.style.set(style);
            this.selected.set(selected);
        }

        public String getName() {
            return name.get();
        }

        public String getStyle() {
            return style.get();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

    }

}
