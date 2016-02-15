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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BoardAutoCreator {
    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());
    private static final String MILESTONES = "Milestones";
    private static final String WORK_ALLOCATION = "Work Allocation";
    private static final int MAX_WORK_ALLOCATION_PANELS = 5;
    public static final String SAMPLE_BOARD = "Sample Board!";
    private static final String FIRST_SAMPLE_REPO_NAME = "HubTurbo/SampleRepo";
    private static final String SECOND_SAMPLE_REPO_NAME = "HubTurbo/SampleRepo2";
    public static final String SAMPLE_BOARD_DIALOG = String.format("%s has been created and loaded.", SAMPLE_BOARD);

    public static final List<String> SAMPLE_PANEL_NAMES =
            Collections.unmodifiableList(Arrays.asList(
                    "All issues in my two sample repos",
                    "Latest 3 urgent open issues",
                    "Open issues assigned to Darius or Manmeet",
                    "Progress of the current milestone",
                    "Issues awaiting prioritization",
                    "Recent unread updates from my repos"
            ));

    public static final List<String> SAMPLE_PANEL_FILTERS =
            Collections.unmodifiableList(Arrays.asList(
            String.format("repo:%s;%s is:issue sort:!updated,comments",
                    FIRST_SAMPLE_REPO_NAME, SECOND_SAMPLE_REPO_NAME),
            String.format("repo:%s count:3 is:issue is:open label:\"urgent\"", FIRST_SAMPLE_REPO_NAME),
            String.format("repo:%s is:issue is:open (assignee:dariusf || assignee:codemanmeet)",
                    FIRST_SAMPLE_REPO_NAME),
            String.format("repo:%s m:curr sort:status", FIRST_SAMPLE_REPO_NAME),
            String.format("repo:%s is:open is:issue !label:priority.", FIRST_SAMPLE_REPO_NAME),
            String.format("repo:%s;%s is:unread updated<24 sort:!updated",
                    FIRST_SAMPLE_REPO_NAME, SECOND_SAMPLE_REPO_NAME)
            ));

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
        MenuItem sample = new MenuItem(SAMPLE_BOARD);
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
        logger.info("Creating " + SAMPLE_BOARD);

        panelControl.closeAllPanels();

        List<PanelInfo> panelData = new ArrayList<>();

        for (int i = 0; i < SAMPLE_PANEL_NAMES.size(); i++) {
            panelData.add(new PanelInfo(SAMPLE_PANEL_NAMES.get(i), SAMPLE_PANEL_FILTERS.get(i)));
        }

        createBoard(panelData, SAMPLE_BOARD);

        panelControl.selectPanel(0); // current

        triggerBoardSaveEventSequence(SAMPLE_BOARD);

        DialogMessage.showInformationDialog("Auto-create Board - " + SAMPLE_BOARD, SAMPLE_BOARD_DIALOG);
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
