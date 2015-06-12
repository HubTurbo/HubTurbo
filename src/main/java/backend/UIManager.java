package backend;

import backend.resource.MultiModel;
import javafx.application.Platform;
import ui.UI;
import util.events.ModelUpdatedEvent;

public class UIManager {

    private final UI ui;

    public UIManager(UI ui) {
        this.ui = ui;
    }

    public void update(MultiModel models, boolean hasMetadata) {
        Platform.runLater(() ->
            ui.triggerEvent(new ModelUpdatedEvent(models, hasMetadata)));
    }

    public void updateNow(MultiModel models) {
        ui.triggerEvent(new ModelUpdatedEvent(models, false));
    }
}

