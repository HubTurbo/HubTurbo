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

public class ManageLabelsTreeCell<T> extends TreeCell<String> {
	
//	private final Stage mainStage;
	
	public ManageLabelsTreeCell(Stage main) {
//		this.mainStage = main;
	}
	
    private TextField textField;
    
    private void createTextField() {
        textField = new TextField(getItem());
        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> stringProperty,
					Boolean oldValue, Boolean newValue) {
				System.out.println(oldValue);
				System.out.println(newValue);
			}
		});
    }
    
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
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
	protected void updateItem(String itemText, boolean empty) {
		super.updateItem(itemText, empty);
		
		if (empty || itemText == null) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItem());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItem());
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
		MenuItem label = new MenuItem("this is a group");
		label.setOnAction((event) -> {
			System.out.println("this is a group");
		});
		return new MenuItem[] {label};
	}

	private boolean isGroupItem(TreeItem<String> treeItem) {
		assert treeItem != null;
		return treeItem.getParent() != null && treeItem.getParent().getValue().equals("Groups");
	}

	private ContextMenu getContextMenuForItem(TreeItem<String> treeItem) {
		if (isGroupItem(treeItem)) {
			return new ContextMenu(createTopLevelContextMenu());
		} else {
			return new ContextMenu(createContextMenu());
		}
	}
}
