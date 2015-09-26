package ui;

import java.util.ArrayList;

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

public class BoardNameDialog extends Dialog<String> {
    
    Preferences prefs;
    TextField nameField;
    Text prompt;
    Text errorText;
    Button submitButton;

    private ArrayList<String> invalidNames = new ArrayList<>(); 
    
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
        
        invalidNames.add("none"); // TODO possibly extend the list in the future
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
        
        errorText = new Text("");
        grid.add(errorText, 0, 1);
        
        if (isBoardNameDuplicate(DEFAULT_NAME)) {
            errorText.setText(ERROR_DUPLICATE_NAME);
        }
        
        getDialogPane().setContent(grid);
    }
    
    public void addListener() {
        nameField.textProperty().addListener(c -> {
            String newName = nameField.getText().trim();
            if (nameField.getText().length() > BOARD_MAX_NAME_LENGTH) {
                nameField.setText(previousText);
                errorText.setText(String.format(ERROR_LONG_NAME, BOARD_MAX_NAME_LENGTH));
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
        if (newName.equals("")) {
            return true;
        }
        return false;
    }
    
    private boolean isBoardNameDuplicate(String newName) {
        if (prefs.getAllBoards().containsKey(newName)) {
            return true;
        }
        return false;
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
