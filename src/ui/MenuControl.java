package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.eclipse.egit.github.core.client.GitHubRequest;

import util.GitHubClientExtended;
import util.ModelUpdater;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class MenuControl extends MenuBar {

	private String repoOwner, repoName;
	private Stage mainStage;
	private Model model;
	private ColumnControl columns;
	private UI ui;
	private GitHubClientExtended client;
	private ModelUpdater modelUpdater;

	public MenuControl(Stage mainStage, Model model, GitHubClientExtended client, ColumnControl columns, UI ui) {
		this.mainStage = mainStage;
		this.model = model;
		this.client = client;
		this.ui = ui;
		this.columns = columns;
		
		createMenuItems(mainStage, model, columns);
		disableMenuItemsRequiringLogin();
	}
	
	MenuItem manageMilestonesMenuItem;
	MenuItem newIssueMenuItem;
	MenuItem manageLabelsMenuItem;
	MenuItem refreshMenuItem;
	
	private void disableMenuItemsRequiringLogin() {
		manageMilestonesMenuItem.setDisable(true);
		newIssueMenuItem.setDisable(true);
		manageLabelsMenuItem.setDisable(true);
		refreshMenuItem.setDisable(true);
	}
	
	private void enableMenuItemsRequiringLogin() {
		manageMilestonesMenuItem.setDisable(false);
		newIssueMenuItem.setDisable(false);
		manageLabelsMenuItem.setDisable(false);
		refreshMenuItem.setDisable(false);
	}

	private void createMenuItems(Stage mainStage, Model model, ColumnControl columns) {
		Menu projects = new Menu("Projects");
		MenuItem login = new MenuItem("Login");
		initLoginForm(login);
		projects.getItems().addAll(login);

		Menu milestones = new Menu("Milestones");
		manageMilestonesMenuItem = new MenuItem("Manage milestones...");
		milestones.getItems().addAll(manageMilestonesMenuItem);
		manageMilestonesMenuItem.setOnAction(e -> {
			(new ManageMilestonesDialog(mainStage, model)).show().thenApply(
					response -> {
						return true;
					});
		});

		Menu issues = new Menu("Issues");
		newIssueMenuItem = new MenuItem("New Issue");
		newIssueMenuItem.setOnAction(e -> {
			TurboIssue issue = new TurboIssue("New issue", "");
			(new IssueDialog(mainStage, model, issue)).show().thenApply(
					response -> {
						if (response.equals("ok")) {
							model.createIssue(issue);
						}
						// Required for some reason
						columns.refresh();
						return true;
					});
		});
		issues.getItems().addAll(newIssueMenuItem);

		Menu labels = new Menu("Labels");
		manageLabelsMenuItem = new MenuItem("Manage labels...");
		manageLabelsMenuItem.setOnAction(e -> {
			(new ManageLabelsDialog(mainStage, model)).show().thenApply(
					response -> {
						return true;
					});
		});

		labels.getItems().addAll(manageLabelsMenuItem);

		Menu view = new Menu("View");

		refreshMenuItem = new MenuItem("Refresh");
		refreshMenuItem.setOnAction((e) -> {
			handleRefresh();
		});
		refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));

		Menu columnsMenu = new Menu("Change number of columns....");
		view.getItems().addAll(refreshMenuItem, columnsMenu);

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

		getMenus().addAll(projects, milestones, issues, labels, view);
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
						System.out.println("Failed to find or open credentials.txt");
					}
				}

				login(username, password);
				repoOwner = repoOwnerField.getText();
				repoName = repoNameField.getText();

				loadDataIntoModel();
				columns.loadIssues();
				if (modelUpdater != null) {
					modelUpdater.stopModelUpdate();
				}
				setupModelUpdate();

				mainStage.setTitle("HubTurbo (" + client.getRemainingRequests() + " requests remaining out of " + client.getRequestLimit() + ")");
				enableMenuItemsRequiringLogin();
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
	

	private void handleRefresh(){
		modelUpdater.stopModelUpdate();
		modelUpdater.startModelUpdate();
	}
	
	private void loadDataIntoModel() {
		model.setRepoId(repoOwner, repoName);
	}

	private void setupModelUpdate() {
		modelUpdater = new ModelUpdater(client, model);
		ui.setModelUpdater(modelUpdater);
		modelUpdater.startModelUpdate();
	}

	// private void setUpHotkeys(Scene scene) {
	// scene.getAccelerators().put(
	// new KeyCodeCombination(KeyCode.DIGIT1,
	// KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
	// (Runnable) () -> changePanelCount(1));
	// }

	private boolean login(String userId, String password) {
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
