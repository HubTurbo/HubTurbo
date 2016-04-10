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
import ui.issuepanel.PanelControl;

import java.util.*;
import java.util.Map.Entry;

import static ui.components.KeyboardShortcuts.CLOSE_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_LEFT_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;

public class PanelMenuCreator {

    private final String currentUsername;

    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

    private final PanelControl panelControl;
    private final ScrollPane panelsScrollPane;

    public PanelMenuCreator(PanelControl panelControl, ScrollPane panelsScrollPane, String currentUsername) {
        this.panelsScrollPane = panelsScrollPane;
        this.panelControl = panelControl;
        this.currentUsername = currentUsername;
    }

    public Menu generatePanelMenu() {
        Menu panelMenu = new Menu("Panels");
        Menu autoCreatePanelMenu = new Menu("Auto-create");
        List<MenuItem> items = new ArrayList<>();
        List<MenuItem> autoCreateItems = new ArrayList<>();
        items.add(createLeftPanelMenuItem());
        items.add(createRightPanelMenuItem());
        items.add(closePanelMenuItem());
        for (Entry<String, String> entry :
                generatePanelDetails(currentUsername).entrySet()) {
            autoCreateItems.add(createPanelMenuItem(entry.getKey(), entry.getValue()));
        }
        autoCreatePanelMenu.getItems().addAll(autoCreateItems);
        panelMenu.getItems().addAll(items);
        panelMenu.getItems().add(autoCreatePanelMenu);
        return panelMenu;
    }

    /**
     * Returns a map of custom panels that can be created using the auto-create menu
     * with the key of the map as the panel name and the value as the corresponding filter name.
     * Uses the username as a parameter to construct the filter names.
     * @param currentUsername
     */
    public static Map<String, String> generatePanelDetails(String currentUsername){
        Map<String, String> customPanels = new LinkedHashMap<>();
        customPanels.put("Open issues and PR's",
                String.format("is:open ((is:issue assignee:%1$s) OR (is:pr author:%1$s))", currentUsername));
        customPanels.put("Current Milestone", "milestone:curr sort:status");
        customPanels.put("Recently Updated issues",
                String.format("assignee:%s updated:<48", currentUsername));
        return Collections.unmodifiableMap(customPanels);
    }

    public MenuItem createLeftPanelMenuItem() {
        MenuItem createLeft = new MenuItem("Create (Left)");
        createLeft.setOnAction(e -> {
            logger.info("Menu: Panels > Create (Left)");
            panelControl.createNewPanelAtStart();
            panelControl.scrollToCurrentlySelectedPanel();
        });
        createLeft.setAccelerator(CREATE_LEFT_PANEL);
        return createLeft;
    }

    public MenuItem createRightPanelMenuItem() {
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

    public MenuItem closePanelMenuItem() {
        MenuItem closePanel = new MenuItem("Close");
        closePanel.setOnAction(e -> {
            logger.info("Menu: Panels > Close");
            panelControl.closeCurrentPanel();
        });
        closePanel.setAccelerator(CLOSE_PANEL);
        return closePanel;
    }

    public MenuItem createPanelMenuItem(String panelName, String panelFilter) {
        MenuItem customizedPanel = new MenuItem(panelName);
        customizedPanel.setOnAction(e -> {
            logger.info("Menu: Panels > Auto-create > " + panelName + "panel");
            panelControl.generatePanelWithNameAndFilter(panelName, panelFilter);
            panelControl.selectLastPanel();
        });
        return customizedPanel;
    }
}
