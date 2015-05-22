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

	public void update(MultiModel models) {
		Platform.runLater(() -> ui.triggerEvent(new ModelUpdatedEvent(models)));
	}
}

