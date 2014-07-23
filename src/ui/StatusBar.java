package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class StatusBar extends HBox {

	private static StatusBar instance = null;
	public static StatusBar getInstance() {
		if (instance == null) {
			instance = new StatusBar();
		}
		return instance;
	}
	
	private final Label text;
	
	public StatusBar() {
		text = new Label();
		HBox.setMargin(text, new Insets(3));
		HBox.setHgrow(this, Priority.ALWAYS);
		getStyleClass().add("top-borders");
		getChildren().add(text);
	}
	
	public static void displayMessage(String text) {
		Platform.runLater(() -> getInstance().text.setText(text));
	}
}
