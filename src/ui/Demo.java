package ui;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Application;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import logic.LogicFacade;
import logic.TurboIssue;
import logic.TurboLabel;

public class Demo extends Application {

	private static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";

	private Stage mainStage;
	private HBox columns;
	private LogicFacade logic = new LogicFacade();

	private final ArrayList<BorderPane> items = new ArrayList<>();

	private BorderPane createItem(String text3) {
		BorderPane item = new BorderPane();
		items.add(item);

		final int index = items.size() - 1;
		Text text = new Text(text3);
		Text text2 = new Text("" + index);

		item.setLeft(text2);
		item.setCenter(text);
		final String style = STYLE_BORDERS;
		item.setStyle(style);

		item.setOnDragDetected((MouseEvent event) -> {
			Dragboard db = item.startDragAndDrop(TransferMode.ANY);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragSource.PANEL_MILESTONES);
			dd.index = index;
			content.putString(dd.serialize());

			db.setContent(content);

			event.consume();
		});

		item.setOnDragDone((DragEvent event) -> {
			if (event.getTransferMode() == TransferMode.MOVE) {
			}

			event.consume();
		});

		item.setOnDragOver((DragEvent event) -> {
			if (event.getGestureSource() != item
					&& event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}

			event.consume();
		});

		item.setOnDragEntered((DragEvent event) -> {
			if (event.getGestureSource() != item
					&& event.getDragboard().hasString()) {
				item.setStyle(STYLE_YELLOW_BORDERS);
			}

			event.consume();
		});

		item.setOnDragExited((DragEvent event) -> {
			item.setStyle(style);

			event.consume();
		});

		item.setOnDragDropped((DragEvent event) -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;

				DragData dd = DragData.deserialize(db.getString());

				if (dd.source == DragSource.PANEL_MILESTONES) {
					text.setText(text.getText() + "\n" + "added item "
							+ dd.index);
				} else if (dd.source == DragSource.TREE_ISSUES) {
					text.setText(text.getText() + "\n" + "added issue "
							+ dd.text);
				} else if (dd.source == DragSource.TREE_LABELS) {
					text.setText(text.getText() + "\n" + "added label "
							+ dd.text);
				} else if (dd.source == DragSource.TREE_CONTRIBUTORS) {
					text.setText(text.getText() + "\n" + "added contributors "
							+ dd.text);
				}
			}
			event.setDropCompleted(success);

