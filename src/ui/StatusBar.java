package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class StatusBar extends HBox {

	private final Label text;
	
	public StatusBar() {
		text = new Label();
		HBox.setMargin(text, new Insets(5));
		HBox.setHgrow(this, Priority.ALWAYS);
		getChildren().add(text);
	}
	
	public void setText(String text) {
		this.text.setText(text);
	}
	
}
