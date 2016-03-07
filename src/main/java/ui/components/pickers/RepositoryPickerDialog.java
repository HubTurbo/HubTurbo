package ui.components.pickers;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Utility;

import java.util.Set;

public class RepositoryPickerDialog extends Dialog<String> {

    private static final String REPO_PICKER_TITLE = "Pick another repository";

    private ComboBox<String> comboBox;
    private RepositoryPickerState state;

    public RepositoryPickerDialog(Set<String> storedRepos, String defaultRepo, Stage stage) {
        initUi(stage, storedRepos, defaultRepo);
    }

    private void initUi(Stage stage, Set<String> storedRepos, String defaultRepo) {
        state = new RepositoryPickerState(storedRepos);
        state.updateQuery(defaultRepo);

        initialiseDialog(stage);
        createComboBox(storedRepos, defaultRepo);
        createButtons();

        getDialogPane().setContent(comboBox);
        comboBox.requestFocus();
    }

    private void initialiseDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(REPO_PICKER_TITLE);
    }

    private void createComboBox(Set<String> storedRepos, String defaultRepo) {
        comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setValue(defaultRepo);
        comboBox.getItems().addAll(storedRepos);
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            String repoId = Utility.removeAllWhitespace(newValue);
            if (!repoId.equals(newValue)) {
                comboBox.setValue(repoId);
                return;
            }
        });
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String chosenRepo = comboBox.getValue();
                assert chosenRepo != null;
                if (Utility.isWellFormedRepoId(chosenRepo)) {
                    return chosenRepo;
                }
            }
            return null;
        });
    }

}
