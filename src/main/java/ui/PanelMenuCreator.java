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
import prefs.Preferences;

import java.util.*;

import static ui.components.KeyboardShortcuts.CLOSE_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_LEFT_PANEL;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;

public class PanelMenuCreator {

    private final Preferences prefs;

    private static final Logger logger = LogManager.getLogger(MenuControl.class.getName());

    private final PanelControl panelControl;
    private final ScrollPane panelsScrollPane;

    public PanelMenuCreator(PanelControl panelControl, ScrollPane panelsScrollPane, Preferences prefs) {
        this.panelsScrollPane = panelsScrollPane;
        this.panelControl = panelControl;
        this.prefs = prefs;
    }

    public Menu generatePanelMenu() {
        Menu panelMenu = new Menu("Panels");
        Menu autoCreatePanelMenu = new Menu("Auto-create");
        List<MenuItem> items = new ArrayList<>();
        List<MenuItem> autoCreateItems = new ArrayList<>();
        items.add(createLeftPanelMenuItem());
        items.add(createRightPanelMenuItem());
        items.add(closePanelMenuItem());
        for (Map.Entry<String, String> entry : generateCustomizedPanelDetails().entrySet()) {
            autoCreateItems.add(createCustomizedPanelMenuItem(entry.getKey(), entry.getValue()));
        }
        autoCreatePanelMenu.getItems().addAll(autoCreateItems);
        panelMenu.getItems().addAll(items);
        panelMenu.getItems().add(autoCreatePanelMenu);
        return panelMenu;
    }

    public Map<String, String> generateCustomizedPanelDetails(){
        Map<String, String> customPanelMap = new LinkedHashMap<>();
        customPanelMap.put("Open issues and PR's",
                String.format("is:open ((is:issue assignee:%s) OR (is:pr author:%s))", prefs.getLastLoginUsername(),
                        prefs.getLastLoginUsername()));
        customPanelMap.put("Current Milestone", "milestone:curr sort:status");
        customPanelMap.put("Recently Updated issues",
                String.format("assignee:%s updated:<48", prefs.getLastLoginUsername()));
        return Collections.unmodifiableMap(customPanelMap);
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

    public MenuItem createCustomizedPanelMenuItem(String panelName, String panelFilter) {
        MenuItem customizedPanel = new MenuItem(panelName);
        customizedPanel.setOnAction(e -> {
            logger.info("Menu: Panels > Auto-create > " + panelName + "panel");
            panelControl.generatePanelWithNameAndFilter(panelName, panelFilter);
            panelControl.selectLastPanel();
        });
        return customizedPanel;
    }
}
