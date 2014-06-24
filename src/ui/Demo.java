package ui;

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
import model.ModelFacade;

public class Demo extends Application {

	public static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_BORDERS_FADED = "-fx-border-color: #B2B1AE; -fx-border-width: 1px; -fx-border-radius: 3;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_FADED = "-fx-text-fill: #B2B1AE;";

	private Stage mainStage;
	private ColumnControl columns;
	private ModelFacade logic = new ModelFacade();

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		mainStage = stage;

		Scene scene = new Scene(createRoot(), 800, 600);
//		setUpHotkeys(scene);

		stage.setTitle("HubTurbo");
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.setScene(scene);
		stage.show();
	}
	
	// Node definitions
		
	private Parent createRoot() {

		columns = new ColumnControl(mainStage, logic);
		
		BorderPane root = new BorderPane();
		root.setCenter(columns);
		root.setTop(createMenuBar());

		return root;
	}

	private void initLoginForm(MenuItem login) {
		login.setOnAction((e) -> {
			Stage dialogStage = new Stage();
			dialogStage.setTitle("GitHub Login");

			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			Label repoName = new Label("Repository:");
			grid.add(repoName, 0, 0);

			TextField repoOwnerField = new TextField("dariusf");
			grid.add(repoOwnerField, 1, 0);

			Label slash = new Label("/");
			grid.add(slash, 2, 0);

			TextField repoNameField = new TextField("issues");
			grid.add(repoNameField, 3, 0);

			Label username = new Label("Username:");
			grid.add(username, 0, 1);

			TextField usernameField = new TextField();
			grid.add(usernameField, 1, 1, 3, 1);

			Label password = new Label("Password:");
			grid.add(password, 0, 2);

			PasswordField passwordField = new PasswordField();
			grid.add(passwordField, 1, 2, 3, 1);

			repoOwnerField.setMaxWidth(80);
			repoNameField.setMaxWidth(80);

			Button loginButton = new Button("Sign in");
			loginButton.setOnAction((ev) -> {
				logic.login(usernameField.getText(), passwordField.getText());
				logic.setRepository(repoOwnerField.getText(),
						repoNameField.getText());
				dialogStage.hide();
				
				columns.loadIssues();
			});

			HBox buttons = new HBox(10);
			buttons.setAlignment(Pos.BOTTOM_RIGHT);
			buttons.getChildren().add(loginButton);
			grid.add(buttons, 3, 3);

			Scene scene = new Scene(grid, 320, 200);
			dialogStage.setScene(scene);

			dialogStage.initOwner(mainStage);
			dialogStage.initModality(Modality.APPLICATION_MODAL);

			dialogStage.setX(mainStage.getX());
			dialogStage.setY(mainStage.getY());

			dialogStage.show();
		});
	}

//	private void setUpHotkeys(Scene scene) {
//		scene.getAccelerators().put(
//				new KeyCodeCombination(KeyCode.DIGIT1,
//						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
//				(Runnable) () -> changePanelCount(1));
//	}

	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		Menu projects = new Menu("Projects");
		MenuItem login = new MenuItem("Login");
		initLoginForm(login);
		MenuItem config = new MenuItem("Configuration");
		projects.getItems().addAll(login, config);

		Menu milestones = new Menu("Milestones");
		MenuItem newMilestone = new MenuItem("New Milestone");
		milestones.getItems().addAll(newMilestone);

		Menu issues = new Menu("Issues");
		MenuItem newIssue = new MenuItem("New Issue");
		issues.getItems().addAll(newIssue);

		Menu labels = new Menu("Labels");
		MenuItem newLabel = new MenuItem("New Label");
		labels.getItems().addAll(newLabel);

		Menu view = new Menu("View");
		Menu columnsMenu = new Menu("Change number of columns....");
		view.getItems().addAll(columnsMenu);

		final ToggleGroup numberOfCols = new ToggleGroup();
		for (int i = 1; i <= 9; i++) {
			RadioMenuItem item = new RadioMenuItem(Integer.toString(i));
			item.setToggleGroup(numberOfCols);
			columnsMenu.getItems().add(item);
			
			final int j = i;
			item.setOnAction((e) -> columns.setColumnCount(j));
			item.setAccelerator(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + Integer.toString(j)), KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));

			if (i == 1)
				item.setSelected(true);
		}

		menuBar.getMenus().addAll(projects, milestones, issues, labels, view);
		return menuBar;
	}
}
