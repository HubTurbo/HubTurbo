package ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.Listable;

public class ListableDisplayBox extends HBox {
	
	private String defaultText;
	private Listable item = null;
	
	public ListableDisplayBox(String defaultText, Listable item) {
		this.defaultText = defaultText;
		setListableItem(item);
		setup();
	}
	
	private void setup() {
		getStyleClass().add("faded-borders");
	}

	public void setListableItem(Listable item) {
		this.item = item;
		update();
	}
	
	private void update() {
		Label label;
		if (item == null) {
			label = new Label(defaultText);
			label.getStyleClass().addAll("faded", "display-box-padding");
		} else {
			label = new Label(item.getListName());
			label.getStyleClass().addAll("display-box-padding");
		}
		getChildren().clear();
		getChildren().add(label);
	}
	
}
