package ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.events.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ui.components.KeyboardShortcuts.*;

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

        Menu repos = new Menu("Repos");
        repos.getItems().addAll(createReposMenu());

        Menu view = new Menu("View");
        view.getItems().addAll(
                createRefreshMenuItem(),
                createDocumentationMenuItem());

        getMenus().addAll(file, newMenu, panels, boards, repos, view);
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
            panels.scrollToCurrentlySelectedPanel();
        });
        createLeft.setAccelerator(CREATE_LEFT_PANEL);

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
                                panels.scrollToCurrentlySelectedPanel();
                                break;
                            }
                        }
                    }
                    panels.widthProperty().removeListener(this);
                }
            };
            panels.widthProperty().addListener(listener);
        });
        createRight.setAccelerator(CREATE_RIGHT_PANEL);

        MenuItem closePanel = new MenuItem("Close");
        closePanel.setOnAction(e -> {
            logger.info("Menu: Panels > Close");
            panels.closeCurrentPanel();
        });
        closePanel.setAccelerator(CLOSE_PANEL);

        cols.getItems().addAll(createRight, createLeft, closePanel);
        return cols;
    }
    
    private void onBoardSave() {
        logger.info("Menu: Boards > Save");
        
        if (!prefs.getLastOpenBoard().isPresent()) {
            onBoardSaveAs();
            return;
        }
        
        List<PanelInfo> panels = getCurrentPanels();
        if (panels.isEmpty()) {
            logger.info("Did not save board " + prefs.getLastOpenBoard().get());
            return;
        }
        
        prefs.addBoard(prefs.getLastOpenBoard().get(), panels);
        ui.triggerEvent(new BoardSavedEvent());
        logger.info("Board " + prefs.getLastOpenBoard().get() + " saved");
    }

    /**
     * Called upon the Boards > Save as being clicked
     */
    private void onBoardSaveAs() {
        logger.info("Menu: Boards > Save as");

        List<PanelInfo> panels = getCurrentPanels();

        if (panels.isEmpty()) {
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
            String boardName = response.get().trim();
            if (isBoardNameValid(boardName)) {
                prefs.addBoard(boardName, panels);
                prefs.setLastOpenBoard(boardName);
                ui.triggerEvent(new BoardSavedEvent());
                logger.info("New board " + boardName + " saved");
                ui.updateTitle();
            }
        }
    }
    
    private boolean isBoardNameValid(String response) {
        if (response.equals("")) {
            logger.info("Did not save new board: Empty name");
            return false;
        }
        return true;
    }

    /**
     * Called upon the Boards > Open being clicked
     */
    private void onBoardOpen(String boardName, List<PanelInfo> panelInfo) {
        logger.info("Menu: Boards > Open > " + boardName);

        panels.closeAllPanels();
        panels.openPanels(panelInfo);
        panels.selectFirstPanel();
        prefs.setLastOpenBoard(boardName);
        ui.updateTitle();
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
            if (prefs.getLastOpenBoard().isPresent()) {
                if (prefs.getLastOpenBoard().get().equals(boardName)) {
                    prefs.clearLastOpenBoard();
                }
            }
            ui.triggerEvent(new BoardSavedEvent());
            logger.info(boardName + " was deleted");
            ui.updateTitle();
        } else {
            logger.info(boardName + " was not deleted");
        }
    }

    private MenuItem[] createBoardsMenu() {
        MenuItem saveAs = new MenuItem("Save as");
        saveAs.setOnAction(e -> onBoardSaveAs());
        
        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> onBoardSave());

        Menu open = new Menu("Open");
        Menu delete = new Menu("Delete");

        ui.registerEvent((BoardSavedEventHandler) e -> {
            open.getItems().clear();
            delete.getItems().clear();

            Map<String, List<PanelInfo>> boards = prefs.getAllBoards();
            
            for (Map.Entry<String, List<PanelInfo>> entry : boards.entrySet()) {
                final String boardName = entry.getKey();
                final List<PanelInfo> panelSet = entry.getValue();

                MenuItem openItem = new MenuItem(boardName);
                openItem.setOnAction(e1 -> onBoardOpen(boardName, panelSet));
                open.getItems().add(openItem);

                MenuItem deleteItem = new MenuItem(boardName);
                deleteItem.setOnAction(e1 -> onBoardDelete(boardName));
                delete.getItems().add(deleteItem);
            }
        });

        return new MenuItem[] {save, saveAs, open, delete};
    }

    /**
     * Returns the list of panel names and filters currently showing the user interface
     * @return
     */
    private List<PanelInfo> getCurrentPanels() {
        return panels.getChildren().stream().flatMap(c -> {
            if (c instanceof FilterPanel) {
                return Stream.of(((FilterPanel) c).getCurrentInfo());
            } else {
                return Stream.of();
            }
        }).collect(Collectors.toList());
    }
    
    public void switchBoard() {
        Optional<String> name = prefs.switchBoard();
        if (name.isPresent()) {
            onBoardOpen(name.get(), prefs.getBoardPanels(name.get()));
        }
    }

    private MenuItem createDocumentationMenuItem() {
        MenuItem documentationMenuItem = new MenuItem("Documentation");
        documentationMenuItem.setOnAction((e) -> {
            logger.info("Menu: View > Documentation");
            ui.getBrowserComponent().showDocs();
        });
        documentationMenuItem.setAccelerator(SHOW_DOCS);
        return documentationMenuItem;
    }

    private MenuItem createRefreshMenuItem() {
        MenuItem refreshMenuItem = new MenuItem("Refresh");
        refreshMenuItem.setOnAction((e) -> {
            logger.info("Menu: View > Refresh");
            if (ui.isNotificationPaneShowing()) {
                // we trigger the notification timeout action first before refreshing
                ui.triggerNotificationTimeoutAction();
            }
            ui.logic.refresh(false);
        });
        refreshMenuItem.setAccelerator(REFRESH);
        return refreshMenuItem;
    }

    private MenuItem[] createNewMenuItems() {
        MenuItem newIssueMenuItem = new MenuItem("Issue");
        newIssueMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Issue");
            ui.triggerEvent(new IssueCreatedEvent());
        });
        newIssueMenuItem.setAccelerator(NEW_ISSUE);

        MenuItem newLabelMenuItem = new MenuItem("Label");
        newLabelMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Label");
            ui.triggerEvent(new LabelCreatedEvent());
        });
        newLabelMenuItem.setAccelerator(NEW_LABEL);

        MenuItem newMilestoneMenuItem = new MenuItem("Milestone");
        newMilestoneMenuItem.setOnAction(e -> {
            logger.info("Menu: New > Milestone");
            ui.triggerEvent(new MilestoneCreatedEvent());
        });
        newMilestoneMenuItem.setAccelerator(NEW_MILESTONE);

        return new MenuItem[] { newIssueMenuItem, newLabelMenuItem, newMilestoneMenuItem };
    }

    private MenuItem[] createReposMenu() {
        Menu remove = new Menu("Remove");

        ui.registerEvent((OpenReposChangedEventHandler) e -> {
            Platform.runLater(() -> updateRepoRemoveList(remove));
        });

        return new MenuItem[] { remove };
    }

    private void updateRepoRemoveList(Menu remove) {
        remove.getItems().clear();

        HashSet<String> currentlyUsedRepo = getCurrentlyUsedRepo();

        for (String repoId : ui.logic.getStoredRepos()) {
            if (!currentlyUsedRepo.contains(repoId)) {
                MenuItem removeItem = new MenuItem(repoId);
                removeItem.setOnAction(e1 -> onRepoRemove(repoId));
                remove.getItems().add(removeItem);
            }
        }
    }

    private HashSet<String> getCurrentlyUsedRepo() {
        HashSet<String> currentlyUsedRepos = new HashSet<>();
        String defaultRepo = ui.logic.getDefaultRepo();
        currentlyUsedRepos.add(defaultRepo);
        currentlyUsedRepos.addAll(panels.getRepositoriesReferencedOnAllPanels());

        return currentlyUsedRepos;
    }

    private void onRepoRemove(String repoId) {
        ui.logic.removeRepository(repoId).thenRun(() -> ui.triggerEvent(new OpenReposChangedEvent()));
    }
}
