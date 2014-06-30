package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.egit.github.core.client.GitHubRequest;

import util.GitHubClientExtended;
import util.ModelUpdater;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class UI extends Application {

	public static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_BORDERS_FADED = "-fx-border-color: #B2B1AE; -fx-border-width: 1px; -fx-border-radius: 3;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_FADED = "-fx-text-fill: #B2B1AE;";

	private Stage mainStage;
	private ColumnControl columns;
	private Model model;
	private GitHubClientExtended client;
	private String repoOwner, repoName;
	private ModelUpdater modelUpdater;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {

		client = new GitHubClientExtended();
		model = new Model(client);

		mainStage = stage;

		Scene scene = new Scene(createRoot(), 800, 600);
		// setUpHotkeys(scene);

		setupStage(stage, scene);
	}

	private void setupStage(Stage stage, Scene scene) {
		stage.setTitle("HubTurbo");
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(e -> {
			if (modelUpdater != null) {
				modelUpdater.stopModelUpdate();
			}
		});
	}

	// Node definitions

	private Parent createRoot() {

		columns = new ColumnControl(mainStage, model);

		BorderPane root = new BorderPane();
		root.setCenter(columns);
		root.setTop(createMenuBar());

		return root;
	}

	private void initLoginForm(MenuItem login) {
		login.setOnAction((e) -> {
			Stage stage = new Stage();
			stage.setTitle("GitHub Login");

			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			Label repoNameLabel = new Label("Repository:");
			grid.add(repoNameLabel, 0, 0);

			TextField repoOwnerField = new TextField("HubTurbo");
			grid.add(repoOwnerField, 1, 0);

			Label slash = new Label("/");
			grid.add(slash, 2, 0);

			TextField repoNameField = new TextField("HubTurbo");
			grid.add(repoNameField, 3, 0);

			Label usernameLabel = new Label("Username:");
			grid.add(usernameLabel, 0, 1);

			TextField usernameField = new TextField();
			grid.add(usernameField, 1, 1, 3, 1);

			Label passwordLabel = new Label("Password:");
			grid.add(passwordLabel, 0, 2);

			PasswordField passwordField = new PasswordField();
			grid.add(passwordField, 1, 2, 3, 1);

			repoOwnerField.setMaxWidth(80);
			repoNameField.setMaxWidth(80);

			Button loginButton = new Button("Sign in");
			loginButton.setOnAction((ev) -> {
				String username = usernameField.getText();
				String password = passwordField.getText();
				if (username.isEmpty() && password.isEmpty()) {
					BufferedReader reader;
					try {
						reader = new BufferedReader(new FileReader(
								"credentials.txt"));
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (username.isEmpty())
								username = line;
							else
								password = line;
						}
						System.out.println("Logged in using credentials.txt");
					} catch (Exception e1) {
						System.out
								.println("Failed to find or open credentials.txt");
					}
				}

				login(username, password);
				repoOwner = repoOwnerField.getText();
				repoName = repoNameField.getText();

				loadDataIntoModel();
				columns.loadIssues();
				setupModelUpdate();

				mainStage.setTitle("HubTurbo (" + client.getRemainingRequests()
						+ " requests remaining out of "
						+ client.getRequestLimit() + ")");
				stage.hide();
			});

			HBox buttons = new HBox(10);
			buttons.setAlignment(Pos.BOTTOM_RIGHT);
			buttons.getChildren().add(loginButton);
			grid.add(buttons, 3, 3);

			Scene scene = new Scene(grid, 320, 200);
			stage.setScene(scene);

			stage.initOwner(mainStage);
			stage.initModality(Modality.APPLICATION_MODAL);

			stage.setX(mainStage.getX());
			stage.setY(mainStage.getY());

			stage.show();
		});
	}

	private void loadDataIntoModel() {
		model.setRepoId(repoOwner, repoName);
	}

	private void setupModelUpdate() {
		modelUpdater = new ModelUpdater(client, model);
		modelUpdater.startModelUpdate();
	}

	// private void setUpHotkeys(Scene scene) {
	// scene.getAccelerators().put(
	// new KeyCodeCombination(KeyCode.DIGIT1,
	// KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
	// (Runnable) () -> changePanelCount(1));
	// }

	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		Menu projects = new Menu("Projects");
		MenuItem login = new MenuItem("Login");
		initLoginForm(login);
		projects.getItems().addAll(login);

		Menu milestones = new Menu("Milestones");
		MenuItem manageMilestones = new MenuItem("Manage milestones...");
		milestones.getItems().addAll(manageMilestones);
		manageMilestones.setOnAction(e -> {
			(new ManageMilestonesDialog(mainStage, model)).show().thenApply(
					response -> {
						return true;
					});
		});

		Menu issues = new Menu("Issues");
		MenuItem newIssue = new MenuItem("New Issue");
		newIssue.setOnAction(e -> {
			TurboIssue issue = model.createIssue(new TurboIssue("New issue", ""));
			TurboIssue copy = new TurboIssue(issue);
			(new IssueDialog(mainStage, model, issue)).show().thenApply(
					response -> {
						if (response.equals("ok")) {
							model.updateIssue(copy, issue);
						} else if (response.equals("cancel")) {
							issue.copyValues(copy);
						}
						// Required for some reason
						columns.refresh();
						return true;
					});
		});
		issues.getItems().addAll(newIssue);

		Menu labels = new Menu("Labels");
		MenuItem manageLabels = new MenuItem("Manage labels...");
		manageLabels.setOnAction(e -> {
			(new ManageLabelsDialog(mainStage, model)).show().thenApply(
					response -> {
						return true;
					});
		});

		labels.getItems().addAll(manageLabels);

		Menu view = new Menu("View");

		MenuItem refresh = new MenuItem("Refresh");
		refresh.setOnAction((e) -> {
			loadDataIntoModel();
			columns.refresh(); // In case
		});
		refresh.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		Menu columnsMenu = new Menu("Change number of columns....");
		view.getItems().addAll(refresh, columnsMenu);

		final ToggleGroup numberOfCols = new ToggleGroup();
		for (int i = 1; i <= 9; i++) {
			RadioMenuItem item = new RadioMenuItem(Integer.toString(i));
			item.setToggleGroup(numberOfCols);
			columnsMenu.getItems().add(item);

			final int j = i;
			item.setOnAction((e) -> columns.setColumnCount(j));
			item.setAccelerator(new KeyCodeCombination(KeyCode.valueOf("DIGIT"
					+ Integer.toString(j)), KeyCombination.SHIFT_DOWN,
					KeyCombination.ALT_DOWN));

			if (i == 1)
				item.setSelected(true);
		}

		menuBar.getMenus().addAll(projects, milestones, issues, labels, view);
		return menuBar;
	}

	public boolean login(String userId, String password) {
		client.setCredentials(userId, password);
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			client.get(request);
		} catch (IOException e) {
			// Login failed
			return false;
		}
		return true;
	}

}
