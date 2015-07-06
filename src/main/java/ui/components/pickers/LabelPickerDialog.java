package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    private TextField textField;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels, Stage stage) {
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

        // TODO sort list with current labels at the top
        ObservableList<LabelPicker.Label> labels = FXCollections.observableArrayList
                        (allLabels.stream()
                        .map(label -> new LabelPicker.Label(label.getActualName(),
                                label.getStyle(),
                                issue.getLabels().contains(label.getActualName())))
                        .collect(Collectors.toList()));
        ListView<LabelPicker.Label> labelList = new ListView<>(labels);
        labelList.setCellFactory(LabelPickerCell.forListView(LabelPicker.Label::selectedProperty,
                new StringConverter<LabelPicker.Label>() {
            @Override
            public String toString(LabelPicker.Label object) {
                return object.getName();
            }

            @Override
            public LabelPicker.Label fromString(String string) {
                return null;
            }
        }));

        vBox.getChildren().addAll(textField, labelList);
        getDialogPane().setContent(vBox);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return labels
                        .stream()
                        .filter(LabelPicker.Label::isSelected)
                        .map(LabelPicker.Label::getName)
                        .collect(Collectors.toList());
            }
            return null;
        });

        requestFocus();
    }

    protected void requestFocus() {
        Platform.runLater(textField::requestFocus);
    }

}
