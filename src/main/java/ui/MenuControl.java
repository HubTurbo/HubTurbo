package ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import prefs.Preferences;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.PanelControl;
import ui.issuepanel.FilterPanel;
import util.DialogMessage;
import util.Utility;
import util.events.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MenuControl extends MenuBar {

    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

    private final PanelControl panels;
    private final ScrollPane panelsScrollPane;
    private final UI ui;
    private final Preferences prefs;

    public MenuControl(UI ui, PanelControl panels, ScrollPane panelsScrollPane, Preferences prefs) {
        this.panels = panels;
        this.prefs = prefs;
        this.panelsScrollPane = panelsScrollPane;
        this.ui = ui;
        createMenuItems();
    }

    private void createMenuItems() {
        Menu file = createFileMenu();

        Menu newMenu = new Menu("New");
        newMenu.getItems().addAll(createNewMenuItems());

        Menu panels = createPanelsMenu();

        Menu boards = new Menu("Boards");
        boards.getItems().addAll(createBoardsMenu());

        Menu view = new Menu("View");
        view.getItems().addAll(
                createRefreshMenuItem(),
                createDocumentationMenuItem());

        getMenus().addAll(file, newMenu, panels, boards, view);
    }

    private Menu createFileMenu() {
        Menu file = new Menu("File");
        
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> {
            logger.info("Logging out of HT");
            prefs.setLastLoginCredentials("", "");
            ui.quit();
        });
        
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> {
            logger.info("Quitting HT");
            ui.quit();
        });
        
        file.getItems().addAll(logout, quit);
        
        return file;

    }

    private Menu createPanelsMenu() {
        Menu cols = new Menu("Panels");

        MenuItem createLeft = new MenuItem("Create (Left)");
        createLeft.setOnAction(e -> {
            logger.info("Menu: Panels > Create (Left)");
            panels.createNewPanelAtStart();
            setHvalue(panelsScrollPane.getHmin());
        });
        createLeft.setAccelerator(KeyboardShortcuts.CREATE_LEFT_PANEL);

        MenuItem createRight = new MenuItem("Create");
        createRight.setOnAction(e -> {
            logger.info("Menu: Panels > Create");
            panels.createNewPanelAtEnd();
            // listener is used as panelsScroll's Hmax property doesn't update
            // synchronously
            ChangeListener<Number> listener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                    for (Node child : panelsScrollPane.getChildrenUnmodifiable()) {
                        if (child instanceof ScrollBar) {
                            ScrollBar scrollBar = (ScrollBar) child;
                            if (scrollBar.getOrientation() == Orientation.HORIZONTAL
                                    && scrollBar.visibleProperty().get()) {
                                setHvalue(panelsScrollPane.getHmax());
                                break;
                            }
                        }
                    }
                    panels.widthProperty().removeListener(this);
                }
            };
            panels.widthProperty().addListener(listener);
        });
        createRight.setAccelerator(KeyboardShortcuts.CREATE_RIGHT_PANEL);

        MenuItem closePanel = new MenuItem("Close");
        closePanel.setOnAction(e -> {
            logger.info("Menu: Panels > Close");
            panels.closeCurrentPanel();
        });
        closePanel.setAccelerator(KeyboardShortcuts.CLOSE_PANEL);

        cols.getItems().addAll(createRight, createLeft, closePanel);
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
        dlg.getEditor().setId("boardnameinput");
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

        panels.closeAllPanels();
        panels.openPanelsWithFilters(filters);
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
        return panels.getChildren().stream().flatMap(c -> {
            if (c instanceof FilterPanel) {
                return Stream.of(((FilterPanel) c).getCurrentFilterString());
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
        documentationMenuItem.setAccelerator(new KeyCodeCombination(KeyboardShortcuts.SHOW_DOCS));
        return documentationMenuItem;
    }

    private MenuItem createRefreshMenuItem() {
        MenuItem refreshMenuItem = new MenuItem("Refresh");
        refreshMenuItem.setOnAction((e) -> {
            logger.info("Menu: View > Refresh");
            ui.logic.refresh();
        });
        refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyboardShortcuts.REFRESH));
        return refreshMenuItem;
    }

    private MenuItem[] createNewMenuItems() {
        MenuItem newIssueMenuItem = new MenuItem("Issue");
        newIssueMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Issue");
            ui.triggerEvent(new IssueCreatedEvent());
        });
        newIssueMenuItem.setAccelerator(KeyboardShortcuts.NEW_ISSUE);

        MenuItem newLabelMenuItem = new MenuItem("Label");
        newLabelMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Label");
            ui.triggerEvent(new LabelCreatedEvent());
        });
        newLabelMenuItem.setAccelerator(KeyboardShortcuts.NEW_LABEL);

        MenuItem newMilestoneMenuItem = new MenuItem("Milestone");
        newMilestoneMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Milestone");
            ui.triggerEvent(new MilestoneCreatedEvent());
        });
        newMilestoneMenuItem.setAccelerator(KeyboardShortcuts.NEW_MILESTONE);

        return new MenuItem[] { newIssueMenuItem, newLabelMenuItem, newMilestoneMenuItem };
    }

    public void scrollTo(int panelIndex, int numOfPanels){
        setHvalue(panelIndex * (panelsScrollPane.getHmax()) / (numOfPanels - 1));
    }

    private void setHvalue(double val) {
        panelsScrollPane.setHvalue(val);
    }
}
