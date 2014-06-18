package ui;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Demo extends Application {

	private static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";

	private Stage mainStage;
	private HBox columns;

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

	private VBox createColumn() {
		VBox col = new VBox();
//		col.setPadding(new Insets(15, 12, 15, 12));
//		col.setSpacing(10);
		col.setPrefWidth(400);
		HBox.setHgrow(col, Priority.ALWAYS);

		col.setStyle(STYLE_BORDERS);
		col.setAlignment(Pos.TOP_CENTER);
		
		ListView<String> listView = new ListView<>();
		ObservableList<String> items = FXCollections.observableArrayList();
		items.add("asakda");
		items.add("kljkljhsk");
		listView.setItems(items);
		
		listView.setCellFactory(new Callback<ListView<String>, 
	            ListCell<String>>() {
	                @Override 
	                public ListCell<String> call(ListView<String> list) {
	                    return new CustomListCell();
	                }
	            }
	        );
		
		col.getChildren().add(listView);
		

//		col.setOnDragOver((DragEvent event) -> {
//			if (event.getGestureSource() != col
//					&& event.getDragboard().hasString()) {
//				event.acceptTransferModes(TransferMode.COPY);
//			}
//
//			event.consume();
//		});
//
//		col.setOnDragEntered((DragEvent event) -> {
//			if (event.getGestureSource() != col
//					&& event.getDragboard().hasString()) {
//
//				if (done)
//					return;
//
//				col.setStyle(STYLE_YELLOW_BORDERS);
//			}
//
//			event.consume();
//		});
//
//		col.setOnDragExited((DragEvent event) -> {
//			col.setStyle(STYLE_BORDERS);
//
//			event.consume();
//		});
//
//		col.setOnDragDropped((DragEvent event) -> {
//			Dragboard db = event.getDragboard();
//			boolean success = false;
//			if (db.hasString() && !done) {
//				done = true;
//				success = true;
//
//				// add the item here
//				DragData dd = DragData.deserialize(db.getString());
//
//				if (dd.source == DragSource.PANEL_MILESTONES) {
//					BorderPane item = items.get(dd.index);
//					col.getChildren().add(item);
//				} else if (dd.source == DragSource.TREE_ISSUES) {
//					BorderPane item = createItem("issue: " + dd.text);
//					col.getChildren().add(item);
//				} else if (dd.source == DragSource.TREE_MILESTONES) {
//					col.getChildren().clear();
//
//					col.setPadding(new Insets(15, 12, 15, 12));
//					Label which = new Label();
//					which.setText("Milestone " + dd.text);
//
//					which.setFont(Font.font("System Regular", FontWeight.BOLD,
//							16));
//
//					Label issuesLabel = new Label();
//					issuesLabel.setText("Issues");
//
//					ObservableList<String> data = FXCollections
//							.observableArrayList();
//					// data.addAll("one", "two", "three");
//
//					ListView<String> issues = new ListView<>(data);
//					String defaultIssuesStyle = issues.getStyle();
//
//					issues.setOnDragOver((e) -> {
//						if (event.getGestureSource() != issues
//								&& event.getDragboard().hasString()) {
//							event.acceptTransferModes(TransferMode.COPY);
//						}
//						event.consume();
//					});
//
//					issues.setOnDragEntered((e) -> {
//						if (event.getGestureSource() != issues
//								&& event.getDragboard().hasString()) {
//							Dragboard db2 = event.getDragboard();
//							if (db2.hasString()) {
//								DragData dd2 = DragData.deserialize(db
//										.getString());
//								if (dd2.source == DragSource.TREE_ISSUES) {
//									issues.setStyle(STYLE_YELLOW_BORDERS);
//								}
//							}
//						}
//						event.consume();
//					});
//
//					issues.setOnDragExited((e) -> {
//						issues.setStyle(defaultIssuesStyle);
//
//						event.consume();
//					});
//
//					issues.setOnDragDropped((e) -> {
//						Dragboard db2 = event.getDragboard();
//						boolean success2 = false;
//						if (db2.hasString()) {
//							success2 = true;
//							DragData dd2 = DragData.deserialize(db2.getString());
//							System.out.println("oiukjjkjl");
//							if (dd2.source == DragSource.TREE_ISSUES) {
//								data.add(dd2.text);
//								System.out.println("aklsjdkasjl");
//							}
//						}
//						event.setDropCompleted(success2);
//
//						event.consume();
//					});
//
//					// // Creating tree items
//					// final TreeItem<String> childNode1 = new TreeItem<>(
//					// "Child Node 1");
//					// final TreeItem<String> childNode2 = new TreeItem<>(
//					// "Child Node 2");
//					// final TreeItem<String> childNode3 = new TreeItem<>(
//					// "Child Node 3");
//					//
//					// // Creating the root element
//					// final TreeItem<String> root = new
//					// TreeItem<>("Root node");
//					// root.setExpanded(true);
//					//
//					// // Adding tree items to the root
//					// root.getChildren().add(childNode1);
//					// root.getChildren().add(childNode2);
//					// root.getChildren().add(childNode3);
//					//
//					// // Creating a column
//					// TreeTableColumn<String, String> column = new
//					// TreeTableColumn<>(
//					// "Issue");
//					// column.setPrefWidth(150);
//					//
//					// TreeTableColumn<String, String> length = new
//					// TreeTableColumn<>(
//					// "Length");
//					// length.setPrefWidth(150);
//					//
//					// // Defining cell content
//					// column.setCellValueFactory((
//					// CellDataFeatures<String, String> p) -> new
//					// ReadOnlyStringWrapper(
//					// p.getValue().getValue()));
//					// length.setCellValueFactory((
//					// CellDataFeatures<String, String> p) -> new
//					// ReadOnlyStringWrapper(
//					// Integer.toString(p.getValue().getValue().length())));
//					//
//					// // Creating a tree table view
//					// final TreeTableView<String> issues = new TreeTableView<>(
//					// root);
//					// issues.getColumns().add(column);
//					// issues.getColumns().add(length);
//					// issues.setPrefWidth(152);
//					// issues.setShowRoot(true);
//
//					col.getChildren().add(which);
//					col.getChildren().add(issuesLabel);
//					col.getChildren().add(issues);
//
//				} else if (dd.source == DragSource.TREE_LABELS) {
//					BorderPane item = createItem("label: " + dd.text);
//					col.getChildren().add(item);
//				} else if (dd.source == DragSource.TREE_CONTRIBUTORS) {
//					BorderPane item = createItem("contributor: " + dd.text);
//					col.getChildren().clear();
//					col.getChildren().add(item);
//				}
//			}
//
//			// this removes the item from its previous parent
//			event.setDropCompleted(success);
//
//			event.consume();
//		});

		return col;
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

		// TODO the panels aren't ordered in insertion order? watch out for that
		ObservableList<Node> panels = columns.getChildren();
		int panelSize = panels.size();

		if (panelSize == to) {
			return;
		}

		if (panelSize < to) {
			for (int i = 0; i < to - panelSize; i++) {
				panels.add(createColumn());
			}
		} else { // panels.size() > to
			int numberToRemove = panels.size() - to;
			panels.remove(panels.size() - 1 - numberToRemove, panels.size() - 1);
		}
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

	private Parent createRoot() {
		MenuBar menuBar = createMenuBar();
		
		BorderPane root = new BorderPane();

		columns = new HBox();
//		columns.setPadding(new Insets(15, 12, 15, 12));
//		columns.setSpacing(10);

		VBox col1 = createColumn();
		VBox col2 = createColumn();
		VBox col3 = createColumn();
		// VBox col3 = createColumn();
		// col1.getChildren().addAll(createItem(), createItem());

		columns.getChildren().addAll(col1, col2 , col3);

//		SplitPane splitPane = new SplitPane();
//		splitPane.getItems().addAll(createTreeView(), columns);
//		splitPane.setDividerPositions(0.2);

		root.setCenter(columns);
		root.setTop(menuBar);

		return root;
	}

	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		Menu projects = new Menu("Projects");
		MenuItem config = new MenuItem("Configuration");
		projects.getItems().addAll(config);
		
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
		for (int i=2; i<=9; i++) {
		    RadioMenuItem item = new RadioMenuItem(Integer.toString(i));
		    item.setUserData(i);
		    item.setToggleGroup(numberOfCols);
		    columns.getItems().add(item);
		    
		    if (i == 3) item.setSelected(true);
		}
		
		menuBar.getMenus().addAll(projects, milestones, issues, labels, view);
		return menuBar;
	}

	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(createRoot(), 800, 600);
		setUpHotkeys(scene);
	
		mainStage = stage;
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
