package ui;

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
import javafx.stage.Stage;

public class ManageLabelsTreeCell<T> extends TreeCell<LabelTreeItem> {
	
//	private final Stage mainStage;
	
	public ManageLabelsTreeCell(Stage main) {
//		this.mainStage = main;
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
    	getItem().setValue(textField.getText());
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

	private MenuItem[] createContextMenu() {
		MenuItem group = new MenuItem("this is a label");
		group.setOnAction((event) -> {
			System.out.println("this is a label");
		});
		MenuItem edit = new MenuItem("Edit Label");
		edit.setOnAction((event) -> {
			getTreeView().edit(getTreeItem());
		});
		return new MenuItem[] {group, edit};
	}

	private MenuItem[] createTopLevelContextMenu() {
		MenuItem label = new MenuItem("New Label");
		label.setOnAction((event) -> {
			System.out.println("this is a group");
		});
		return new MenuItem[] {label};
	}

	private boolean isGroupItem(TreeItem<LabelTreeItem> treeItem) {
		assert treeItem != null;
		return treeItem.getParent() != null && treeItem.getParent().getValue().equals("Groups");
	}

	private ContextMenu getContextMenuForItem(TreeItem<LabelTreeItem> treeItem) {
		if (isGroupItem(treeItem)) {
			return new ContextMenu(createTopLevelContextMenu());
		} else {
			return new ContextMenu(createContextMenu());
		}
	}
}
