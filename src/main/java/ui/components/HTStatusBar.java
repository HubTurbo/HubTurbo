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
		getStyleClass().add("top-borders");

		setupTimerLabel();
		getRightItems().add(timerLabel);
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
	
	public static void addProgress(double increment) {
		double progress = Math.min(Math.max(0, getInstance().getProgress() + increment), 1);
		Platform.runLater(() -> {
			getInstance().setProgress(progress);
		});
	}

	public static void addProgressAndDisplayMessage(double increment, String message) {
		double progress = Math.min(Math.max(0, getInstance().getProgress() + increment), 1);
		Platform.runLater(() -> {
			getInstance().setText(message);
			getInstance().setProgress(progress);
		});
	}

	public static void updateProgress(double progress) {
		Platform.runLater(() -> {
			getInstance().setProgress(progress);
		});
	}
}
