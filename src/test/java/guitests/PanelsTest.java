package guitests;

import static ui.components.KeyboardShortcuts.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.events.PanelClickedEventHandler;

public class PanelsTest extends UITest {

    final AtomicBoolean eventTriggered = new AtomicBoolean(false);

    PanelControl panelControl;

    @Before
    public void setupUIComponent() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void panelsTest() {

        UI.events.registerEvent((PanelClickedEventHandler) e -> eventTriggered.set(true));

        press(MAXIMIZE_WINDOW);
        traverseMenu("Panels", "Create");
        waitAndAssertEquals(2, panelControl::getPanelCount);
        traverseMenu("Panels", "Create");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        clickFilterTextFieldAtPanel(0);

        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);

        selectPanel(1);

        final FilterPanel panel0 = getFilterPanel(0);
        final FilterPanel panel1 = getFilterPanel(1);
        final FilterPanel panel2 = getFilterPanel(2);

        reorderPanelsByDragging(panel0, panel1, panel2);

        clickOn(panel0.getCloseButton());
        waitUntilNodeDisappears(panel0);

        // Ensure that new panels are associated with the current default repo
        awaitCondition(() -> existsQuiet(IdGenerator.getPanelIdReference(2)));
    }

    private void reorderPanelsByDragging(FilterPanel panel0, FilterPanel panel1, FilterPanel panel2) {
        dragUnconditionally(panel1, panel0);
        awaitCondition(() -> getFilterPanel(0) == panel1);

        dragUnconditionally(panel0, panel1);
        awaitCondition(() -> getFilterPanel(0) == panel0);

        dragUnconditionally(panel1, panel0);
        awaitCondition(() -> getFilterPanel(0) == panel1);

        dragUnconditionally(panel1, panel2);
        awaitCondition(() -> getFilterPanel(0) == panel0 && getFilterPanel(1) == panel2 && getFilterPanel(2) == panel1);

        dragUnconditionally(panel2, panel1);
        awaitCondition(() -> getFilterPanel(0) == panel0 && getFilterPanel(1) == panel1 && getFilterPanel(2) == panel2);

        dragUnconditionally(panel0, panel1);
        awaitCondition(() -> getFilterPanel(0) == panel1);
    }

    private void selectPanel(int index) {
        eventTriggered.set(false);
        clickOn(getFilterPanel(index).getNameText());
        awaitCondition(eventTriggered::get);
    }

    private FilterPanel getFilterPanel(int index) {
        PanelControl panels = TestController.getUI().getPanelControl();
        return (FilterPanel) panels.getPanel(index);
    }
}
