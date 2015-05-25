package ui.components;

import javafx.application.Platform;
import org.controlsfx.control.StatusBar;
import ui.UI;
import util.events.UpdateProgressEventHandler;

import java.util.HashMap;
import java.util.Map;

public class HTStatusBar extends StatusBar implements StatusUI {

	private final UI ui;
	private final Map<String, TextProgressBar> progressBars;

	public HTStatusBar(UI ui) {
		this.ui = ui;
		progressBars = new HashMap<>();

		setup();

		setupProgressEvents();
	}

	private void setupProgressEvents() {
		ui.registerEvent((UpdateProgressEventHandler) e -> {
			Platform.runLater(() -> {
				if (progressBars.containsKey(e.repoId)) {
					if (e.done) {
						getRightItems().remove(progressBars.get(e.repoId));
					} else {
						progressBars.get(e.repoId).setProgress(e.progress);
					}
				} else {
					TextProgressBar progressBar = new TextProgressBar(e.repoId);
					progressBars.put(e.repoId, progressBar);
					getRightItems().add(progressBar);
				}
			});
		});
	}

	private void setup() {
		getStyleClass().add("top-borders");
	}

	public void displayMessage(String text) {
		Platform.runLater(() -> {
			setText(text);
		});
	}

	@Override
	public void clear() {
		displayMessage("");
	}
}
