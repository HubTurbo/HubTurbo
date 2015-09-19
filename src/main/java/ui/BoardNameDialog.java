package ui;

import java.util.Optional;

import ui.MenuControl;
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
    
    MenuControl menuControl;
    Preferences prefs;
    TextField nameField;
    Text prompt;
    Text errorText;
    Button submitButton;
    GridPane grid;
    
    private String previousText = "";
    private static final String DEFAULT_NAME = "New Board";
    private static final int BOARD_MAX_NAME_LENGTH = 20;
    private static final String ERROR_DUPLICATE_NAME = "Warning: duplicate name. Overwrite?";
    private static final String ERROR_EMPTY_NAME = "Error: empty name.";

    public BoardNameDialog(Preferences prefs, Stage mainStage) {
        this.prefs = prefs; // for checking against existing boards
        initializeDialog(mainStage);
        setupGrid();
        createButtons();
        addListener();
    }
    
    private void initializeDialog(Stage mainStage) {
        initOwner(mainStage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Save board as");
    }
    
    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        setResultConverter(submit -> {
            if (submit == confirmButtonType) {
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
        
        nameArea.getChildren().addAll(prompt, nameField);
        grid.add(nameArea, 0, 0);
        
        errorText = new Text("");
        grid.add(errorText, 0, 1);
        
        getDialogPane().setContent(grid);
    }
    
    public void addListener() {
        nameField.textProperty().addListener(c -> {
            if (nameField.getText().length() > BOARD_MAX_NAME_LENGTH) {
                nameField.setText(previousText);
            }
            previousText = nameField.getText();
        });
        
    }
    
    private boolean isBoardNameEmpty(String newName) {
        if (newName.equals("")) {
            return false;
        }
        return true;
    }
    
    private boolean isBoardNameDuplicate(String newName) {
        if (prefs.getAllBoards().containsKey(newName)) {
            return false;
        }
        return true;
    }
    
    private static void setupGridPane(GridPane grid) {
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(7);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        grid.setPrefSize(360, 60);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }

}
