package ui;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;
import ui.issuecolumn.ColumnControl;
import util.DialogMessage;
import util.events.IssueCreatedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;

public class MenuControl extends MenuBar {

	private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

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
			logger.info("Menu: View > Columns > Create Column (Left)");
			columns.createNewPanelAtStart();
			columnsScroll.setHvalue(columnsScroll.getHmin());
		});
		createLeft.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

		MenuItem createRight = new MenuItem("Create Column");
		createRight.setOnAction(e -> {
			logger.info("Menu: View > Columns > Create Column");
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
		closeColumn.setOnAction(e -> {
			logger.info("Menu: View > Columns > Close Column");
			columns.closeCurrentColumn();
		});
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		cols.getItems().addAll(createRight, createLeft, closeColumn);
		return cols;
	}

	private MenuItem createDocumentationMenuItem() {
		MenuItem documentationMenuItem = new MenuItem("Documentation");
		documentationMenuItem.setOnAction((e) -> {
			logger.info("Menu: View > Documentation");
			ui.getBrowserComponent().showDocs();
		});
		documentationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
		return documentationMenuItem;
	}
	
	private MenuItem createRefreshMenuItem() {
		MenuItem refreshMenuItem = new MenuItem("Refresh");
		refreshMenuItem.setOnAction((e) -> {
			logger.info("Menu: View > Refresh");
			ServiceManager.getInstance().restartModelUpdate();
			columns.refresh();
		});
		refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		return refreshMenuItem;
	}
	
	private MenuItem createForceRefreshMenuItem() {
		MenuItem forceRefreshMenuItem = new MenuItem("Force Refresh");
		forceRefreshMenuItem.setOnAction((e) -> {
			triggerForceRefreshProgressDialog();
		});
		
		forceRefreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.CONTROL_DOWN));
		return forceRefreshMenuItem;
	}
	
	private void triggerForceRefreshProgressDialog() {
		Task<Boolean> task = new Task<Boolean>(){
			@Override
			protected Boolean call() throws IOException {
				try {
					logger.info("Menu: View > Force Refresh");
					ServiceManager.getInstance().stopModelUpdate();
					ServiceManager.getInstance().getModel().forceReloadComponents();
					ServiceManager.getInstance().restartModelUpdate();
				} catch(SocketTimeoutException e) {
					handleSocketTimeoutException();
					return false;
				} catch(UnknownHostException e) {
					handleUnknownHostException();
					return false;
				} catch (Exception e) {
					logger.error("Menu: View > Force Refresh unsuccessful due to " + e);
					e.printStackTrace();
					return false;
				}
				logger.info("Menu: View > Force Refresh completed");
				return true;
			}

			private void handleSocketTimeoutException() {
				Platform.runLater(()->{
					logger.error("Menu: View > Force Refresh unsuccessful due to SocketTimeoutException");
					DialogMessage.showWarningDialog("Internet Connection is down", 
							"Timeout while loading items from github. Please check your internet connection.");
					
				});
			}

			private void handleUnknownHostException() {
				Platform.runLater(()->{
					logger.error("Menu: View > Force Refresh unsuccessful due to UnknownHostException");
					DialogMessage.showWarningDialog("No Internet Connection", 
							"Please check your internet connection and try again");
				});
			}
		};
		DialogMessage.showProgressDialog(task, "Reloading issues for current repo... This may take awhile, please wait.");
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private MenuItem[] createNewMenuItems() {
		MenuItem newIssueMenuItem = new MenuItem("Issue");
		newIssueMenuItem.setOnAction(e -> {
			logger.info("Menu: New > Issue");
			ui.triggerEvent(new IssueCreatedEvent());
		});
		newIssueMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));

		MenuItem newLabelMenuItem = new MenuItem("Label");
		newLabelMenuItem.setOnAction(e -> {
			logger.info("Menu: New > Label");
			ui.triggerEvent(new LabelCreatedEvent());
		});
		newLabelMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));

		MenuItem newMilestoneMenuItem = new MenuItem("Milestone");
		newMilestoneMenuItem.setOnAction(e -> {
			logger.info("Menu: New > Milestone");
			ui.triggerEvent(new MilestoneCreatedEvent());
		});
		newMilestoneMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));

		return new MenuItem[] {newIssueMenuItem, newLabelMenuItem, newMilestoneMenuItem};
	}
}
