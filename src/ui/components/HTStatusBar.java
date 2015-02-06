package ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import org.controlsfx.control.StatusBar;

public class HTStatusBar extends StatusBar {

	private static HTStatusBar instance = null;

	public static HTStatusBar getInstance() {
		if (instance == null) {
			instance = new HTStatusBar();
		}
		return instance;
	}

	private Label timerLabel = new Label();

	public HTStatusBar() {
		setupTimerLabel();
		getRightItems().add(timerLabel);

		getStyleClass().add("top-borders");
	}

	private void setupTimerLabel() {
		HBox.setMargin(timerLabel, new Insets(3));
	}

	public static void displayMessage(String text) {
		Platform.runLater(() -> {
			getInstance().setText(text);
		});
	}

	public static void updateRefreshTimer(int time) {
		Platform.runLater(() -> {
			getInstance().timerLabel.setText(Integer.toString(time));
		});
	}
}
