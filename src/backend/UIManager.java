package backend;

import util.events.ModelUpdatedEvent;
import backend.resource.MultiModel;
import javafx.application.Platform;
import ui.UI;

public class UIManager {

	private final UI ui;

	public UIManager(UI ui) {
		this.ui = ui;
	}

	public void update(MultiModel models) {
		Platform.runLater(() -> ui.triggerEvent(new ModelUpdatedEvent(models)));
	}
}

