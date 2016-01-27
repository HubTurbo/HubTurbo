package ui;


import backend.resource.TurboUser;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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
    public static final String SAMPLE = "Sample Board!";
    public static final String SAMPLE_REPO_NAME = "HubTurbo/SampleRepo";
    private static final int MAX_WORK_ALLOCATION_PANELS = 5;

    private final UI ui;
    private final PanelControl panelControl;
    private final Preferences prefs;

    public BoardAutoCreator(UI ui, PanelControl panelControl, Preferences prefs) {
        this.ui = ui;
        this.panelControl = panelControl;
        this.prefs = prefs;
    }

    public Menu generateBoardAutoCreateMenu() {
        Menu autoCreate = new Menu("Auto-create");

        MenuItem sample = new MenuItem(SAMPLE);
        sample.setOnAction(e -> createSampleBoard());
        autoCreate.getItems().add(sample);

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

        int noOfPanelsToBeGenerated = Math.min(MAX_WORK_ALLOCATION_PANELS, userList.size());

        List<PanelInfo> panelData = generatePanelInfoFromTurboUsers(userList.subList(0, noOfPanelsToBeGenerated),
                "Work allocated to %s", "assignee:%s sort:milestone,status");

        String boardName = Utility.getNameClosestToDesiredName(WORK_ALLOCATION, prefs.getAllBoardNames());

        createBoard(panelData, boardName);

        DialogMessage.showInformationDialog("Auto-create Board - " + WORK_ALLOCATION,
                WORK_ALLOCATION + " board has been created and loaded.\n\n" +
                        "It is saved under the name \"" + boardName + "\".");
    }

    private void createSampleBoard() {
        logger.info("Creating " + SAMPLE);

        panelControl.closeAllPanels();

        List<PanelInfo> panelData = new ArrayList<>();

        panelData.add(new PanelInfo("Open issues and PR's", SAMPLE_REPO_NAME + " " + "(is:issue OR is:pr) is:open"));
        panelData.add(new PanelInfo("V5 Milestone", SAMPLE_REPO_NAME + " " + "milestone:V5 sort:status"));
        panelData.add(new PanelInfo("Urgent issues assigned to Darius",
                SAMPLE_REPO_NAME + " " + "label:urgent assignee:dariusf"));

        createBoard(panelData, SAMPLE);

        panelControl.selectPanel(1); // current

        triggerBoardSaveEventSequence(SAMPLE);

        DialogMessage.showInformationDialog("Auto-create Board - " + SAMPLE,
                SAMPLE + " has been created and loaded.\n\n" +
                        "It is saved under the name \"" + SAMPLE + "\".");
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
