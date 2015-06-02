package ui;

//import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import util.DialogMessage;
import util.PlatformEx;
import util.events.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MenuControl extends MenuBar {

	private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

	private final ColumnControl columns;
	private final ScrollPane columnsScrollPane;
	private final UI ui;
	private final Preferences prefs;

	public MenuControl(UI ui, ColumnControl columns, ScrollPane columnsScrollPane, Preferences prefs) {
		this.columns = columns;
		this.prefs = prefs;
		this.columnsScrollPane = columnsScrollPane;
		this.ui = ui;
		createMenuItems();
	}

	private void createMenuItems() {
		Menu newMenu = new Menu("New");
		newMenu.getItems().addAll(createNewMenuItems());

		Menu panels = createPanelsMenu();

		Menu boards = new Menu("Boards");
		boards.getItems().addAll(createBoardsMenu());

		Menu view = new Menu("View");
		view.getItems().addAll(createRefreshMenuItem(), createForceRefreshMenuItem(), createDocumentationMenuItem());
		
		Menu preferences = new Menu("Preferences");
		preferences.getItems().add(createPreferencesMenu());
		
		getMenus().addAll(newMenu, panels, boards, view, preferences);
	}

	private MenuItem createPreferencesMenu() {
		MenuItem logout = new MenuItem("Logout");
		logout.setOnAction(e -> {
			logger.info("Logging out of HT");
//			DataManager.getInstance().setLastLoginPassword("");
			ui.quit();
		});
		return logout;

	}

	private Menu createPanelsMenu() {
		Menu cols = new Menu("Panels");

		MenuItem createLeft = new MenuItem("Create (Left)");
		createLeft.setOnAction(e -> {
			logger.info("Menu: Panels > Create (Left)");
			columns.createNewPanelAtStart();
			setHvalue(columnsScrollPane.getHmin());
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
								setHvalue(columnsScrollPane.getHmax());
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

		MenuItem closeColumn = new MenuItem("Close");
		closeColumn.setOnAction(e -> {
			logger.info("Menu: Panels > Close");
			columns.closeCurrentColumn();
		});
		closeColumn.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		cols.getItems().addAll(createRight, createLeft, closeColumn);
		return cols;
	}

	/**
	 * Called upon the Boards > Save being clicked
	 */
	private void onBoardSave() {
		logger.info("Menu: Boards > Save");

		List<String> filterStrings = getCurrentFilterExprs();

	    if (filterStrings.isEmpty()) {
		    logger.info("Did not save new board");
		    return;
	    }

		TextInputDialog dlg = new TextInputDialog("");
		dlg.setTitle("Board Name");
		dlg.getDialogPane().setContentText("What should this board be called?");
		dlg.getDialogPane().setHeaderText("Please name this board");
		Optional<String> response = dlg.showAndWait();

		if (response.isPresent()) {
			prefs.addBoard(response.get(), filterStrings);
			ui.triggerEvent(new BoardSavedEvent());
			logger.info("New board" + response.get() + " saved, containing " + filterStrings);
		}
	}

	/**
	 * Called upon the Boards > Open being clicked
	 */
	private void onBoardOpen(String boardName, List<String> filters) {
		logger.info("Menu: Boards > Open > " + boardName);

		columns.closeAllColumns();
		columns.openColumnsWithFilters(filters);
	}

	/**
	 * Called upon the Boards > Delete being clicked
	 */
	private void onBoardDelete(String boardName) {
		logger.info("Menu: Boards > Delete > " + boardName);

        Alert dlg = new Alert(AlertType.CONFIRMATION, "");
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Confirmation");
		dlg.getDialogPane().setHeaderText("Delete board '" + boardName + "'?");
        dlg.getDialogPane().setContentText("Are you sure you want to delete this board?");
        Optional<ButtonType> response = dlg.showAndWait();

        if (response.isPresent() && response.get().getButtonData() == ButtonData.OK_DONE) {
			prefs.removeBoard(boardName);
			ui.triggerEvent(new BoardSavedEvent());
			logger.info(boardName + " was deleted");
        } else {
			logger.info(boardName + " was not deleted");
        }
	}

	private MenuItem[] createBoardsMenu() {
		MenuItem save = new MenuItem("Save");
		save.setOnAction(e -> onBoardSave());

		Menu open = new Menu("Open");
		Menu delete = new Menu("Delete");

		ui.registerEvent((BoardSavedEventHandler) e -> {
			open.getItems().clear();
			delete.getItems().clear();

			Map<String, List<String>> boards = prefs.getAllBoards();

			for (final String boardName : boards.keySet()) {
				final List<String> filterSet = boards.get(boardName);

				MenuItem openItem = new MenuItem(boardName);
				openItem.setOnAction(e1 -> onBoardOpen(boardName, filterSet));
				open.getItems().add(openItem);

				MenuItem deleteItem = new MenuItem(boardName);
				deleteItem.setOnAction(e1 -> onBoardDelete(boardName));
				delete.getItems().add(deleteItem);
			}
		});

		return new MenuItem[] {save, open, delete};
	}

	/**
	 * Returns the list of filter strings currently showing the user interface
	 * @return
	 */
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
			ui.logic.refresh();
		});
		refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
		return refreshMenuItem;
	}

	private MenuItem createForceRefreshMenuItem() {
		MenuItem forceRefreshMenuItem = new MenuItem("Force Refresh");
		forceRefreshMenuItem.setOnAction((e) -> triggerForceRefreshProgressDialog());

		forceRefreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.CONTROL_DOWN));
		return forceRefreshMenuItem;
	}

	private void triggerForceRefreshProgressDialog() {
		Task<Boolean> task = new Task<Boolean>() {
			@Override
			protected Boolean call() throws IOException {
				try {
					logger.info("Menu: View > Force Refresh");

					updateProgress(0, 1);
//					updateMessage(String.format("Reloading %s...",
//						ServiceManager.getInstance().getRepoId().generateId()));

					// TODO
//					ServiceManager.getInstance().forceRefresh((message, progress) -> {
//						updateProgress(progress * 100, 100);
//						updateMessage(message);
//					});
                    PlatformEx.runAndWait(columns::recreateColumns);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return false;
				}
				logger.info("Menu: View > Force Refresh completed");
				return true;
			}

//			private void handleSocketTimeoutException(Exception e) {
//				Platform.runLater(() -> {
//					logger.error(e.getMessage(), e);
//					DialogMessage.showWarningDialog("Internet Connection is down",
//							"Timeout while loading items from github. Please check your internet connection.");
//
//				});
//			}

//			private void handleUnknownHostException(Exception e) {
//				Platform.runLater(() -> {
//					logger.error(e.getMessage(), e);
//					DialogMessage.showWarningDialog("No Internet Connection",
//							"Please check your internet connection and try again");
//				});
//			}
		};
		DialogMessage.showProgressDialog(task, "Reloading repoistory...");
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
	
	public void scrollTo(int columnIndex, int NumOfColumns){
		setHvalue(columnIndex * (columnsScrollPane.getHmax()) / (NumOfColumns - 1));
	}
	private void setHvalue(double val) {
		columnsScrollPane.setHvalue(val);
	}

}
