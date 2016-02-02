package ui;


import backend.resource.TurboUser;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.DialogMessage;
import util.Utility;
import util.events.BoardSavedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoardAutoCreator {
    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());
    private static final String MILESTONES = "Milestones";
    private static final String WORK_ALLOCATION = "Work Allocation";
    private static final int MAX_WORK_ALLOCATION_PANELS = 5;

    private final UI ui;
    private final PanelControl panelControl;
    private final Preferences prefs;
    private final Stage stage;

    public BoardAutoCreator(UI ui, PanelControl panelControl, Preferences prefs, Stage stage) {
        this.ui = ui;
        this.panelControl = panelControl;
        this.prefs = prefs;
        this.stage = stage;
    }

    public Menu generateBoardAutoCreateMenu() {
        Menu autoCreate = new Menu("Auto-create");

        MenuItem milestone = new MenuItem(MILESTONES);
        milestone.setOnAction(e -> createMilestoneBoard());
        autoCreate.getItems().add(milestone);

        MenuItem workAllocation = new MenuItem(WORK_ALLOCATION);
        workAllocation.setOnAction(e -> createWorkAllocationBoard());
        autoCreate.getItems().add(workAllocation);

        return autoCreate;
    }

    private void createMilestoneBoard() {
        logger.info("Creating " + MILESTONES + " board");

        panelControl.closeAllPanels();

        List<PanelInfo> panelData = new ArrayList<>();

        panelData.add(new PanelInfo("Previous Milestone", "milestone:curr-1 sort:status"));
        panelData.add(new PanelInfo("Current Milestone", "milestone:curr sort:status"));
        panelData.add(new PanelInfo("Next Milestone", "milestone:curr+1 sort:status"));
        panelData.add(new PanelInfo("Next Next Milestone", "milestone:curr+2 sort:status"));
        panelData.add(new PanelInfo("Next Next Next Milestone", "milestone:curr+3 sort:status"));

        String boardName = Utility.getNameClosestToDesiredName(MILESTONES, prefs.getAllBoardNames());

        createBoard(panelData, boardName);

        panelControl.selectPanel(1); // current

        triggerBoardSaveEventSequence(boardName);

        DialogMessage.showInformationDialog("Auto-create Board - " + MILESTONES,
                MILESTONES + " board has been created and loaded.\n\n" +
                        "It is saved under the name \"" + boardName + "\".");
    }

    private void createWorkAllocationBoard() {
        logger.info("Creating " + WORK_ALLOCATION + " board");

        List<TurboUser> userList = ui.logic.getRepo(ui.logic.getDefaultRepo()).getUsers();

        if (userList.isEmpty()) {
            DialogMessage.showInformationDialog("Auto-create Board - " + WORK_ALLOCATION,
                    WORK_ALLOCATION + " board cannot be created as your" +
                            " account has no push access to the default repo.");

            return;
        }
        List<ComboBox<TurboUser>> listOfComboBoxes = new ArrayList<ComboBox<TurboUser>>();

        ComboBox<Integer> comboBox = new ComboBox<>();
        Dialog dlg = new Dialog<>();
        DialogPane dlgPane = new DialogPane();
        dlgPane.setPrefWidth(200);
        dlg.setWidth(200);
        dlg.setDialogPane(dlgPane);
        HBox assigneeSelector = new HBox(5);
        assigneeSelector.setAlignment(Pos.CENTER_LEFT);
        assigneeSelector.setPrefWidth(100);
        dlgPane.setContent(assigneeSelector);
        assigneeSelector.getChildren().add(comboBox);

        int noOfUsers = (userList.size() > 10) ? 10 : userList.size();
        for (int j = 0; j < noOfUsers; j++) {
            comboBox.getItems().addAll(j+1);
        }

        comboBox.valueProperty().addListener((observable, oldVal, newVal) -> {
            generateUserComboBoxes(oldVal, newVal, userList, comboBox, assigneeSelector, listOfComboBoxes, dlgPane, dlg);
        });

        dlg.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                List<TurboUser> selectedUserList = new ArrayList<>();
                for (int i = 0; i < listOfComboBoxes.size(); i++) {
                    selectedUserList.add(listOfComboBoxes.get(i).getValue());
                }

                generatePanels(selectedUserList);
            }
        });

        dlgPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.initOwner(stage);
        dlg.showAndWait();
    }

    private void generatePanels(List<TurboUser> userList) {
        System.out.println("generating panels!");
        int noOfPanelsToBeGenerated = Math.min(MAX_WORK_ALLOCATION_PANELS, userList.size());

        List<PanelInfo> panelData = generatePanelInfoFromTurboUsers(userList.subList(0, noOfPanelsToBeGenerated),
                "Work allocated to %s", "assignee:%s sort:milestone,status");

        String boardName = Utility.getNameClosestToDesiredName(WORK_ALLOCATION, prefs.getAllBoardNames());

        createBoard(panelData, boardName);

        DialogMessage.showInformationDialog("Auto-create Board - " + WORK_ALLOCATION,
                WORK_ALLOCATION + " board has been created and loaded.\n\n" +
                        "It is saved under the name \"" + boardName + "\".");
    }

    private void generateUserComboBoxes(Integer oldNoOfUsers, int noOfUsers, List<TurboUser> userList, ComboBox<Integer> comboBox, HBox assigneeSelector, List<ComboBox<TurboUser>> listOfComboBoxes, DialogPane dlgPane, Dialog dlg) {
        if (oldNoOfUsers == null) oldNoOfUsers = 0;
        if (noOfUsers > oldNoOfUsers) {
            for (int i = oldNoOfUsers; i < noOfUsers; i++) {
                ComboBox<TurboUser> userComboBox = new ComboBox<>();
                userComboBox.getItems().addAll(userList); // fill selector
                assigneeSelector.getChildren().add(userComboBox);
                userComboBox.setPrefWidth(100);
                dlg.setWidth(dlgPane.getWidth()+105);
                listOfComboBoxes.add(userComboBox);
            }
        } else {
            for (int i = assigneeSelector.getChildren().size() - 1; i >= noOfUsers+1; i--) {
                assigneeSelector.getChildren().remove(i);
            }
        }
    }

    private List<PanelInfo> generatePanelInfoFromTurboUsers(List<TurboUser> users,
                                                            String nameTemplate, String filterTemplate) {
        return users.stream()
                .map(user -> new PanelInfo(String.format(nameTemplate, user.getFriendlierName()),
                        String.format(filterTemplate, user.getLoginName())))
                .collect(Collectors.toList());
    }

    // TODO: extract out once #1074 (abstract board functionality from menu control) is done
    private void createBoard(List<PanelInfo> panelData, String boardName) {
        panelControl.closeAllPanels();

        FilterPanel panelAdded;

        for (PanelInfo panelInfo : panelData) {
            panelAdded = panelControl.addPanelAt(panelControl.getPanelCount());
            panelAdded.setPanelName(panelInfo.getPanelName());
            panelAdded.setFilterByString(panelInfo.getPanelFilter());
        }

        panelControl.selectFirstPanel();

        triggerBoardSaveEventSequence(boardName);
    }

    private void triggerBoardSaveEventSequence(String boardName) {
        prefs.addBoard(boardName, panelControl.getCurrentPanelInfos());
        prefs.setLastOpenBoard(boardName);
        TestController.getUI().triggerEvent(new BoardSavedEvent());
        logger.info("Auto-created board, saved as \"" + boardName + "\"");
        TestController.getUI().updateTitle();
    }
}
