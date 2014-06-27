package ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.Listable;

public class ListablesDisplayBox extends HBox {
	
	private String defaultText;
	private Listable item = null;
	
	public ListablesDisplayBox(String defaultText, Listable item) {
		this.defaultText = defaultText;
		setListableItem(item);
		setup();
	}
	
	private void setup() {
		setStyle(UI.STYLE_BORDERS_FADED);
	}

	public void setListableItem(Listable item) {
		this.item = item;
		update();
	}
	
	private void update() {
		Label label;
		if (item == null) {
			label = new Label(defaultText);
			label.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");
		} else {
			label = new Label(item.getListName());
			label.setStyle("-fx-padding: 5 5 5 5;");
		}
		getChildren().clear();
		getChildren().add(label);
	}
	
}
