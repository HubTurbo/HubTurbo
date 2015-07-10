package backend;

import backend.resource.MultiModel;
import javafx.application.Platform;
import ui.UI;
import util.events.ModelUpdatedEvent;

import java.util.Optional;

public class UIManager {

    private final UI ui;

    public UIManager(UI ui) {
        this.ui = ui;
    }

    public void update(MultiModel models, Optional<Integer> remainingRequests, boolean hasMetadata) {
        System.out.println(remainingRequests.get());
        Platform.runLater(() ->
            ui.triggerEvent(new ModelUpdatedEvent(models, remainingRequests, hasMetadata)));
    }

    public void updateNow(MultiModel models, Optional<Integer> remainingRequests) {
        ui.triggerEvent(new ModelUpdatedEvent(models, remainingRequests, false));
    }
}

