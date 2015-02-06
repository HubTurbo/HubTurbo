package ui.components;

import javafx.application.Platform;

import org.controlsfx.control.StatusBar;

public class HTStatusBar extends StatusBar {

	private static HTStatusBar instance = null;
	public static HTStatusBar getInstance() {
		if (instance == null) {
			instance = new HTStatusBar();
		}
		return instance;
	}
		
	public HTStatusBar() {
	}
	
	public static void displayMessage(String text) {
		Platform.runLater(() -> {
			getInstance().setText(text);
		});
	}
}
