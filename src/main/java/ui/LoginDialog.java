package ui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import ui.components.Dialog;
import util.DialogMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginDialog extends Dialog<Boolean> {

    private static final String DIALOG_TITLE = "GitHub Login";
    private static final int DIALOG_HEIGHT = 200;
    private static final int DIALOG_WIDTH = 570;

    private static final String LABEL_REPO = "Repository:";
    private static final String LABEL_GITHUB = "github.com /";
    private static final String FIELD_DEFAULT_REPO_OWNER = "<owner/organization>";
    private static final String FIELD_DEFAULT_REPO_NAME = "<repository>";
    private static final String PASSWORD_LABEL = "Password:";
    private static final String USERNAME_LABEL = "Username:";
    private static final String BUTTON_SIGN_IN = "Sign in";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final UI ui;

    private TextField repoOwnerField;
    private TextField repoNameField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginDialog(UI ui, Stage parentStage, String owner, String repo, String username, String password) {
        super(parentStage);
        this.ui = ui;
        repoOwnerField.setText(owner);
        repoNameField.setText(repo);
        usernameField.setText(username);
        passwordField.setText(password);
        changeFocus();
        if (repoOwnerField.getText().isEmpty()) {
            repoOwnerField.setText(FIELD_DEFAULT_REPO_OWNER);
        }
        if (repoNameField.getText().isEmpty()) {
            repoNameField.setText(FIELD_DEFAULT_REPO_NAME);
        }
    }

    private void login() {
        Platform.runLater(() -> enableUI(false));
        CompletableFuture.supplyAsync(() -> {
            return ui.logic.loginController.attemptLogin(repoOwnerField.getText(),
                                                         repoNameField.getText(),
                                                         usernameField.getText(),
                                                         passwordField.getText());
        }, executor).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    close();
                    completeResponse(true);
                });
            } else {
                Platform.runLater(() -> {
                    DialogMessage.showErrorDialog("Login Error", "Failed to sign in. Please try again.");
                    enableUI(true);
                });
            }
        });
    }

    @Override
    protected void onClose(WindowEvent e) {
        completeResponse(false);
    }

    @Override
    protected Parent content() {
        setTitle(DIALOG_TITLE);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setStageStyle(StageStyle.UTILITY);

        GridPane grid = new GridPane();
        setupGridPane(grid);

        Label repoLabel = new Label(LABEL_REPO);
        grid.add(repoLabel, 0, 0);

        Label githubLabel = new Label(LABEL_GITHUB);
        grid.add(githubLabel, 1, 0);

        repoOwnerField = new TextField();
        repoOwnerField.setId(IdGenerator.getLoginDialogOwnerFieldId());
        repoOwnerField.setPrefWidth(140);
        grid.add(repoOwnerField, 2, 0);

        Label slash = new Label("/");
        grid.add(slash, 3, 0);

        repoNameField = new TextField();
        repoNameField.setPrefWidth(250);
        grid.add(repoNameField, 4, 0);

        Label usernameLabel = new Label(USERNAME_LABEL);
        grid.add(usernameLabel, 0, 1);

        usernameField = new TextField();
        grid.add(usernameField, 1, 1, 4, 1);

        Label passwordLabel = new Label(PASSWORD_LABEL);
        grid.add(passwordLabel, 0, 2);

        passwordField = new PasswordField();
        grid.add(passwordField, 1, 2, 4, 1);

        repoOwnerField.setOnAction(e -> login());
        repoNameField.setOnAction(e -> login());
        usernameField.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);
        loginButton = new Button(BUTTON_SIGN_IN);
        loginButton.setOnAction(e -> login());
        buttons.getChildren().add(loginButton);
        grid.add(buttons, 4, 3);

        return grid;
    }

    private void changeFocus() {
        // Change focus depending on what fields are present
        if (repoOwnerField.getText().isEmpty()) {
            Platform.runLater(repoOwnerField::requestFocus);
        } else if (repoNameField.getText().isEmpty()) {
            Platform.runLater(repoNameField::requestFocus);
        } else if (usernameField.getText().isEmpty()) {
            Platform.runLater(usernameField::requestFocus);
        } else {
            Platform.runLater(passwordField::requestFocus);
        }
    }

    /**
     * Configures the central grid pane before it's used.
     */
    private static void setupGridPane(GridPane grid) {
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(7);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        grid.setPrefSize(390, 100);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        applyColumnConstraints(grid, 20, 16, 33, 2, 29);
    }

    /**
     * A variadic function that applies percentage-width column constraints to
     * the given grid pane.
     *
     * @param grid   the grid pane to apply column constraints to
     * @param values an array of integer values which should add up to 100
     */
    private static void applyColumnConstraints(GridPane grid, int... values) {
        // The values should sum up to 100%
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        assert sum == 100 : "Column constraints should sum up to 100%!";

        // Apply constraints to grid
        ColumnConstraints column;
        for (int value : values) {
            column = new ColumnConstraints();
            column.setPercentWidth(value);
            grid.getColumnConstraints().add(column);
        }
    }

    private void enableUI(boolean enable) {
        boolean disable = !enable;
        loginButton.setDisable(disable);
        repoOwnerField.setDisable(disable);
        repoNameField.setDisable(disable);
        usernameField.setDisable(disable);
        passwordField.setDisable(disable);
    }

}
