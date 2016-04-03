package guitests;

import static ui.components.KeyboardShortcuts.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import ui.IdGenerator;
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
        waitUntilNodeAppears(getFilterPanel(0).getFilterTextField());

        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);

        selectPanel(1);

        final FilterPanel panel0 = getFilterPanel(0);
        final FilterPanel panel1 = getFilterPanel(1);
        final FilterPanel panel2 = getFilterPanel(2);

        reorderPanelsByDragging(panel0, panel1, panel2);

        click(panel0.getCloseButton());
        waitUntilNodeDisappears(panel0);

        // Switch default repo
        clickRepositorySelector();
        selectAll();
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        pushKeys(CREATE_RIGHT_PANEL);

        // Ensure that new panels are associated with the current default repo
        awaitCondition(() -> existsQuiet(IdGenerator.getPanelIdReference(2)));
    }

    private void reorderPanelsByDragging(FilterPanel panel0, FilterPanel panel1, FilterPanel panel2) {
        dragUnconditionally(dragSrc(panel1), dragDest(panel0));
        awaitCondition(() -> getFilterPanel(0) == panel1);

        dragUnconditionally(dragSrc(panel0), dragDest(panel1));
        awaitCondition(() -> getFilterPanel(0) == panel0);

        dragUnconditionally(dragSrc(panel1), dragDest(panel0));
        awaitCondition(() -> getFilterPanel(0) == panel1);

        dragUnconditionally(dragSrc(panel1), dragDest(panel2));
        awaitCondition(() -> getFilterPanel(0) == panel0 && getFilterPanel(1) == panel2 && getFilterPanel(2) == panel1);

        dragUnconditionally(dragSrc(panel2), dragDest(panel1));
        awaitCondition(() -> getFilterPanel(0) == panel0 && getFilterPanel(1) == panel1 && getFilterPanel(2) == panel2);

        dragUnconditionally(dragSrc(panel0), dragDest(panel1));
        awaitCondition(() -> getFilterPanel(0) == panel1);
    }

    private Node dragSrc(FilterPanel panel) {
        return panel.getCloseButton();
    }

    private Node dragDest(FilterPanel panel) {
        return panel.getFilterTextField();
    }

    private void selectPanel(int index) {
        eventTriggered.set(false);
        click(getFilterPanel(index).getNameText());
        awaitCondition(eventTriggered::get);
    }

    private FilterPanel getFilterPanel(int index) {
        PanelControl panels = TestController.getUI().getPanelControl();
        return (FilterPanel) panels.getPanel(index);
    }
}
