package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

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

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 200, 10, 0));
        textField = new TextField();
        textField.setMaxWidth(180);
        textField.setMinWidth(180);
        textField.setPrefWidth(180);
        vBox.getChildren().add(textField);
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
}
