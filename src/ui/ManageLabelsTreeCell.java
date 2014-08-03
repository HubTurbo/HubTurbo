package ui;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import model.Model;
import model.TurboLabel;

public class ManageLabelsTreeCell<T> extends TreeCell<LabelTreeItem> {

	private final Model model;
	private final Stage stage;
	private final SidePanel sidePanel;

	public ManageLabelsTreeCell(Stage stage, Model model, SidePanel sidePanel) {
		this.model = model;
		this.stage = stage;
		this.sidePanel = sidePanel;
	}
    
	@Override
	protected void updateItem(LabelTreeItem treeItem, boolean empty) {
		super.updateItem(treeItem, empty);
		
		if (treeItem == null) {
			setText(null);
			setGraphic(null);
		}
		else {
			Label label = new Label(treeItem.getValue());
	        setGraphic(label);
			setContextMenu(getContextMenuForItem(getTreeItem()));
			if (getTreeItem().getValue() instanceof TurboLabel) {
				label.getStyleClass().add("labels");
				label.setStyle(((TurboLabel) getTreeItem().getValue()).getStyle());
				
				setOnDragDetected((event) -> {
					Dragboard db = startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();
					DragData dd = new DragData(DragData.Source.LABEL_TAB, ((TurboLabel) getTreeItem().getValue()).toGhName());
					content.putString(dd.serialise());
					db.setContent(content);
					event.consume();
				});
				
				setOnDragDone((event) -> {
					event.consume();
				});
			}
		}
	}
	
	private void triggerLabelEdit(TurboLabel label, boolean isNewLabel) {
		String oldName = label.toGhName();

		(new EditLabelDialog(stage, label)).show().thenApply(response -> {
	    	if (isNewLabel) {
	    		TurboLabel createdLabel = model.createLabel(response);
	    		// Make sure this TurboLabelGroup has a reference to the new label
				((TurboLabelGroup) getTreeItem().getValue()).addLabel(createdLabel);
				getTreeItem().getChildren().add(new TreeItem<LabelTreeItem>(createdLabel));
				getTreeItem().setExpanded(true);
	    	} else {
	    		model.updateLabel(response, oldName);
	    		// The tree view doesn't update automatically because there is;
		    	// no binding; manually trigger the update
		    	label.copyValues(response);
		    	updateItem(label, false);
		    	sidePanel.refresh();
	    	}
			return true;
		}).exceptionally(e -> {
			e.printStackTrace();
			return false;
		});
	}

	private MenuItem[] createLabelContextMenu() {
		MenuItem edit = new MenuItem("Edit Label");
		edit.setOnAction((event) -> {
			assert getItem() instanceof TurboLabel;
			TurboLabel original = (TurboLabel) getItem();
			triggerLabelEdit(original, false);
		});
		MenuItem delete = new MenuItem("Delete Label");
		delete.setOnAction((event) -> {
			model.deleteLabel((TurboLabel) getItem());
			for (TreeItem<LabelTreeItem> lti : getTreeItem().getParent().getChildren()) {
				if (lti.getValue().getValue().equals(getItem().getValue())) {
					getTreeItem().getParent().getChildren().remove(lti);
					break;
				}
			}
		});
		return new MenuItem[] {edit, delete};
	}

	private MenuItem[] createGroupContextMenu() {
				
		MenuItem groupMenuItem = new MenuItem("Edit Group");
		groupMenuItem.setOnAction((event) -> {
			
			TurboLabelGroup group = (TurboLabelGroup) getItem();
			
			(new EditGroupDialog(stage, group))
				.setExclusiveCheckboxEnabled(false)
				.show().thenApply(response -> {
					assert response.getValue() != null;
					if (response.getValue().isEmpty()) {
						return false;
					}
	
		    		// Get all the old names
		    		ArrayList<String> oldNames = new ArrayList<>(group.getLabels().stream().map(l -> l.toGhName()).collect(Collectors.toList()));
	
		    		// Update every label using TurboLabelGroup::setValue
		    		group.setValue(response.getValue());
		    		group.setExclusive(response.isExclusive());
	
		    		// Trigger updates on all the labels
		    		for (int i=0; i<oldNames.size(); i++) {
		    			model.updateLabel(group.getLabels().get(i), oldNames.get(i));
		    		}
		    		
		    		// Manually update the treeview, since there is no binding
		    		TreeItem<LabelTreeItem> item = getTreeItem();
		    		TreeItem<LabelTreeItem> parent = item.getParent();
		    		parent.getChildren().remove(item);
		    		parent.getChildren().add(item);
		    		
					return true;
				})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
		});

		MenuItem label = new MenuItem("New Label");
		label.setOnAction((event) -> {
			
			// Create a new label
			TurboLabel newLabel = new TurboLabel();
			newLabel.setName("newlabel");
			
			// Set its group value to null if it's being created under the <Ungrouped> group
			String groupName = getTreeItem().getValue().getValue();
			if (groupName.equals(LabelManagementComponent.UNGROUPED_NAME)) groupName = null;
			newLabel.setGroup(groupName);
			
			// Set its exclusivity
			newLabel.setExclusive(((TurboLabelGroup) getTreeItem().getValue()).isExclusive());
			
			triggerLabelEdit(newLabel, true);
			
		});
		
		boolean isUngroupedHeading = getTreeItem().getValue().getValue().equals(LabelManagementComponent.UNGROUPED_NAME);

		if (isUngroupedHeading) {
			return new MenuItem[] {label};
		} else {
			return new MenuItem[] {groupMenuItem, label};
		}
	}

	private boolean isGroupItem(TreeItem<LabelTreeItem> treeItem) {
		assert treeItem != null;
		return treeItem.getParent() != null && treeItem.getParent().getValue().getValue().equals(LabelManagementComponent.ROOT_NAME);
	}
	
	private ContextMenu getContextMenuForItem(TreeItem<LabelTreeItem> treeItem) {
		if (isRoot(treeItem)) {
			return new ContextMenu(createRootContextMenu());
		} else if (isGroupItem(treeItem)) {
			return new ContextMenu(createGroupContextMenu());
		} else {
			return new ContextMenu(createLabelContextMenu());
		}
	}

	private MenuItem[] createRootContextMenu() {
		MenuItem newGroup = new MenuItem("New Label Group");
		newGroup.setOnAction((event) -> {
			createNewGroup(stage, getTreeView());
		});
		return new MenuItem[] {newGroup};
	}

	public static void createNewGroup(Stage stage, TreeView<LabelTreeItem> treeView) {
		TurboLabelGroup group = new TurboLabelGroup("");
		(new EditGroupDialog(stage, group))
			.setExclusiveCheckboxEnabled(true)
			.show().thenApply(response -> {

				assert response.getValue() != null;
				if (response.getValue().isEmpty()) {
					return false;
				}

				TreeItem<LabelTreeItem> item = new TreeItem<>(response);
				treeView.getRoot().getChildren().add(item);

				return true;
			}).exceptionally(ex -> {
				ex.printStackTrace();
				return false;
			});
	}

	private boolean isRoot(TreeItem<LabelTreeItem> treeItem) {
		return treeItem.getParent() == null && treeItem.getValue().getValue().equals(LabelManagementComponent.ROOT_NAME);
	}
}
