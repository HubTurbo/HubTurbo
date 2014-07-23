package ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import service.ServiceManager;

public class MenuControl extends MenuBar {

	private final ColumnControl columns;
	private final SidePanel sidePanel;

	public MenuControl(ColumnControl columns, SidePanel sidePanel) {
		this.columns = columns;
		this.sidePanel = sidePanel;
		
		createMenuItems();
	}
	
	private void createMenuItems() {
		Menu issues = new Menu("Issues");
		issues.getItems().addAll(createNewIssueMenuItem());

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createColumnsMenuItem());

		getMenus().addAll(issues, view);
	}


	private MenuItem createColumnsMenuItem() {
		Menu cols = new Menu("Columns");

		MenuItem createLeft = new MenuItem("Create Column (Left)");
		createLeft.setOnAction(e -> columns.createNewSearchPanelAtStart());
		createLeft.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

		MenuItem createRight = new MenuItem("Create Column");
		createRight.setOnAction(e -> columns.createNewSearchPanelAtEnd());
		createRight.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

		MenuItem closeColumn = new MenuItem("Close Column");
		closeColumn.setOnAction(e -> columns.closeCurrentColumn());
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		cols.getItems().addAll(createRight, createLeft, closeColumn);
		return cols;
	}


	private MenuItem createRefreshMenuItem() {
		MenuItem refreshMenuItem = new MenuItem("Refresh");
		refreshMenuItem.setOnAction((e) -> {
			ServiceManager.getInstance().restartModelUpdate();
			columns.refresh();
		});
		refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		return refreshMenuItem;
	}

	private MenuItem createNewIssueMenuItem() {
		MenuItem newIssueMenuItem = new MenuItem("New Issue");
		newIssueMenuItem.setOnAction(e -> sidePanel.onCreateIssueHotkey());
		newIssueMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		return newIssueMenuItem;
	}
}
