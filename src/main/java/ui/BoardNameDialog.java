package ui;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class BoardNameDialog extends Dialog<String> {
    
    private final Preferences prefs;
    private TextField nameField;
    private Text errorText;
    private Button submitButton;
    private static Set<String> invalidNames;
    
    private String previousText = "";
    private static final String DEFAULT_NAME = "New Board";
    private static final int BOARD_MAX_NAME_LENGTH = 100;
    private static final String ERROR_DUPLICATE_NAME = "Warning: duplicate name. Overwrite?";
    private static final String ERROR_EMPTY_NAME = "Error: empty name.";
    private static final String ERROR_LONG_NAME = "Error: board name cannot exceed %d letters.";
    private static final String ERROR_INVALID_NAME = "Error: invalid name.";

    public BoardNameDialog(Preferences prefs, Stage mainStage) {
        this.prefs = prefs;
        initializeDialog(mainStage);
        setupGrid();
        createButtons();
        addListener();
        initializeInvalidNames();
    }
    
    private static void initializeInvalidNames() {
        Set<String> invalids = new HashSet<String>();
        invalids.add("none"); // TODO possibly extend the set in the future
        invalidNames = Collections.unmodifiableSet(invalids);
    }
    
    private void initializeDialog(Stage mainStage) {
        initOwner(mainStage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Save board as");
    }
    
    private void createButtons() {
        ButtonType submitButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        submitButton = (Button) getDialogPane().lookupButton(submitButtonType);
        submitButton.setId("boardsavebutton");
        
        setResultConverter(submit -> {
            if (submit == submitButtonType) {
                return nameField.getText();
            }
            return null;
        });
        
    }
    
    protected void setupGrid() {
        GridPane grid = new GridPane();
        setupGridPane(grid);
        
        setTitle("Save board as");
        
        HBox nameArea = new HBox();

        Text prompt = new Text("New board name: ");
        
        nameField = new TextField(DEFAULT_NAME);
        nameField.setPrefWidth(300);
        Platform.runLater(() -> {
            nameField.requestFocus();
        });
        nameField.setId("boardnameinput");
        
        nameArea.getChildren().addAll(prompt, nameField);
        grid.add(nameArea, 0, 0);
        
        errorText = new Text("");
        grid.add(errorText, 0, 1);
        
        getDialogPane().setContent(grid);
    }
    
    private void addListener() {
        nameField.textProperty().addListener(c -> {
            String newName = nameField.getText().trim();
            if (nameField.getText().length() > BOARD_MAX_NAME_LENGTH) {
                nameField.setText(previousText);
                errorText.setText(ERROR_LONG_NAME);
                submitButton.setDisable(false);
            } else if (isBoardNameInvalid(newName)) {
                errorText.setText(ERROR_INVALID_NAME);
                submitButton.setDisable(true);
            } else if (isBoardNameEmpty(newName)) {
                errorText.setText(ERROR_EMPTY_NAME);
                submitButton.setDisable(true);
            } else if (isBoardNameDuplicate(newName)) {
                errorText.setText(ERROR_DUPLICATE_NAME);
                submitButton.setDisable(false);
            } else {
                errorText.setText("");
                submitButton.setDisable(false);
            }
            previousText = nameField.getText();
        });
    }
    
    private boolean isBoardNameInvalid(String newName) {
        return invalidNames.contains(newName);
    }
    
    private boolean isBoardNameEmpty(String newName) {
        return newName.isEmpty();
    }
    
    private boolean isBoardNameDuplicate(String newName) {
        return prefs.getAllBoards().containsKey(newName);
    }
    
    private static void setupGridPane(GridPane grid) {
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(25));
        grid.setPrefSize(360, 60);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }

}