			event.consume();
		});

		return item;
	}

	boolean done = false;

	private IssuePanel createIssuePanel() {
		IssuePanel issuePanel = new IssuePanel(mainStage);
		// col.setPadding(new Insets(15, 12, 15, 12));
		// col.setSpacing(10);
		issuePanel.setPrefWidth(400);
		HBox.setHgrow(issuePanel, Priority.ALWAYS);
		issuePanel.setStyle(STYLE_BORDERS);
		// issuePanel.setAlignment(Pos.TOP_CENTER);

		return issuePanel;
	}

	private Parent createTreeView() {
		final TreeItem<String> treeRoot = new TreeItem<>("root");

		TreeItem<String> milestones = new TreeItem<>("Milestones");

		TreeItem<String> issues = new TreeItem<>("Issues");
		TreeItem<String> contributors = new TreeItem<>("Contributors");
		TreeItem<String> labels = new TreeItem<>("Labels");

		treeRoot.getChildren().addAll(
				Arrays.asList(milestones, issues, contributors, labels));

		milestones.getChildren().addAll(
				Arrays.asList(new TreeItem<String>("v1.0.1"),
						new TreeItem<String>("v1.0.2")));

		issues.getChildren().addAll(
				Arrays.asList(new TreeItem<String>("#2 Main page won't load"),
						new TreeItem<String>("#45 Input validation fails")));

		contributors.getChildren().addAll(
				Arrays.asList(new TreeItem<String>("Ben"),
						new TreeItem<String>("Jerry")));

		labels.getChildren().addAll(
				Arrays.asList(new TreeItem<String>("Status"),
						new TreeItem<String>("Urgency")));
		labels.getChildren()
				.get(0)
				.getChildren()
				.addAll(Arrays.asList(new TreeItem<String>("Accepted"),
						new TreeItem<String>("Fixing")));
		labels.getChildren()
				.get(1)
				.getChildren()
				.addAll(Arrays.asList(new TreeItem<String>("Urgent"),
						new TreeItem<String>("Low")));

		final TreeView<String> treeView = new TreeView<>();
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		treeRoot.setExpanded(true);
		treeView.setPrefWidth(180);

		treeRoot.getChildren().stream().forEach((child) -> {
			child.setExpanded(true);
			child.getChildren().stream().forEach((TreeItem<String> child1) -> {
				child1.setExpanded(true);
			});
		});

		treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			@Override
			public TreeCell<String> call(TreeView<String> stringTreeView) {
				return new CustomTreeCell<String>(mainStage);
			}
		});

		return treeView;

	}

	private void changePanelCount(int to) {

		// // TODO the panels aren't ordered in insertion order? watch out for
		// that
		// ObservableList<Node> panels = columns.getChildren();
		// int panelSize = panels.size();
		//
		// if (panelSize == to) {
		// return;
		// }
		//
		// if (panelSize < to) {
		// for (int i = 0; i < to - panelSize; i++) {
		// panels.add(createIssuePanel());
		// }
		// } else { // panels.size() > to
		// int numberToRemove = panels.size() - to;
		// panels.remove(panels.size() - 1 - numberToRemove, panels.size() - 1);
		// }

		// Tests

		col1.filter(new Filter().withTitle("one")
				.exceptUnderMilestone("v0.0.1").or().withTitle("akjshdkj"));
		// test.setTitle("data binding demo");
	}

	private void setUpHotkeys(Scene scene) {
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT1,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(1));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT2,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(2));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT3,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(3));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT4,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(4));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT5,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(5));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT6,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(6));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT7,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(7));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT8,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(8));
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.DIGIT9,
						KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN),
				(Runnable) () -> changePanelCount(9));
	}

	TurboIssue test;
	IssuePanel col1;

	private Parent createRoot() {
		MenuBar menuBar = createMenuBar();

		BorderPane root = new BorderPane();

		columns = new HBox();
		// columns.setPadding(new Insets(15, 12, 15, 12));
		// columns.setSpacing(10);

		col1 = createIssuePanel();
		IssuePanel col2 = createIssuePanel();
		IssuePanel col3 = createIssuePanel();

		test = new TurboIssue("issue one", "description one");
		test.getLabels().addAll(new TurboLabel("bug"), new TurboLabel("thisisalonglabel"));
		TurboIssue two = new TurboIssue("issue two", "desc two");
		TurboIssue three = new TurboIssue("issue two", "desc three");
		TurboIssue four = new TurboIssue("issue four", "desc four");
		four.getLabels().addAll(new TurboLabel("request"), new TurboLabel("feature"));
		TurboIssue five = new TurboIssue("issue five", "desc five");

		col1.getItems().add(test);
		col1.getItems().add(two);
		col1.getItems().add(three);
		col1.getItems().add(four);
		col1.getItems().add(five);

		col2.getItems().add(test);

		// VBox col3 = createColumn();
		// col1.getChildren().addAll(createItem(), createItem());

		columns.getChildren().addAll(col1, col2, col3);

		// SplitPane splitPane = new SplitPane();
		// splitPane.getItems().addAll(createTreeView(), columns);
		// splitPane.setDividerPositions(0.2);

		root.setCenter(columns);
		root.setTop(menuBar);

		return root;
	}

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
		Menu columns = new Menu("Change number of columns....");
		view.getItems().addAll(columns);

		final ToggleGroup numberOfCols = new ToggleGroup();
		for (int i = 2; i <= 9; i++) {
			RadioMenuItem item = new RadioMenuItem(Integer.toString(i));
			item.setUserData(i);
			item.setToggleGroup(numberOfCols);
			columns.getItems().add(item);

			if (i == 3)
				item.setSelected(true);
		}

		menuBar.getMenus().addAll(projects, milestones, issues, labels, view);
		return menuBar;
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

			Text title = new Text("GitHub Login");
			title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(title, 0, 0, 2, 1);

			Label repoURL = new Label("Repository name:");
			grid.add(repoURL, 0, 1);

			TextField repoURLField = new TextField();
			grid.add(repoURLField, 1, 1);

			Label username = new Label("User Name:");
			grid.add(username, 0, 2);

			TextField usernameField = new TextField();
			grid.add(usernameField, 1, 2);

			Label password = new Label("Password:");
			grid.add(password, 0, 3);

			PasswordField passwordField = new PasswordField();
			grid.add(passwordField, 1, 3);

			Button loginButton = new Button("Sign in");
			loginButton.setOnAction((ev) -> {
				logic.login(usernameField.getText(), passwordField.getText());
				logic.setRepository(repoURLField.getText());
				dialogStage.hide();
				loadIssues();
			});

			HBox buttons = new HBox(10);
			buttons.setAlignment(Pos.BOTTOM_RIGHT);
			buttons.getChildren().add(loginButton);
			grid.add(buttons, 1, 4);

			Scene scene = new Scene(grid, 350, 275);
			dialogStage.setScene(scene);

			dialogStage.initOwner(mainStage);
			dialogStage.initModality(Modality.APPLICATION_MODAL);

			dialogStage.setX(mainStage.getX());
			dialogStage.setY(mainStage.getY());

			dialogStage.show();
		});
	}

	private void loadIssues() {
		col1.setItems(FXCollections.observableArrayList(logic.getIssues()));
	}

	@Override
	public void start(Stage stage) {
		mainStage = stage;
		
		Scene scene = new Scene(createRoot(), 800, 600);
		setUpHotkeys(scene);

		stage.setTitle("Demo");
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
