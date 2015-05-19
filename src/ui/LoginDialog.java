package ui;

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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import ui.components.Dialog;
import ui.components.HTStatusBar;
import ui.issuecolumn.ColumnControl;
import util.DialogMessage;
import util.HTLog;
import util.PlatformEx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

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

	private static final Logger logger = LogManager.getLogger(LoginDialog.class.getName());
	private final UI ui;

	private TextField repoOwnerField;
	private TextField repoNameField;
	private TextField usernameField;
	private PasswordField passwordField;
	private ColumnControl columns;
	private Button loginButton;

	public LoginDialog(UI ui, Stage parentStage, ColumnControl columns) {
		super(parentStage);
		this.ui = ui;
		this.columns = columns;
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

		repoOwnerField = new TextField(FIELD_DEFAULT_REPO_OWNER);
		repoOwnerField.setPrefWidth(140);
		grid.add(repoOwnerField, 2, 0);

		Label slash = new Label("/");
		grid.add(slash, 3, 0);

		repoNameField = new TextField(FIELD_DEFAULT_REPO_NAME);
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

		populateSavedFields();

		repoOwnerField.setOnAction(this::login);
		repoNameField.setOnAction(this::login);
		usernameField.setOnAction(this::login);
		passwordField.setOnAction(this::login);

		HBox buttons = new HBox(10);
		buttons.setAlignment(Pos.BOTTOM_RIGHT);
		loginButton = new Button(BUTTON_SIGN_IN);
		loginButton.setOnAction(this::login);
		buttons.getChildren().add(loginButton);
		grid.add(buttons, 4, 3);

		return grid;
	}

	/**
	 * Fills in fields which have values at this point.
	 */
	private void populateSavedFields() {
//		Optional<RepositoryId> lastViewed = DataManager.getInstance().getLastViewedRepository();
//		if (lastViewed.isPresent()) {
//			repoOwnerField.setText(lastViewed.get().getOwner());
//			repoNameField.setText(lastViewed.get().getName());
//		}
//
//		String lastLoginName = DataManager.getInstance().getLastLoginUsername();
//		if (!lastLoginName.isEmpty()) {
//			usernameField.setText(lastLoginName);
//		}
//
//		String lastLoginPassword = DataManager.getInstance().getLastLoginPassword();
//		if (!lastLoginPassword.isEmpty()) {
//			passwordField.setText(lastLoginPassword);
//		}
//		// Change focus depending on what fields are present
//		Platform.runLater(() -> {
//			if(!lastLoginPassword.isEmpty()){
//				login(null);
//			} else if (!lastLoginName.isEmpty()) {
//				passwordField.requestFocus();
//			} else if (lastViewed.isPresent()) {
//				usernameField.requestFocus();
//			}
//		});
	}

	/**
	 * Configures the central grid pane before it's used.
	 * @param grid
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
	 * @param grid the grid pane to apply column constraints to
	 * @param values an array of integer values which should add up to 100
	 */
	private static void applyColumnConstraints(GridPane grid, int... values) {

		// The values should sum up to 100%
		int sum = 0;
		for (int i=0; i<values.length; i++) {
			sum += values[i];
		}
		assert sum == 100 : "Column constraints should sum up to 100%!";

		// Apply constraints to grid
		ColumnConstraints column;
		for (int i=0; i<values.length; i++) {
			column = new ColumnConstraints();
			column.setPercentWidth(values[i]);
			grid.getColumnConstraints().add(column);
		}
	}

	private void login(Event e) {

		// Resolve username and password

		String owner = repoOwnerField.getText();
		String repo = repoNameField.getText();
		String username = usernameField.getText();
		String password = passwordField.getText();

		// If either field is empty, try to load credentials.txt
		if (username.isEmpty() || password.isEmpty()) {
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
				logger.info("Logged in using credentials.txt");
			} catch (Exception ex) {
				logger.info("Failed to find or open credentials.txt");
			}
		}

		// Update UI

		enableElements(false);

		// Run blocking operations in the background

		HTStatusBar.displayMessage("Signing in at GitHub...");
		boolean couldLogIn = false;
		try {
			couldLogIn = ui.logic.login(username, password).get();
		} catch (InterruptedException | ExecutionException e1) {
			HTLog.error(logger, e1);
		}

		Task<Boolean> task = new Task<Boolean>() {
		    @Override
		    protected Boolean call() throws Exception {

			    HTStatusBar.displayMessage("Signed in; loading data...");
			    updateProgress(0, 1);
			    updateMessage("Loading data from " + owner + "/" + repo + "...");
			    boolean loadSuccess = loadRepository(owner, repo, (message, progress) -> {
				    updateProgress(progress * 100, 100);
				    updateMessage(message);
			    });
                PlatformEx.runAndWait(columns::restoreColumns);
		    	return loadSuccess;
		    }
		};

		task.setOnSucceeded(wse -> {
			if (task.getValue()) {
//				HTStatusBar.displayMessage(String.format("%s loaded successfully! (%s)",
//					ServiceManager.getInstance().getRepoId().generateId(),
//					ServiceManager.getInstance().getRemainingRequestsDesc()));
//				logger.info("Remaining requests: " +
//					ServiceManager.getInstance().getRemainingRequestsDesc());
				completeResponse(true);
				close();
			} else {
				handleError("Issues failed to load. Please try again.");
			}
		});
		task.setOnFailed(wse -> {
			Throwable thrown = task.getException();
			logger.error(thrown.getLocalizedMessage(), thrown);
			handleError("An error occurred: " + task.getException());
		});

		if (couldLogIn) {

			// Save login details only on successful login
//			DataManager.getInstance().setLastLoginUsername(username);
//			DataManager.getInstance().setLastLoginPassword(password);

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
			HTStatusBar.displayMessage(message);
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

	private boolean loadRepository(String owner, String repoName,
	                               BiConsumer<String, Float> taskUpdate) throws IOException {
//		boolean loaded = ServiceManager.getInstance().setupRepository(owner, repoName, taskUpdate);
//		ServiceManager.getInstance().startModelUpdate();
//		IRepositoryIdProvider currRepo = ServiceManager.getInstance().getRepoId();
//		if (currRepo != null) {
//			String repoId = currRepo.generateId();
//			DataManager.getInstance().addToLastViewedRepositories(repoId);
//		}
//		return loaded;
		return true;
	}
}
