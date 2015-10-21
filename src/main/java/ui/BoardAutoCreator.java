package ui;


import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.DialogMessage;
import util.Utility;
import util.events.BoardSavedEvent;

import java.util.stream.Collectors;

public class BoardAutoCreator {
    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());
    private static final String MILESTONES = "Milestones";

    private final PanelControl panelControl;
    private final Preferences prefs;

    public BoardAutoCreator(PanelControl panelControl, Preferences prefs) {
        this.panelControl = panelControl;
        this.prefs = prefs;
    }

    public Menu generateBoardAutoCreateMenu() {
        Menu autoCreate = new Menu("Auto-create");

        MenuItem milestone = new MenuItem(MILESTONES);
        milestone.setOnAction(e -> createMilestoneBoard());
        autoCreate.getItems().add(milestone);

        return autoCreate;
    }

    private void createMilestoneBoard() {
        logger.info("Creating " + MILESTONES + " board");

        panelControl.closeAllPanels();

        // reverse ordering so always addPanelAt(0)
        FilterPanel panelCurrPlus3 = panelControl.addPanelAt(0);
        panelCurrPlus3.setPanelName("Next Next Next Milestone");
        panelCurrPlus3.setFilterByString("milestone:curr+3 sort:status");

        FilterPanel panelCurrPlus2 = panelControl.addPanelAt(0);
        panelCurrPlus2.setPanelName("Next Next Milestone");
        panelCurrPlus2.setFilterByString("milestone:curr+2 sort:status");

        FilterPanel panelCurrPlus1 = panelControl.addPanelAt(0);
        panelCurrPlus1.setPanelName("Next Milestone");
        panelCurrPlus1.setFilterByString("milestone:curr+1 sort:status");

        FilterPanel panelCurr = panelControl.addPanelAt(0);
        panelCurr.setPanelName("Current Milestone");
        panelCurr.setFilterByString("milestone:curr sort:status");

        FilterPanel panelCurrMinus1 = panelControl.addPanelAt(0);
        panelCurrMinus1.setPanelName("Previous Milestone");
        panelCurrMinus1.setFilterByString("milestone:curr-1 sort:status");

        panelControl.selectPanel(1); // current

        String boardName = Utility.generateName(MILESTONES,
                prefs.getAllBoards().keySet().stream().collect(Collectors.toList()));

        triggerBoardSaveEventSequence(boardName);

        DialogMessage.showInformationDialog("Auto-create Board - " + MILESTONES,
                MILESTONES + " board has been created and loaded.\n\n" +
                        "It is saved under the name \"" + boardName + "\".");
    }

    private void triggerBoardSaveEventSequence(String boardName) {
        prefs.addBoard(boardName, panelControl.getCurrentPanelInfos());
        prefs.setLastOpenBoard(boardName);
        TestController.getUI().triggerEvent(new BoardSavedEvent());
        logger.info("Auto-created board, saved as \"" + boardName + "\"");
        TestController.getUI().updateTitle();
    }
}
