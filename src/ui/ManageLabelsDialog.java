package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

		TreeView<LabelTreeItem> treeView = createTreeView(stage);
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
			
			(new GroupDialog(stage, "newgroup" + getUniqueId(), false))
			.setExclusiveCheckboxVisible(true)
			.show().thenApply(response -> {

				assert response.getValue() != null;
				if (response.getValue().isEmpty()) {
					return false;
				}

				TreeItem<LabelTreeItem> item = new TreeItem<>(response);
				treeView.getRoot().getChildren().add(item);

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

	private TreeView<LabelTreeItem> createTreeView(Stage stage) {
		
		final TreeItem<LabelTreeItem> treeRoot = new TreeItem<>(new TurboLabelGroup(ROOT_NAME));
		
		populateTree(treeRoot);

		final TreeView<LabelTreeItem> treeView = new TreeView<>();
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		treeView.setPrefWidth(180);

		treeRoot.setExpanded(true);
		treeRoot.getChildren().forEach(child -> child.setExpanded(true));

		treeView.setCellFactory(new Callback<TreeView<LabelTreeItem>, TreeCell<LabelTreeItem>>() {
			@Override
			public TreeCell<LabelTreeItem> call(TreeView<LabelTreeItem> stringTreeView) {
				return new ManageLabelsTreeCell<LabelTreeItem>(stage, model);
			}
		});

		return treeView;
	}

	public static HashMap<String, ArrayList<TurboLabel>> groupLabels(List<TurboLabel> labels) {
		HashMap<String, ArrayList<TurboLabel>> groups = new HashMap<>();
		for (TurboLabel l : labels) {
			String groupName = l.getGroup() == null ? UNGROUPED_NAME : l.getGroup();

			if (groups.get(groupName) == null) {
				groups.put(groupName, new ArrayList<TurboLabel>());
			}
			groups.get(groupName).add(l);
		}
		return groups;
	}

	private void populateTree(TreeItem<LabelTreeItem> treeRoot) {
		
		for (TurboLabel l : model.getLabels()) {
			assert l.getGroup() == null || !l.getGroup().equals(UNGROUPED_NAME);
		}
	
		// Hash all labels by group
		HashMap<String, ArrayList<TurboLabel>> labels = groupLabels(model.getLabels());
		ArrayList<TurboLabel> ungrouped = labels.get(UNGROUPED_NAME);
		if (ungrouped == null) ungrouped = new ArrayList<>();
		labels.remove(UNGROUPED_NAME);

		// Add labels with a group into the tree
		for (String groupName : labels.keySet()) {
			TurboLabelGroup group = new TurboLabelGroup(groupName);
			TreeItem<LabelTreeItem> groupItem = new TreeItem<>(group);
			treeRoot.getChildren().add(groupItem);
			
			boolean exclusive = true;
			for (TurboLabel l : labels.get(group.getValue())) {
				group.addLabel(l);
				TreeItem<LabelTreeItem> labelItem = new TreeItem<>(l);
				groupItem.getChildren().add(labelItem);
				exclusive = exclusive && l.isExclusive();
			}
			
			// Set exclusivity status
			group.setExclusive(exclusive);
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
