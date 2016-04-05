package ui;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.issuepanel.PanelControl;
import util.Utility;
import util.events.*;

import java.util.*;
import java.util.stream.Collectors;

import static ui.components.KeyboardShortcuts.*;

public class MenuControl extends MenuBar {

    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

    private final PanelControl panels;
    private final UI ui;
    private final Preferences prefs;
    private final Stage mainStage;
    private final BoardAutoCreator boardAutoCreator;
    private final PanelMenuCreator panelMenuCreator;

    public MenuControl(UI ui, PanelControl panels, ScrollPane panelsScrollPane, Preferences prefs, Stage mainStage) {
        this.panels = panels;
        this.prefs = prefs;
        this.ui = ui;
        this.mainStage = mainStage;
        this.boardAutoCreator = new BoardAutoCreator(ui, panels, prefs);
        this.panelMenuCreator = new PanelMenuCreator(panels, panelsScrollPane);

        createMenuItems();
    }

    private void createMenuItems() {
        Menu file = createFileMenu();

        Menu newMenu = new Menu("New");
        newMenu.getItems().addAll(createNewMenuItems());

        Menu panels = panelMenuCreator.generatePanelMenu();

        Menu boards = new Menu("Boards");
        boards.getItems().addAll(createBoardsMenu());

        Menu repos = new Menu("Repos");
        repos.getItems().addAll(createReposMenu());

        Menu view = new Menu("View");
        view.getItems().addAll(
                createRefreshMenuItem(),
                createDocumentationMenuItem());

        Menu update = createUpdateMenu();

        getMenus().addAll(file, newMenu, panels, boards, repos, view, update);
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

    /**
     * Checks whether the current open board has unsaved changes
     * @return {@code true} if current open board is dirty, {@code false} otherwise.
     */
    public final boolean isCurrentOpenBoardDirty() {
        if (!prefs.getLastOpenBoardPanelInfos().isPresent()) {
            return true;
        }
        return !panels.getCurrentPanelInfos().equals(prefs.getLastOpenBoardPanelInfos().get());
    }

    /**
     * Prompts a {@code ConfirmChangesDialog} to ask the user whether or not to save the unsaved changes
     * in the current board.
     */
    public final void promptToSaveCurrentBoard() {
        ConfirmChangesDialog dlg = new ConfirmChangesDialog(mainStage);
        Optional<ButtonType> response = dlg.showAndWait();
        if (response.get() != ButtonType.YES) {
            logger.info("User abandoned unsaved changes.");
            return;
        }

        saveBoard();
        logger.info("Changes to the current board saved.");
    }

    /**
     * Prompts a {@code BoardNameDialog} to ask the user for a new board name
     * @return the user's response
     */
    public final Optional<String> promptForNewBoardName() {
        BoardNameDialog dlg = new BoardNameDialog(prefs, mainStage);
        Optional<String> response = dlg.showAndWait();
        ui.showMainStage();
        panels.selectFirstPanel();
        return response;
    }

    /**
     * Logs the user action and creates a new board
     */
    private void onBoardNew() {
        logger.info("Menu: Boards > New");
        if (isCurrentOpenBoardDirty()) {
            promptToSaveCurrentBoard();
        }

        newBoard();
    }

    /**
     * Creates an empty board. User will be prompted to
     * confirm the action if there are unclosed panels
     */
    public final void newBoard() {
        Optional<String> response = promptForNewBoardName();

        response.ifPresent(boardName -> {
            List<PanelInfo> panelList = new ArrayList<>();
            panelList.add(new PanelInfo("Panel", ""));

            createAndOpenNewBoard(boardName, panelList);
        });
    }

    /**
     * Logs the user action and saves the current board
     */
    private void onBoardSave() {
        logger.info("Menu: Boards > Save");
        saveBoard();
    }

    /**
     * Tries to save current board. If current board has not been saved before, it calls {@code saveBoardAs()}
     */
    public final void saveBoard() {
        if (!prefs.getLastOpenBoard().isPresent()) {
            saveBoardAs();
            return;
        }

        List<PanelInfo> panelList = panels.getCurrentPanelInfos();
        if (panelList.isEmpty()) {
            logger.info("Did not save board " + prefs.getLastOpenBoard().get());
            return;
        }

        prefs.addBoard(prefs.getLastOpenBoard().get(), panelList);
        prefs.setLastOpenBoardPanelInfos(panelList);
        ui.triggerEvent(new BoardSavedEvent());
        logger.info("Board " + prefs.getLastOpenBoard().get() + " saved");
    }

    /**
     * Logs the user action and triggers "Save as" on the current board
     */
    private void onBoardSaveAs() {
        logger.info("Menu: Boards > Save as");
        saveBoardAs();
    }

    /**
     * Prompts a BoardNameDialog and saves the current board based on its response
     */
    public final void saveBoardAs() {
        Optional<String> response = promptForNewBoardName();

        response.ifPresent(boardName -> {
            createAndOpenNewBoard(boardName, panels.getCurrentPanelInfos());
        });
    }

    /**
     * Creates a new board with the given board name and panel infos, then open that new board
     * @param boardName
     * @param panelList
     */
    public final void createAndOpenNewBoard(String boardName, List<PanelInfo> panelList) {
        prefs.addBoard(boardName, panelList);
        prefs.setLastOpenBoard(boardName);
        prefs.setLastOpenBoardPanelInfos(panelList);
        openBoard(boardName, panelList);

        ui.triggerEvent(new BoardSavedEvent());
        logger.info("New board " + boardName + " created and opened");
        ui.updateTitle();
    }

    /**
     * Logs the user action and opens the board that user requested
     */
    private void onBoardOpen(String boardName, List<PanelInfo> panelInfos) {
        logger.info("Menu: Boards > Open > " + boardName);
        openBoard(boardName, panelInfos);
    }

    /**
     * Opens the board named {@code boardName}.
     *
     * @param boardName name of the board
     * @param panelInfos list of panel infos of the board
     */
    public final void openBoard(String boardName, List<PanelInfo> panelInfos) {
        panels.closeAllPanels();
        panels.openPanels(panelInfos);
        panels.selectFirstPanel();
        prefs.setLastOpenBoard(boardName);
        prefs.setLastOpenBoardPanelInfos(panelInfos);
        ui.updateTitle();

        ui.triggerEvent(new UsedReposChangedEvent());
    }

    /**
     * Logs the user action and deletes the board that user requested
     */
    private void onBoardDelete(String boardName) {
        logger.info("Menu: Boards > Delete > " + boardName);
        deleteBoard(boardName);
    }

    /**
     * Deletes the board named {@boardName}
     *
     * @param boardName name of the board to delete
     */
    public final void deleteBoard(String boardName) {
        Alert dlg = new Alert(AlertType.CONFIRMATION, "");
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Confirmation");
        dlg.getDialogPane().setHeaderText("Delete board '" + boardName + "'?");
        dlg.getDialogPane().setContentText("Are you sure you want to delete this board?");
        Optional<ButtonType> response = dlg.showAndWait();

        if (response.isPresent() && response.get().getButtonData() == ButtonData.OK_DONE) {
            prefs.removeBoard(boardName);
            if (prefs.getLastOpenBoard().isPresent() &&
                    prefs.getLastOpenBoard().get().equals(boardName)) {

                prefs.clearLastOpenBoard();
                prefs.clearLastOpenBoardPanelInfos();
            }
            ui.triggerEvent(new BoardSavedEvent());
            logger.info(boardName + " was deleted");
            ui.updateTitle();
        } else {
            logger.info(boardName + " was not deleted");
        }
    }

    private MenuItem[] createBoardsMenu() {
        MenuItem newBoard = new MenuItem("New");
        newBoard.setOnAction(e -> onBoardNew());

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

        Menu autoCreate = boardAutoCreator.generateBoardAutoCreateMenu();

        return new MenuItem[] { newBoard, save, saveAs, open, delete, autoCreate };
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
                ui.hideNotification();
            }
            ui.logic.refresh();
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
        MenuItem choose = new MenuItem("Show Repository Picker");

        ui.registerEvent((UnusedStoredReposChangedEventHandler) e -> {
            Platform.runLater(() -> updateRepoRemoveList(remove));
        });

        choose.setAccelerator(SHOW_REPO_PICKER);
        choose.setOnAction(e -> ui.triggerEvent(new ShowRepositoryPickerEvent()));

        return new MenuItem[] { remove, choose };
    }

