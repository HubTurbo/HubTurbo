package ui;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import logic.TurboLabel;

public class LabelDisplayBox extends FlowPane {

	private ObservableList<TurboLabel> labels;
	
	public LabelDisplayBox() {
		this.labels = FXCollections.observableArrayList();
		setup();
	}
	
	public LabelDisplayBox(List<TurboLabel> labels) {
		this.labels = FXCollections.observableArrayList(labels);
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
	
	public LabelDisplayBox setLabels(List<TurboLabel> labels) {
		this.labels = FXCollections.observableArrayList(labels);
		populateWithLabels();
		return this;
	}
	
	private void populateWithLabels() {
		getChildren().clear();
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
	
	public LabelDisplayBox showBordersAndPlaceholder() {
		setStyle(Demo.STYLE_BORDERS_FADED);

		Label noLabels = new Label("Labels");
		noLabels.setStyle(Demo.STYLE_FADED + "-fx-padding: 5 5 5 5;");
		getChildren().add(noLabels);
		return this;
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
		String style = "-fx-background-color: #" + colour + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 20; -fx-padding: 3;";
		return style;
	}
}
