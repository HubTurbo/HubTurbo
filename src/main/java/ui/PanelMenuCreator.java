package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;

import java.util.ArrayList;
import java.util.List;

import static ui.components.KeyboardShortcuts.CLOSE_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_LEFT_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;

public class PanelMenuCreator {
    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());
    private final PanelControl panelControl;
    private final ScrollPane panelsScrollPane;

    public PanelMenuCreator(PanelControl panelControl, ScrollPane panelsScrollPane) {
        this.panelsScrollPane = panelsScrollPane;
        this.panelControl = panelControl;
    }

    public Menu generatePanelMenu(){
        Menu panelMenu = new Menu("Panels");
        List<MenuItem> items = new ArrayList<>();
        items.add(createLeftPanel());
        items.add(createRightPanel());
        items.add(createAssigneePanel());
        items.add(createRecentlyUpdatedPanel());
        items.add(createMilestonePanel());
        items.add(closePanel());
        panelMenu.getItems().addAll(items);
        return panelMenu;
    }

    public MenuItem createLeftPanel(){
        MenuItem createLeft = new MenuItem("Create (Left)");
        createLeft.setOnAction(e -> {
            logger.info("Menu: Panels > Create (Left)");
            panelControl.createNewPanelAtStart();
            panelControl.scrollToCurrentlySelectedPanel();
        });
        createLeft.setAccelerator(CREATE_LEFT_PANEL);
        return createLeft;
    }

    public MenuItem createRightPanel(){
        MenuItem createRight = new MenuItem("Create");
        createRight.setOnAction(e -> {
            logger.info("Menu: Panels > Create");
            panelControl.createNewPanelAtEnd();
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
                                panelControl.scrollToCurrentlySelectedPanel();
                                break;
                            }
                        }
                    }
                    panelControl.widthProperty().removeListener(this);
                }
            };
            panelControl.widthProperty().addListener(listener);
        });
        createRight.setAccelerator(CREATE_RIGHT_PANEL);
        return createRight;
    }

    public MenuItem closePanel(){
        MenuItem closePanel = new MenuItem("Close");
        closePanel.setOnAction(e -> {
            logger.info("Menu: Panels > Close");
            panelControl.closeCurrentPanel();
        });
        closePanel.setAccelerator(CLOSE_PANEL);
        return closePanel;
    }

    public MenuItem createAssigneePanel(){
        MenuItem assigneePanel = new MenuItem("Self-assigned issues");
        assigneePanel.setOnAction(e -> {
            logger.info("Menu: Panels > Create Self-assigned issues panel");
            generatePanelWithNameAndFilter("Open issues and PR's",
                    "is:open ((is:issue assignee:me) OR (is:pr author:me))");
            panelControl.selectLastPanel();
        });
        return assigneePanel;
    }

    public MenuItem createRecentlyUpdatedPanel(){
        MenuItem recentlyUpdatedPanel = new MenuItem("Recently Updated issues");
        recentlyUpdatedPanel.setOnAction(e -> {
            logger.info("Menu: Panels > Create Recently Updated issues panel");
            generatePanelWithNameAndFilter("Recently Updated issues", "assignee:me updated:<48");
            panelControl.selectLastPanel();
        });
        return recentlyUpdatedPanel;
    }

    public MenuItem createMilestonePanel(){
        MenuItem recentlyUpdatedPanel = new MenuItem("Current Milestone");
        recentlyUpdatedPanel.setOnAction(e -> {
            logger.info("Menu: Panels > Create Current Milestone panel");
            generatePanelWithNameAndFilter("Current Milestone", "milestone:curr sort:status");
            panelControl.selectLastPanel();
        });
        return recentlyUpdatedPanel;
    }

    public FilterPanel generatePanelWithNameAndFilter(String panelName, String filterName){
        FilterPanel panelAdded = panelControl.addPanelAt(panelControl.getPanelCount());
        panelAdded.setPanelName(panelName);
        panelAdded.setFilterByString(filterName);
        return panelAdded;
    }
}
