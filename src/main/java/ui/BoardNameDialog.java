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
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

public class BoardNameDialog extends Dialog<String> {
    
    private Preferences prefs;
    private TextField nameField;
    private Text prompt;
    private Button submitButton;
    private static Set<String> invalidNames;
    private ValidationSupport validationSupport = new ValidationSupport();
    
    private String previousText = "";
    private static final String DEFAULT_NAME = "New Board";
    private static final int BOARD_MAX_NAME_LENGTH = 10;
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
        addValidation();
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
        ButtonType submitButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        submitButton = (Button) getDialogPane().lookupButton(submitButtonType);
        
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
        
        prompt = new Text("New board name: ");
        
        nameField = new TextField(DEFAULT_NAME);
        nameField.setPrefWidth(300);
        Platform.runLater(() -> {
            nameField.requestFocus();
        });
        
        nameArea.getChildren().addAll(prompt, nameField);
        grid.add(nameArea, 0, 0);
        
        getDialogPane().setContent(grid);
    }
    
    private void addValidation() {
        validationSupport.registerValidator(nameField, (c, newVal) -> {
            boolean isEmpty = isBoardNameEmpty(nameField.getText().trim());
            return ValidationResult.fromErrorIf(nameField, ERROR_EMPTY_NAME, isEmpty);
        });
        
        validationSupport.registerValidator(nameField, (c, newVal) -> {
            boolean isDuplicate = isBoardNameDuplicate(nameField.getText().trim());
            return ValidationResult.fromErrorIf(nameField, ERROR_DUPLICATE_NAME, isDuplicate);
        });
        
        validationSupport.registerValidator(nameField, (c, newVal) -> {
            boolean isInvalid = isBoardNameInvalid(nameField.getText().trim());
            return ValidationResult.fromErrorIf(nameField, ERROR_INVALID_NAME, isInvalid);
        });
        
        validationSupport.registerValidator(nameField, (c, newVal) -> {
            boolean isInvalid = (nameField.getText().length() > BOARD_MAX_NAME_LENGTH);
            return ValidationResult.fromErrorIf
                    (nameField, String.format(ERROR_LONG_NAME, BOARD_MAX_NAME_LENGTH), isInvalid);
        });
    }
    
    private void addListener() {
        nameField.textProperty().addListener(c -> {
            if (nameField.getText().length() > BOARD_MAX_NAME_LENGTH) {
                nameField.setText(previousText);
            }
            previousText = nameField.getText();
            
            if (isBoardNameInvalid(nameField.getText().trim()) || isBoardNameEmpty(nameField.getText().trim())) {
                submitButton.setDisable(true);
            } else {
                submitButton.setDisable(false);
            }
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
