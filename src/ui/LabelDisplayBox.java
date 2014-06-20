package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import logic.TurboLabel;

public class LabelDisplayBox extends FlowPane {

	ObservableList<TurboLabel> labels;
	
	public LabelDisplayBox(ObservableList<TurboLabel> labels) {
		this.labels = labels;
		setup();
	}

	private void setup() {
		setHgap(3);
		setVgap(3);
		
		labels.addListener(new ListChangeListener<TurboLabel>() {
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TurboLabel> arg0) {
				populateWithLabels();
			}
		});
		populateWithLabels();
	}
	
	private void populateWithLabels() {
		for (TurboLabel label : labels) {
			Label labelText = new Label(label.getName());
			labelText.setStyle(getStyleFor(label));
			label.nameProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(
						ObservableValue<? extends String> stringProperty,
						String oldValue, String newValue) {
					labelText.setText(newValue);
				}
			});
			getChildren().add(labelText);
		}
	}
	
	private String getStyleFor(TurboLabel label) {
		String colour = label.getColour();
//		if (colour.equals("#000000")) {
//			
//		}
//		if (label.getName().equals("bug")) {
//			colour = "red";
//		} else if (label.getName().equals("feature")) {
//			colour = "green";
//		}
		String style = "-fx-background-color: " + colour + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 20; -fx-padding: 3;";
		return style;
	}
}
