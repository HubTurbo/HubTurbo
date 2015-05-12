package backend;

import javafx.application.Platform;
import ui.UI;

public class UIManager {

	private final UI ui;

	public UIManager(UI ui) {
		this.ui = ui;
	}

	public void update(Model model) {
		Platform.runLater(() -> {
			ui.triggerEvent(new ModelUpdatedEvent(model));
		});
	}
}

