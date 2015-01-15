package ui;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import service.ServiceManager;
import storage.DataManager;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import util.DialogMessage;
import util.events.IssueCreatedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;
import util.events.PanelSavedEvent;
import util.events.PanelSavedEventHandler;


public class MenuControl extends MenuBar {

	private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

	private final ColumnControl columns;
	private final ScrollPane columnsScrollPane;
	private final UI ui;

	public MenuControl(UI ui, ColumnControl columns, ScrollPane columnsScrollPane) {
		this.columns = columns;
		this.columnsScrollPane = columnsScrollPane;
		this.ui = ui;
		createMenuItems();
	}

	private void createMenuItems() {
		Menu newMenu = new Menu("New");
		newMenu.getItems().addAll(createNewMenuItems());

		Menu panels = createPanelsMenu();

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createForceRefreshMenuItem(), createDocumentationMenuItem());

		getMenus().addAll(newMenu, panels, view);
	}

	private Menu createPanelsMenu() {
		Menu cols = new Menu("Panels");

		MenuItem createLeft = new MenuItem("Create (Left)");
		createLeft.setOnAction(e -> {
			logger.info("Menu: Panels > Create (Left)");
			columns.createNewPanelAtStart();
			columnsScrollPane.setHvalue(columnsScrollPane.getHmin());
		});
		createLeft.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN,
				KeyCombination.SHIFT_DOWN));

		MenuItem createRight = new MenuItem("Create");
		createRight.setOnAction(e -> {
			logger.info("Menu: Panels > Create");
			columns.createNewPanelAtEnd();
			// listener is used as columnsScroll's Hmax property doesn't update
			// synchronously
			ChangeListener<Number> listener = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					for (Node child : columnsScrollPane.getChildrenUnmodifiable()) {
						if (child instanceof ScrollBar) {
							ScrollBar scrollBar = (ScrollBar) child;
							if (scrollBar.getOrientation() == Orientation.HORIZONTAL
									&& scrollBar.visibleProperty().get()) {
								columnsScrollPane.setHvalue(columnsScrollPane.getHmax());
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

		MenuItem closeColumn = new MenuItem("Close");
		closeColumn.setOnAction(e -> {
			logger.info("Menu: Panels > Close");
			columns.closeCurrentColumn();
		});
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		Menu sets = new Menu("Sets");
		sets.getItems().addAll(createPanelsSetsMenu());
		
		cols.getItems().addAll(createRight, createLeft, closeColumn, sets);
		return cols;
	}

	private MenuItem[] createPanelsSetsMenu() {
		MenuItem save = new MenuItem("Save");
		save.setOnAction(e -> {
            List<String> filterExprs = getCurrentFilterExprs();
            
            if (!filterExprs.isEmpty()) {
            	Optional<String> response = Dialogs.create()
		            .title("Panel Set Name")
		            .lightweight()
		            .masthead("Please name this panel set")
		            .message("What should this panel set be called?").showTextInput();
                 
            	if (response.isPresent()) {
	            	DataManager.getInstance().addPanelSet(response.get(), filterExprs);
	            	ui.triggerEvent(new PanelSavedEvent());
            	}
            }
		});
		
		Menu open = new Menu("Open");
		Menu delete = new Menu("Delete");

		ui.registerEvent(new PanelSavedEventHandler() {
			@Override
			public void handle(PanelSavedEvent e) {
				open.getItems().clear();
				delete.getItems().clear();

				for (String panelSetName : DataManager.getInstance().getAllPanelSets().keySet()) {
					final List<String> filterSet = DataManager.getInstance().getAllPanelSets().get(panelSetName);
					MenuItem openItem = new MenuItem(panelSetName);
					openItem.setOnAction(e1 -> {
						columns.closeAllColumns();
						columns.openColumnsWithFilters(filterSet);
					});
					open.getItems().add(openItem);
					
					MenuItem deleteItem = new MenuItem(panelSetName);
					deleteItem.setOnAction(e1 -> {

						Action response = Dialogs
							.create()
							.title("Confirmation")
							.masthead("Delete panel set '" + panelSetName + "'?")
							.message("Are you sure you want to delete this panelSet?")
							.actions(new Action[] { Dialog.Actions.YES, Dialog.Actions.NO })
							.showConfirm();
						
						if (response == Dialog.Actions.YES) {
							DataManager.getInstance().removePanelSet(panelSetName);
							ui.triggerEvent(new PanelSavedEvent());
						}
					});
					delete.getItems().add(deleteItem);
				}
			}
		});

		return new MenuItem[] {save, open, delete};
	}

	private List<String> getCurrentFilterExprs() {
		return columns.getChildren().stream().flatMap(c -> {
			if (c instanceof IssueColumn) {
				return Stream.of(((IssueColumn) c).getCurrentFilterString());
			} else {
				return Stream.of();
			}
		}).collect(Collectors.toList());
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
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			protected Boolean call() throws IOException {
				try {
					logger.info("Menu: View > Force Refresh");
					ServiceManager.getInstance().stopModelUpdate();
					ServiceManager.getInstance().getModel().forceReloadComponents();
					ServiceManager.getInstance().restartModelUpdate();
				} catch (SocketTimeoutException e) {
					handleSocketTimeoutException(e);
					return false;
				} catch (UnknownHostException e) {
					handleUnknownHostException(e);
					return false;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
					return false;
				}
				logger.info("Menu: View > Force Refresh completed");
				return true;
			}

			private void handleSocketTimeoutException(Exception e) {
				Platform.runLater(() -> {
					logger.error(e.getMessage(), e);
					DialogMessage.showWarningDialog("Internet Connection is down",
							"Timeout while loading items from github. Please check your internet connection.");

				});
			}

			private void handleUnknownHostException(Exception e) {
				Platform.runLater(() -> {
					logger.error(e.getMessage(), e);
					DialogMessage.showWarningDialog("No Internet Connection",
							"Please check your internet connection and try again");
				});
			}
		};
		DialogMessage.showProgressDialog(task,
				"Reloading issues for current repo... This may take awhile, please wait.");
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

		return new MenuItem[] { newIssueMenuItem, newLabelMenuItem, newMilestoneMenuItem };
	}
}
