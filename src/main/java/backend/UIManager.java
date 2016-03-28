package backend;

import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import javafx.application.Platform;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ui.GuiElement;
import ui.UI;
import ui.issuepanel.FilterPanel;
import util.events.ModelUpdatedEvent;
import util.events.UpdateRateLimitsEvent;

import java.util.List;
import java.util.Map;

public class UIManager {

    private final UI ui;

    public UIManager(UI ui) {
        this.ui = ui;
    }

    public void update(Map<FilterExpression, List<GuiElement>> elementsToShow,
                       List<TurboUser> users) {
        Platform.runLater(() -> ui.triggerEvent(new ModelUpdatedEvent(elementsToShow, users)));
    }

    public void updateRateLimits(ImmutablePair<Integer, Long> rateLimits) {
        ui.triggerEvent(new UpdateRateLimitsEvent(rateLimits.left, rateLimits.right));
    }

    /**
     * Gets the list of panels currently shown in the UI
     *
     * @return
     */
    public List<FilterPanel> getAllPanels() {
        return ui.guiController.getAllPanels();
    }
}