    private void updateRepoRemoveList(Menu remove) {
        remove.getItems().clear();

        Set<String> currentlyUsedRepos = Utility.convertSetToLowerCase(ui.getCurrentlyUsedRepos());
        Set<String> removableRepos = ui.logic.getStoredRepos()
                .stream().filter(repoId -> !currentlyUsedRepos.contains(repoId.toLowerCase()))
                .collect(Collectors.toSet());

        for (String repoId : removableRepos) {
            MenuItem removeItem = new MenuItem(repoId);
            removeItem.setOnAction(e1 -> onRepoRemove(repoId));
            remove.getItems().add(removeItem);
        }

        remove.getItems().add(new SeparatorMenuItem());

        // Supposedly, we would like the menu not to close when the disabled MenuItem-s
        // below are clicked. But this is a JDK bug; we can use CustomMenuItem.setHideOnClick(false)
        // if we want to. The bug is that it only works for ContextMenu and not Menu (which
        // we are using).
        for (String usedRepoId : ui.getCurrentlyUsedRepos()) {
            MenuItem disabledRemoveItem = new MenuItem(usedRepoId + " [in use, not removable]");
            disabledRemoveItem.setDisable(true);
            remove.getItems().add(disabledRemoveItem);
        }

    }

    private void onRepoRemove(String repoId) {
        ui.logic.removeStoredRepository(repoId).thenRun(() -> ui.triggerEvent(new UnusedStoredReposChangedEvent()));
    }

    private Menu createUpdateMenu() {
        Menu update = new Menu("Update");

        MenuItem checkProgress = new MenuItem("Check progress...");

        checkProgress.setOnAction(e -> {
            ui.showUpdateProgressWindow();
        });

        update.getItems().addAll(checkProgress);

        return update;
    }
}
