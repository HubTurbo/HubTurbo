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
import util.events.IssueCreatedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;

public class MenuControl extends MenuBar {
	private final ColumnControl columns;
	private final ScrollPane columnsScroll;
	private final UI ui;

	public MenuControl(UI ui, ColumnControl columns, ScrollPane columnsScroll) {
		this.columns = columns;
		this.columnsScroll = columnsScroll;
		this.ui = ui;
		createMenuItems();
	}
	
	private void createMenuItems() {
		Menu newMenu = new Menu("New");
		newMenu.getItems().addAll(createNewMenuItems());

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createForceRefreshMenuItem(), createColumnsMenuItem(), createDocumentationMenuItem());

		getMenus().addAll(newMenu, view);
	}


	private MenuItem createColumnsMenuItem() {
		Menu cols = new Menu("Columns");

		MenuItem createLeft = new MenuItem("Create Column (Left)");
		createLeft.setOnAction(e -> {
			columns.createNewPanelAtStart();
			columnsScroll.setHvalue(columnsScroll.getHmin());
		});
		createLeft.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

		MenuItem createRight = new MenuItem("Create Column");
		createRight.setOnAction(e -> {
			columns.createNewPanelAtEnd();
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
		createRight.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));

		MenuItem closeColumn = new MenuItem("Close Column");
		closeColumn.setOnAction(e -> columns.closeCurrentColumn());
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		cols.getItems().addAll(createRight, createLeft, closeColumn);
		return cols;
	}

	private MenuItem createDocumentationMenuItem() {
		MenuItem documentationMenuItem = new MenuItem("Documentation");
		documentationMenuItem.setOnAction((e) -> {
			ui.getBrowserComponent().showDocs();
		});
		documentationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
		return documentationMenuItem;
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
	
	private MenuItem createForceRefreshMenuItem() {
		MenuItem forceRefreshMenuItem = new MenuItem("Force Refresh");
		forceRefreshMenuItem.setOnAction((e) -> {
			try {
				ServiceManager.getInstance().stopModelUpdate();
				ServiceManager.getInstance().getModel().forceReloadComponents();
				ServiceManager.getInstance().restartModelUpdate();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		forceRefreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.CONTROL_DOWN));
		return forceRefreshMenuItem;
	}

	private MenuItem[] createNewMenuItems() {
		MenuItem newIssueMenuItem = new MenuItem("Issue");
		newIssueMenuItem.setOnAction(e -> ui.triggerEvent(new IssueCreatedEvent()));
		newIssueMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));

		MenuItem newLabelMenuItem = new MenuItem("Label");
		newLabelMenuItem.setOnAction(e -> ui.triggerEvent(new LabelCreatedEvent()));
		newLabelMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));

		MenuItem newMilestoneMenuItem = new MenuItem("Milestone");
		newMilestoneMenuItem.setOnAction(e -> ui.triggerEvent(new MilestoneCreatedEvent()));
		newMilestoneMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));

		return new MenuItem[] {newIssueMenuItem, newLabelMenuItem, newMilestoneMenuItem};
	}
}
