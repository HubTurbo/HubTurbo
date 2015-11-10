package guitests;

import static junit.framework.TestCase.assertTrue;
import static ui.components.KeyboardShortcuts.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import ui.TestController;
import ui.UI;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.events.PanelClickedEventHandler;

public class PanelsTest extends UITest {

    final AtomicBoolean eventTriggered = new AtomicBoolean(false);

    @Test
    public void panelsTest() {

        UI.events.registerEvent((PanelClickedEventHandler) e -> eventTriggered.set(true));

        pushKeys(MAXIMIZE_WINDOW);
        pushKeys(CREATE_RIGHT_PANEL);
        pushKeys(CREATE_RIGHT_PANEL);
        waitUntilNodeAppears(getPanel(0).getFilterTextField());

        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);

        selectPanel(1);

        final FilterPanel panel0 = getPanel(0);
        final FilterPanel panel1 = getPanel(1);
        final FilterPanel panel2 = getPanel(2);

        reorderPanelsByDragging(panel0, panel1, panel2);
        reorderPanelsViaHotkeys(panel0, panel1);

        click(panel0.getCloseButton());
        waitUntilNodeDisappears(panel0);

        // Switch default repo
        doubleClick("#repositorySelector");
        doubleClick();
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        pushKeys(CREATE_RIGHT_PANEL);

        // Ensure that new panels are associated with the current default repo
        awaitCondition(() -> existsQuiet("#dummy2/dummy2_col2"));
    }

    private void reorderPanelsByDragging(FilterPanel panel0, FilterPanel panel1, FilterPanel panel2) {
        dragUnconditionally(dragSrc(panel1), dragDest(panel0));
        awaitCondition(() -> getPanel(0) == panel1);

        dragUnconditionally(dragSrc(panel0), dragDest(panel1));
        awaitCondition(() -> getPanel(0) == panel0);

        dragUnconditionally(dragSrc(panel1), dragDest(panel0));
        awaitCondition(() -> getPanel(0) == panel1);

        dragUnconditionally(dragSrc(panel1), dragDest(panel2));
        awaitCondition(() -> getPanel(0) == panel0 && getPanel(1) == panel2 && getPanel(2) == panel1);

        dragUnconditionally(dragSrc(panel2), dragDest(panel1));
        awaitCondition(() -> getPanel(0) == panel0 && getPanel(1) == panel1 && getPanel(2) == panel2);

        dragUnconditionally(dragSrc(panel0), dragDest(panel1));
        awaitCondition(() -> getPanel(0) == panel1);
    }

    private void reorderPanelsViaHotkeys(FilterPanel panel0, FilterPanel panel1) {
        selectPanel(0);
        pushKeys(SWAP_PANEL_RIGHT);
        awaitCondition(() -> isSwapComplete() && getPanel(0) == panel0);
        pushKeys(SWAP_PANEL_LEFT);
        awaitCondition(() -> isSwapComplete() && getPanel(0) == panel1);
    }

    private Node dragSrc(FilterPanel panel) {
        return panel.getCloseButton();
    }

    private Node dragDest(FilterPanel panel) {
        return panel.getFilterTextField();
    }

    private boolean isSwapComplete() {
        PanelControl panels = TestController.getUI().getPanelControl();
        return panels.getChildren().stream()
            .noneMatch(child -> child instanceof HBox);
    }

    private void selectPanel(int index) {
        eventTriggered.set(false);
        click(getPanel(index).getNameText());
        awaitCondition(eventTriggered::get);
    }

    private FilterPanel getPanel(int index) {
        PanelControl panels = TestController.getUI().getPanelControl();
        return (FilterPanel) panels.getPanel(index);
    }
}
