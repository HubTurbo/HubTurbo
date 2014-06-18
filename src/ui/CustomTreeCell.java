package ui;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

public class CustomTreeCell<T> extends TreeCell<String> {
	
	private final Stage mainStage;
	
	public CustomTreeCell(Stage main) {
		this.mainStage = main;
	}
	
	@Override
	protected void updateItem(String itemText, boolean empty) {
		super.updateItem(itemText, empty);
		
		if (!empty && itemText != null) {
			setText(itemText);
			setGraphic(getTreeItem().getGraphic());
			setContextMenu(getContextMenuForItem(getTreeItem()));
			
			setOnDragDetected((MouseEvent event) -> {
				TreeItem<String> treeItem = getTreeItem();
	
				Dragboard db = startDragAndDrop(TransferMode.ANY);
				ClipboardContent content = new ClipboardContent();
				DragSource sourceType = DragSource.PANEL_ISSUES;
	
				switch (getType(treeItem)) {
				case ISSUE:
					sourceType = DragSource.TREE_ISSUES;
					break;
				case MILESTONE:
					sourceType = DragSource.TREE_MILESTONES;
					break;
				case CONTRIBUTOR:
					sourceType = DragSource.TREE_CONTRIBUTORS;
					break;
				case LABEL:
					sourceType = DragSource.TREE_LABELS;
					break;
				}
				DragData dd = new DragData(sourceType);
				dd.text = treeItem.getValue();
				content.putString(dd.serialize());
	
				db.setContent(content);
	
				event.consume();
			});
	
			setOnDragDone((DragEvent event) -> {
	
				if (event.getTransferMode() == TransferMode.MOVE) {
				}
	
				event.consume();
			});
			
		} else {
			setText(null);
			setGraphic(null);
		}
		
	}

	private MenuItem[] createIssueMenu() {
		MenuItem deleteIssue = new MenuItem("Delete Issue");
		deleteIssue.setOnAction((event) -> {
			getTreeItem().getParent().getChildren().remove(getTreeItem());
		});
		return new MenuItem[] {deleteIssue};
	}

	private MenuItem[] createTopLevelIssueMenu() {
		MenuItem newIssue = new MenuItem("New Issue");
		newIssue.setOnAction((event) -> {
			(new NewIssueDialog(mainStage)).show().thenApply(
					msg -> {
						System.out.println(msg);
						if (msg.equals("ok")) {
							System.out.println("YES");
						}
						// return new
						// CompletableFuture<Boolean>().;
						return true;
					});
		});
		return new MenuItem[] {newIssue};
	}

	private MenuItem[] createMilestoneMenu() {
		MenuItem placeholder = new MenuItem("milestone menu");
		return new MenuItem[] {placeholder};
	}

	private MenuItem[] createTopLevelMilestoneMenu() {
		MenuItem placeholder = new MenuItem("top level milestone menu");
		return new MenuItem[] {placeholder};
	}

	private MenuItem[] createLabelMenu() {
		MenuItem placeholder = new MenuItem("label menu");
		return new MenuItem[] {placeholder};
	}

	private MenuItem[] createTopLevelLabelMenu() {
		MenuItem placeholder = new MenuItem("top level label menu");
		return new MenuItem[] {placeholder};
	}

	private MenuItem[] createContributorMenu() {
		MenuItem placeholder = new MenuItem("contributor menu");
		return new MenuItem[] {placeholder};
	}

	private MenuItem[] createTopLevelContributorMenu() {
		MenuItem placeholder = new MenuItem("top level contributor menu");
		return new MenuItem[] {placeholder};
	}

	private EntityType getType(TreeItem<String> treeItem) {
		TreeItem<String> current = treeItem;
		while (!current.getParent().getValue().equals("root")) {
			current = current.getParent();
		}
	
		switch (current.getValue()) {
		case "Issues":
			return EntityType.ISSUE;
		case "Milestones":
			return EntityType.MILESTONE;
		case "Contributors":
			return EntityType.CONTRIBUTOR;
		case "Labels":
			return EntityType.LABEL;
		default:
			throw new IllegalArgumentException(
					"Unrecognized tree node type " + current.getValue());
		}
	}

	private boolean isTopLevelItem(TreeItem<String> treeItem) {
		return treeItem.getParent().getValue().equals("root");
	}

	private ContextMenu getContextMenuForItem(TreeItem<String> treeItem) {
		switch (getType(treeItem)) {
		case ISSUE:
			if (isTopLevelItem(treeItem)) {
				return new ContextMenu(createTopLevelIssueMenu());
			} else {
				return new ContextMenu(createIssueMenu());
			}
		case MILESTONE: {
			if (isTopLevelItem(treeItem)) {
				return new ContextMenu(createTopLevelMilestoneMenu());
			} else {
				return new ContextMenu(createMilestoneMenu());
			}
		}
		case CONTRIBUTOR: {
			if (isTopLevelItem(treeItem)) {
				return new ContextMenu(createTopLevelContributorMenu());
			} else {
				return new ContextMenu(createContributorMenu());
			}
		}
		case LABEL: {
			if (isTopLevelItem(treeItem)) {
				return new ContextMenu(createTopLevelLabelMenu());
			} else {
				return new ContextMenu(createLabelMenu());
			}
		}
		default:
			throw new IllegalArgumentException();
		}
	}
}
