package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    private TextField textField;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels, Stage stage) {
        System.out.print("All Labels: ");
        allLabels.forEach(label -> System.out.print(label + " "));
        System.out.println();
        System.out.print("Current Labels: ");
        issue.getLabels().forEach(label -> System.out.print(label + " "));
        System.out.println();

        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs

        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
        setHeaderText((issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId() + ": " + issue.getTitle());

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        textField = new TextField();
        textField.setPrefColumnCount(30);

        ObservableList<Label> labels = FXCollections.observableArrayList
                        (allLabels.stream()
                        .map(label -> new Label(label.getActualName()))
                        .collect(Collectors.toList()));
        ListView<Label> labelList = new ListView<>(labels);
        labelList.setCellFactory(CheckBoxListCell.forListView(Label::selectedProperty, new StringConverter<Label>() {
            @Override
            public String toString(Label object) {
                return object.getName();
            }

            @Override
            public Label fromString(String string) {
                return null;
            }
        }));

        vBox.getChildren().addAll(textField, labelList);
        getDialogPane().setContent(vBox);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return issue.getLabels();
            }
            return null;
        });

        requestFocus();
    }

    protected void requestFocus() {
        Platform.runLater(textField::requestFocus);
    }

    public static class Label {

        private ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
        private BooleanProperty selected = new SimpleBooleanProperty(false);

        public Label(String name) {
            this.name.set(name);
        }

        public String getName() {
            return name.get();
        }

        public ReadOnlyStringProperty nameProperty() {
            return name.getReadOnlyProperty();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

    }

}
