package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
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
import model.TurboLabel;

public class ManageLabelsDialog implements Dialog<String> {

	public static final String UNGROUPED_NAME = "<Ungrouped>";
	public static final String ROOT_NAME = "root";
	
	private final Stage parentStage;
	private final Model model;

	private CompletableFuture<String> response;

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
		stage.setTitle("Manage Labels");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		TreeView<LabelTreeItem> treeView = createTreeView();
		layout.getChildren().addAll(treeView, createButtons(treeView, stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}
	
	private Node createButtons(TreeView<LabelTreeItem> treeView, Stage stage) {
		VBox container = new VBox();
		container.setSpacing(5);
		
		Button newGroup = new Button("New Group");
		newGroup.setOnAction(e -> {
			
			(new GroupDialog(stage, "newgroup" + getUniqueId(), false)).show().thenApply(response -> {

				TreeItem<LabelTreeItem> item = new TreeItem<>(new TurboLabelGroup("newgroup" + getUniqueId()));
				treeView.getRoot().getChildren().add(item);

				Platform.runLater(() -> {
					treeView.requestFocus();
					// This does not work for some reason
					treeView.edit(item);
				});

				return true;
			});
			
		});
		
		Button close = new Button("Close");
		close.setOnAction(e -> {
			stage.close();
		});
		
		container.getChildren().addAll(newGroup, close);
		
		return container;
	}

	private TreeView<LabelTreeItem> createTreeView() {
		
		final TreeItem<LabelTreeItem> treeRoot = new TreeItem<>(new TurboLabelGroup(ROOT_NAME));
		
		populateTree(treeRoot);

		final TreeView<LabelTreeItem> treeView = new TreeView<>();
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		treeView.setPrefWidth(180);
		treeView.setEditable(true);

		treeRoot.setExpanded(true);
		treeRoot.getChildren().forEach(child -> child.setExpanded(true));

		treeView.setCellFactory(new Callback<TreeView<LabelTreeItem>, TreeCell<LabelTreeItem>>() {
			@Override
			public TreeCell<LabelTreeItem> call(TreeView<LabelTreeItem> stringTreeView) {
				return new ManageLabelsTreeCell<LabelTreeItem>(model);
			}
		});

		return treeView;
	}

	private void populateTree(TreeItem<LabelTreeItem> treeRoot) {
		
		// Hash all labels by group
		HashMap<String, ArrayList<TurboLabel>> labels = new HashMap<>();
		ArrayList<TurboLabel> ungrouped = new ArrayList<>();
		for (TurboLabel l : model.getLabels()) {
			if (l.getGroup() == null || l.getGroup().equals(UNGROUPED_NAME)) {
				l.setGroup(null);
				ungrouped.add(l);
			} else {
				if (labels.get(l.getGroup()) == null) {
					labels.put(l.getGroup(), new ArrayList<TurboLabel>());
				}
				labels.get(l.getGroup()).add(l);
			}
		}
				
		// Add labels with a group into the tree
		for (String groupName : labels.keySet()) {
			TurboLabelGroup group = new TurboLabelGroup(groupName);
			TreeItem<LabelTreeItem> groupItem = new TreeItem<>(group);
			treeRoot.getChildren().add(groupItem);
			
			for (TurboLabel l : labels.get(group.getValue())) {
				group.addLabel(l);
				TreeItem<LabelTreeItem> labelItem = new TreeItem<>(l);
				groupItem.getChildren().add(labelItem);
			}
		}
		
		// Do the same for ungrouped labels
		TurboLabelGroup ungroupedGroup = new TurboLabelGroup(UNGROUPED_NAME);
		TreeItem<LabelTreeItem> ungroupedItem = new TreeItem<>(ungroupedGroup);
		treeRoot.getChildren().add(ungroupedItem);

		for (TurboLabel l : ungrouped) {
			ungroupedGroup.addLabel(l);
			TreeItem<LabelTreeItem> labelItem = new TreeItem<>(l);
			ungroupedItem.getChildren().add(labelItem);
		}
	}

	public static String getUniqueId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
