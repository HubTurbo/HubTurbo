package backend;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import javafx.application.Platform;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ui.UI;
import util.events.ModelUpdatedEvent;
import util.events.UpdateRateLimitsEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIManager {

    private final UI ui;

    public UIManager(UI ui) {
        this.ui = ui;
    }

    public void update(MultiModel models,
                       Map<FilterExpression, ImmutablePair<List<TurboIssue>, Boolean>> issuesToShow) {
        Platform.runLater(() ->
                ui.triggerEvent(new ModelUpdatedEvent(models, issuesToShow)));
    }

    public void updateEmpty(MultiModel models) {
        ui.triggerEvent(new ModelUpdatedEvent(models, new HashMap<>()));
    }

    public void updateRateLimits(ImmutablePair<Integer, Long> rateLimits) {
        ui.triggerEvent(new UpdateRateLimitsEvent(rateLimits.left, rateLimits.right));
    }

    public List<FilterExpression> getAllFilters() {
        return ui.guiController.getAllFilters();
    }
}

