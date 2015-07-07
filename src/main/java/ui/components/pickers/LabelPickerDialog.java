package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
    private LabelListView labelListView;
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

        Label instructions = new Label("UP/DOWN to navigate, TAB to toggle selection");
        instructions.setPadding(new Insets(0, 0, 10, 0));

        updateLabelsList("");
        labelListView = new LabelListView(this);
        labelListView.setItems(labels);
        labelListView.setCellFactory(LabelPickerCell.forListView(LabelPicker.Label::checkedProperty,
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

        vBox.getChildren().addAll(instructions, textField, labelListView);
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

        labelListView.setFirstItem();
        requestFocus();
    }

    protected void requestFocus() {
        Platform.runLater(textField::requestFocus);
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLabelsList(newValue);
            labelListView.setItems(labels);
            labelListView.setFirstItem();
            labelListView.setItems(null);
            labelListView.setItems(labels);
        });
        textField.setOnKeyPressed(e -> {
            if (!e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
                if (e.getCode() == KeyCode.DOWN) {
                    labelListView.handleUpDownKeys(true);
                    labelListView.showSelectedItemChange();
                    labelListView.setItems(null);
                    labelListView.setItems(labels);
                    e.consume();
                } else if (e.getCode() == KeyCode.UP) {
                    labelListView.handleUpDownKeys(false);
                    labelListView.showSelectedItemChange();
                    labelListView.setItems(null);
                    labelListView.setItems(labels);
                    e.consume();
                } else if (e.getCode() == KeyCode.TAB) {
                    labelListView.toggleSelectedItem();
                    e.consume();
                }
            }
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
        labels.forEach(label -> label.checkedProperty().addListener((observable, wasSelected, isSelected) -> {
            resultList.put(label.getName(), isSelected);
        }));
    }

}
