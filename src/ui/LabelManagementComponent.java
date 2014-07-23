package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboLabel;

public class LabelManagementComponent {

	public static final String UNGROUPED_NAME = "<Ungrouped>";
	public static final String ROOT_NAME = "Labels";
	
	private final Stage parentStage;
	private final Model model;
	private final SidePanel sidePanel;

	public LabelManagementComponent(Stage parentStage, Model model, SidePanel sidePanel) {
		this.parentStage = parentStage;
		this.model = model;
		this.sidePanel = sidePanel;
	}

	public VBox initialise() {
		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		TreeView<LabelTreeItem> treeView = createTreeView(parentStage);
		layout.getChildren().addAll(createButtons(treeView), treeView);
		return layout;
	}

	private Node createButtons(TreeView<LabelTreeItem> treeView) {
		Button create = new Button("New Label Group");
		create.setOnAction(e -> {
			ManageLabelsTreeCell.createNewGroup(parentStage, treeView);
		});
		HBox.setHgrow(create, Priority.ALWAYS);
		create.setMaxWidth(Double.MAX_VALUE);

		VBox container = new VBox();
		container.setSpacing(5);
		container.getChildren().addAll(create);
		
		return container;
	}

	private TreeView<LabelTreeItem> createTreeView(Stage stage) {
		
		final TreeItem<LabelTreeItem> treeRoot = new TreeItem<>(new TurboLabelGroup(ROOT_NAME));
		populateTree(treeRoot);

		final TreeView<LabelTreeItem> treeView = new TreeView<>();
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		HBox.setHgrow(treeView, Priority.ALWAYS);
//		VBox.setVgrow(treeView, Priority.ALWAYS);
		treeView.setPrefHeight(2000);

		treeRoot.setExpanded(true);
		treeRoot.getChildren().forEach(child -> child.setExpanded(true));

		treeView.setCellFactory(new Callback<TreeView<LabelTreeItem>, TreeCell<LabelTreeItem>>() {
			@Override
			public TreeCell<LabelTreeItem> call(TreeView<LabelTreeItem> stringTreeView) {
				return new ManageLabelsTreeCell<LabelTreeItem>(stage, model, sidePanel);
			}
		});

		return treeView;
	}

	private void populateTree(TreeItem<LabelTreeItem> treeRoot) {
		
		for (TurboLabel l : model.getLabels()) {
			assert l.getGroup() == null || !l.getGroup().equals(UNGROUPED_NAME);
		}
	
		// Hash all labels by group
		HashMap<String, ArrayList<TurboLabel>> labels = TurboLabel.groupLabels(model.getLabels(), UNGROUPED_NAME);
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
