package ui;

import java.util.ArrayList;
import java.util.stream.Collectors;

import model.Model;
import model.TurboLabel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ManageLabelsTreeCell<T> extends TreeCell<LabelTreeItem> {

	private final Model model;
	
	public ManageLabelsTreeCell(Model model) {
		this.model = model;
	}
	
    private TextField textField;
    
    private void createTextField() {
        textField = new TextField(getItem().getValue());
        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit();
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> stringProperty,
					Boolean previouslyFocused, Boolean currentlyFocused) {
				assert previouslyFocused != currentlyFocused;
				if (!currentlyFocused) {
                    commitEdit();
				}
			}
		});
    }
    
    // This is NOT an overridden method.
    // The overridden one is, however, called in here.
    public void commitEdit() {
    	super.commitEdit(getItem());
    	
    	if (getItem() instanceof TurboLabel) {
    		TurboLabel label = (TurboLabel) getItem();
    		String oldName = label.toGhName();
        	label.setValue(textField.getText());

        	model.updateLabel(label, oldName);
    	}
    	else if (getItem() instanceof TurboLabelGroup) {
    		TurboLabelGroup group = (TurboLabelGroup) getItem();

    		// Get all the old names
    		ArrayList<String> oldNames = new ArrayList<>(group.getLabels().stream().map(l -> l.toGhName()).collect(Collectors.toList()));

    		// Update every label using TurboLabelGroup::setValue
    		group.setValue(textField.getText());

    		// Trigger updates on all the labels
    		for (int i=0; i<oldNames.size(); i++) {
    			model.updateLabel(group.getLabels().get(i), oldNames.get(i));
    		}
    	}
    	else {
    		assert false;
    	}
    }
    
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().getValue());
        setGraphic(getTreeItem().getGraphic());
    }
    
    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.setText(getItem().getValue());
        textField.selectAll();
        textField.requestFocus();
    }
    
	@Override
	protected void updateItem(LabelTreeItem itemText, boolean empty) {
		super.updateItem(itemText, empty);
		
		if (empty || itemText == null) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItem().getValue());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItem().getValue());
                setGraphic(getTreeItem().getGraphic());
    			setContextMenu(getContextMenuForItem(getTreeItem()));
            }
		}
	}

	private MenuItem[] createLabelContextMenu() {
		MenuItem edit = new MenuItem("Edit Label");
		edit.setOnAction((event) -> {
			getTreeView().edit(getTreeItem());
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
				
		MenuItem group = new MenuItem("Edit Group");
		group.setOnAction((event) -> {
			getTreeView().edit(getTreeItem());
		});

		MenuItem label = new MenuItem("New Label");
		label.setOnAction((event) -> {
			
			// Create a new label
			TurboLabel newLabel = new TurboLabel("new-label" + ManageLabelsDialog.getUniqueId());
			
			// Set its group value to null if it's being created under the <Ungrouped> group
			String groupName = getTreeItem().getValue().getValue();
			if (groupName.equals(ManageLabelsDialog.UNGROUPED_NAME)) groupName = null;
			newLabel.setGroup(groupName);
			
			newLabel = model.createLabel(newLabel);
			
			// Make sure this TurboLabelGroup has a reference to the new label
			((TurboLabelGroup) getTreeItem().getValue()).addLabel(newLabel);
			getTreeItem().getChildren().add(new TreeItem<LabelTreeItem>(newLabel));
			
			getTreeItem().setExpanded(true);
		});
		
		boolean isUngroupedHeading = getTreeItem().getValue().getValue().equals(ManageLabelsDialog.UNGROUPED_NAME);

		if (isUngroupedHeading) {
			return new MenuItem[] {label};
		} else {
			return new MenuItem[] {group, label};
		}
	}

	private boolean isGroupItem(TreeItem<LabelTreeItem> treeItem) {
		assert treeItem != null;
		return treeItem.getParent() != null && treeItem.getParent().getValue().getValue().equals(ManageLabelsDialog.ROOT_NAME);
	}

	private ContextMenu getContextMenuForItem(TreeItem<LabelTreeItem> treeItem) {
		if (isGroupItem(treeItem)) {
			return new ContextMenu(createGroupContextMenu());
		} else {
			return new ContextMenu(createLabelContextMenu());
		}
	}
}
