package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import service.ServiceManager;
import ui.issuecolumn.ColumnControl;
import ui.sidepanel.SidePanel;
import util.ConfigFileHandler;

public class MenuControl extends MenuBar {

	private final ColumnControl columns;
	private final SidePanel sidePanel;
	private final ScrollPane columnsScroll;

	public MenuControl(ColumnControl columns, SidePanel sidePanel, ScrollPane columnsScroll) {
		this.columns = columns;
		this.sidePanel = sidePanel;
		this.columnsScroll = columnsScroll;
		createMenuItems();
	}
	
	private void createMenuItems() {
		Menu issues = new Menu("Issues");
		issues.getItems().addAll(createNewIssueMenuItem());

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createColumnsMenuItem(), createDocumentationMenuItem());

		getMenus().addAll(issues, view);
	}


	private MenuItem createColumnsMenuItem() {
		Menu cols = new Menu("Columns");

		MenuItem createLeft = new MenuItem("Create Column (Left)");
		createLeft.setOnAction(e -> {
			columns.createNewSearchPanelAtStart();
			columnsScroll.setHvalue(columnsScroll.getHmin());
		});
		createLeft.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

		MenuItem createRight = new MenuItem("Create Column");
		createRight.setOnAction(e -> {
			columns.createNewSearchPanelAtEnd();
			// listener is used as columnsScroll's Hmax property doesn't update synchronously 
			ChangeListener<Number> listener = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2) {
					for (Node child : columnsScroll.getChildrenUnmodifiable()) {
						if (child instanceof ScrollBar) {
							ScrollBar scrollBar = (ScrollBar) child;
							if (scrollBar.getOrientation() == Orientation.HORIZONTAL &&
									scrollBar.visibleProperty().get()) {
								columnsScroll.setHvalue(columnsScroll.getHmax());
								break;
							}			
						}
					}
					columns.widthProperty().removeListener(this);
				}
			};
			columns.widthProperty().addListener(listener);
		});
		createRight.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

		MenuItem closeColumn = new MenuItem("Close Column");
		closeColumn.setOnAction(e -> columns.closeCurrentColumn());
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		cols.getItems().addAll(createRight, createLeft, closeColumn);
		return cols;
	}

	private MenuItem createDocumentationMenuItem() {
		MenuItem documentationMenuItem = new MenuItem("Documentation");
		documentationMenuItem.setOnAction((e) -> {
			MarkupPopup popup = createDescPopup();
			popup.show();
		});
		documentationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
		return documentationMenuItem;
	}
	
	private MarkupPopup createDescPopup(){
		MarkupPopup popup = new MarkupPopup("Done");
		ConfigFileHandler handler = new ConfigFileHandler();
		popup.setDisplayedText(handler.getDocumentationMarkup());
		return popup;
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
