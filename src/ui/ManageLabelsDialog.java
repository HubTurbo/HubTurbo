package ui;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;

public class ManageLabelsDialog implements Dialog<String> {

	Stage parentStage;
	Model model;

	CompletableFuture<String> response;

	public ManageLabelsDialog(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 330, 400);

		Stage stage = new Stage();
		stage.setTitle("Manage labels");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		TreeView<String> treeView = createTreeView();
		layout.getChildren().addAll(treeView, createButtons(treeView.getRoot(), stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}

	private Node createButtons(TreeItem<String> root, Stage stage) {
		VBox container = new VBox();
		container.setSpacing(5);
		
		Button newGroup = new Button("New Group");
		newGroup.setOnAction(e -> {
			root.getChildren().add(new TreeItem<String>("New group"));
			// TODO trigger an edit on that node
		});
		
		Button close = new Button("Close");
		close.setOnAction(e -> {
			stage.close();
		});
		
		container.getChildren().addAll(newGroup, close);
		
		return container;
	}

	private TreeView<String> createTreeView() {
		
		final TreeItem<String> treeRoot = new TreeItem<>("Groups");
		treeRoot.setExpanded(true);
		
		TreeItem<String> status = new TreeItem<>("Status");

		treeRoot.getChildren().addAll(
				Arrays.asList(status));

		status.getChildren().addAll(
				Arrays.asList(new TreeItem<String>("NotStarted"),
						new TreeItem<String>("Done")));

		final TreeView<String> treeView = new TreeView<>();
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		treeView.setPrefWidth(180);
		treeView.setEditable(true);

		treeRoot.getChildren().forEach(child -> child.setExpanded(true));

		treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			@Override
			public TreeCell<String> call(TreeView<String> stringTreeView) {
				return new ManageLabelsTreeCell<String>(parentStage);
			}
		});

		return treeView;
	}
}
