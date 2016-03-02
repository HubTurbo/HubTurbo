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
import util.events.WarnUserEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UIManager {

    private final UI ui;

    public UIManager(UI ui) {
        this.ui = ui;
    }

    public void update(Map<FilterExpression, ImmutablePair<List<GuiElement>, List<String>>> elementsToShow,
                       List<TurboUser> users) {
        Map<FilterExpression, List<GuiElement>> guiElementsToShow = elementsToShow.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getLeft()));
        Map<FilterExpression, List<String>> warningsToShow = elementsToShow.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getRight()));
        Platform.runLater(() ->
                ui.triggerEvent(new ModelUpdatedEvent(guiElementsToShow, users)));
        Platform.runLater(() ->
                ui.triggerEvent(new WarnUserEvent(warningsToShow)));
    }

    public void updateRateLimits(ImmutablePair<Integer, Long> rateLimits) {
        ui.triggerEvent(new UpdateRateLimitsEvent(rateLimits.left, rateLimits.right));
    }

    /**
     * Gets the list of panels currently shown in the UI
     * @return
     */
    public List<FilterPanel> getAllPanels() {
        return ui.guiController.getAllPanels();
    }
}

