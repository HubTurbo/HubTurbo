package ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public class ManageLabelsTreeCell<T> extends TreeCell<String> {
	
//	private final Stage mainStage;
	
	public ManageLabelsTreeCell(Stage main) {
//		this.mainStage = main;
	}
	
	@Override
	protected void updateItem(String itemText, boolean empty) {
		super.updateItem(itemText, empty);
		
		if (!empty && itemText != null) {
			setText(itemText);
			setGraphic(getTreeItem().getGraphic());
			setContextMenu(getContextMenuForItem(getTreeItem()));
		} else {
			setText(null);
			setGraphic(null);
		}
	}

	private MenuItem[] createContextMenu() {
		MenuItem group = new MenuItem("this is a label");
		group.setOnAction((event) -> {
			System.out.println("this is a label");
		});
		return new MenuItem[] {group};
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
