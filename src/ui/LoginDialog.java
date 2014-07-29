package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;
import util.DialogMessage;

public class LoginDialog extends Dialog<Boolean> {

	private TextField repoOwnerField;
	private TextField repoNameField;
	private TextField usernameField;
	private PasswordField passwordField;
	private NotificationPane notificationPane;
	private ColumnControl columns;
	private Button loginButton;
	private Label statusLabel;

	public LoginDialog(Stage parentStage, ColumnControl columns) {
		super(parentStage);
		this.columns = columns;
	}
	
	@Override
	protected void onClose(WindowEvent e) {
		completeResponse(false);
	}
	
	@Override
	protected Parent content() {

		setTitle("GitHub Login");
		setSize(320, 200);
		setStageStyle(StageStyle.UTILITY);
		
		notificationPane = new NotificationPane();
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Label repoNameLabel = new Label("Repository:");
		grid.add(repoNameLabel, 0, 0);

		repoOwnerField = new TextField("HubTurbo");
		grid.add(repoOwnerField, 1, 0);

		Label slash = new Label("/");
		grid.add(slash, 2, 0);

		repoNameField = new TextField("HubTurbo");
		grid.add(repoNameField, 3, 0);

		Label usernameLabel = new Label("Username:");
		grid.add(usernameLabel, 0, 1);

		usernameField = new TextField();
		grid.add(usernameField, 1, 1, 3, 1);

		Label passwordLabel = new Label("Password:");
		grid.add(passwordLabel, 0, 2);

		passwordField = new PasswordField();
		grid.add(passwordField, 1, 2, 3, 1);

		statusLabel = new Label();
		HBox.setHgrow(statusLabel, Priority.ALWAYS);
		grid.add(statusLabel, 0, 3);
		
		repoOwnerField.setMaxWidth(80);
		repoNameField.setMaxWidth(80);

		loginButton = new Button("Sign in");
		loginButton.setOnAction(this::login);

		repoOwnerField.setOnAction(this::login);
		repoNameField.setOnAction(this::login);
		usernameField.setOnAction(this::login);
		passwordField.setOnAction(this::login);

		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.BOTTOM_RIGHT);
		buttons.getChildren().add(loginButton);
		grid.add(buttons, 3, 3);
		
		notificationPane.setContent(grid);
		
		return notificationPane;
	}
	
	private void login(Event e) {
		
		// Resolve username and password
		
		String owner = repoOwnerField.getText();
		String repo = repoNameField.getText();
		String username = usernameField.getText();
		String password = passwordField.getText();
		
		if (username.isEmpty() && password.isEmpty()) {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader("credentials.txt"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (username.isEmpty()) {
						username = line;
					} else {
						password = line;
					}
				}
				System.out.println("Logged in using credentials.txt");
			} catch (Exception ex) {
				System.out.println("Failed to find or open credentials.txt");
			}
		}
		
		// Update UI

		enableElements(false);
		
		// Run blocking operations in the background
		
		StatusBar.displayMessage("Signing in at GitHub...");
    	boolean couldLogIn = ServiceManager.getInstance().login(username, password);

		Task<Boolean> task = new Task<Boolean>() {
		    @Override
		    protected Boolean call() throws Exception {
		    	StatusBar.displayMessage("Signed in; loading data...");
			    loadRepository(owner, repo);
		    	return true;
		    }
		};
		task.setOnSucceeded(wse -> {
			if (task.getValue()) {
				StatusBar.displayMessage("Issues loaded successfully! " + ServiceManager.getInstance().getRemainingRequests() + " requests remaining out of " + ServiceManager.getInstance().getRequestLimit() + ".");
				columns.resumeColumns();
				completeResponse(true);
				close();
			} else {
				handleError("Issues failed to load. Please try again.");
			}
		});
		task.setOnFailed(wse -> {
			handleError("An error occurred: " + task.getException());
		});
		
		if (couldLogIn) {
			DialogMessage.showProgressDialog(task, "Loading issues from " + owner + "/" + repo + "...");
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
		} else {
			handleError("Failed to sign in. Please try again.");
		}
		
	}

	private void handleError(String message) {
		Platform.runLater(()->{
			enableElements(true);
			StatusBar.displayMessage(message);
			DialogMessage.showWarningDialog("Warning", message);
		});
	}

	private void enableElements(boolean enable) {
		boolean disable = !enable;
		loginButton.setDisable(disable);
		repoOwnerField.setDisable(disable);
		repoNameField.setDisable(disable);
		usernameField.setDisable(disable);
		passwordField.setDisable(disable);
	}
	
	private void loadRepository(String owner, String repoName) throws IOException {
		ServiceManager.getInstance().setupRepository(owner, repoName);
		ServiceManager.getInstance().setupAndStartModelUpdate();
	}
}
