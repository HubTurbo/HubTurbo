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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    private TextField textField;
    private TurboIssue issue;
    private List<TurboLabel> allLabels;
    private ObservableList<LabelPicker.Label> labels;
    private ListView<LabelPicker.Label> labelListView;
    private Map<String, Boolean> resultList;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels, Stage stage) {
        this.issue = issue;
        this.allLabels = allLabels;
        resultList = new HashMap<>();
        allLabels.forEach(label -> resultList.put(label.getActualName(),
                issue.getLabels().contains(label.getActualName())));

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
        setupKeyEvents();

        updateLabelsList("");
        labelListView = new ListView<>(labels);
        labelListView.setCellFactory(LabelPickerCell.forListView(LabelPicker.Label::selectedProperty,
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

        vBox.getChildren().addAll(textField, labelListView);
        getDialogPane().setContent(vBox);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return allLabels
                        .stream()
                        .filter(label -> resultList.get(label.getName()))
                        .map(TurboLabel::getActualName)
                        .collect(Collectors.toList());
            }
            return null;
        });

        requestFocus();
    }

    protected void requestFocus() {
        Platform.runLater(textField::requestFocus);
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLabelsList(newValue);
            labelListView.setItems(null);
            labelListView.setItems(labels);
        });
    }

    private void updateLabelsList(String match) {
        List<TurboLabel> matchedLabels = allLabels
                .stream()
                .filter(label -> label.getActualName().contains(match))
                .collect(Collectors.toList());
        ObservableList<LabelPicker.Label> selectedLabels = FXCollections.observableArrayList(matchedLabels.stream()
                .filter(label -> resultList.get(label.getActualName()))
                .map(label -> new LabelPicker.Label(label.getActualName(), label.getStyle(), true))
                .collect(Collectors.toList()));
        ObservableList<LabelPicker.Label> notSelectedLabels = FXCollections.observableArrayList(matchedLabels.stream()
                .filter(label -> !resultList.get(label.getActualName()))
                .map(label -> new LabelPicker.Label(label.getActualName(), label.getStyle(), false))
                .collect(Collectors.toList()));
        labels = FXCollections.concat(selectedLabels, notSelectedLabels);
        labels.forEach(label -> label.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            resultList.put(label.getName(), isSelected);
        }));
    }

}
