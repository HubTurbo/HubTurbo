package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.stream.Collectors;

import model.Model;
import model.TurboLabel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ManageLabelsTreeCell<T> extends TreeCell<LabelTreeItem> {

	private final Model model;
	private final Stage stage;
	private ChangeListener<Boolean> textFieldListener;
	
	public ManageLabelsTreeCell(Stage stage, Model model) {
		this.model = model;
		this.stage = stage;
	}
	
    private TextField textField;
    
    private void initialiseTextFieldListener(){
    	WeakReference<ManageLabelsTreeCell<T>> that = new WeakReference<ManageLabelsTreeCell<T>>(this);
    	textFieldListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> stringProperty,
					Boolean previouslyFocused, Boolean currentlyFocused) {
				assert previouslyFocused != currentlyFocused;
				ManageLabelsTreeCell<T> thisRef = that.get();
				if (thisRef != null && !currentlyFocused) {
                    thisRef.commitEdit();
				}
			}
		};
    }
    
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
        initialiseTextFieldListener();
        textField.focusedProperty().addListener(new WeakChangeListener<Boolean>(textFieldListener));
    }
    
    // This is NOT an overridden method.
    // The overridden one is, however, called in here.
    public void commitEdit() {
    	super.commitEdit(getItem());
    	
		getTreeView().setEditable(false);

		assert textField.getText() != null;
    	if (textField.getText().isEmpty()) {
    		cancelEdit();
    		return;
    	}

		assert getItem() instanceof TurboLabel;
		TurboLabel label = (TurboLabel) getItem();
		String oldName = label.toGhName();
    	label.setValue(textField.getText());

    	model.updateLabel(label, oldName);
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
			getTreeView().setEditable(true);
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
				
		MenuItem groupMenuItem = new MenuItem("Edit Group");
		groupMenuItem.setOnAction((event) -> {
//			getTreeView().edit(getTreeItem());
			
			TurboLabelGroup group = (TurboLabelGroup) getItem();
			
			(new GroupDialog(stage, group.getValue(), group.isExclusive()))
			.setExclusiveCheckboxVisible(false)
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
			TurboLabel newLabel = new TurboLabel("newlabel" + ManageLabelsDialog.getUniqueId());
			
			// Set its group value to null if it's being created under the <Ungrouped> group
			String groupName = getTreeItem().getValue().getValue();
			if (groupName.equals(ManageLabelsDialog.UNGROUPED_NAME)) groupName = null;
			newLabel.setGroup(groupName);
			
			// Set its exclusivity
			newLabel.setExclusive(((TurboLabelGroup) getTreeItem().getValue()).isExclusive());
			
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
			return new MenuItem[] {groupMenuItem, label};
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
