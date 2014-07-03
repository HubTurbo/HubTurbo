package ui;

import java.lang.ref.WeakReference;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import model.TurboLabel;

public class LabelDisplayBox extends FlowPane {

	private ObservableList<TurboLabel> labels;
	private boolean displayWhenEmpty;
	
	public LabelDisplayBox() {
		this(FXCollections.observableArrayList(), false);
	}

	public LabelDisplayBox(ObservableList<TurboLabel> labels, boolean displayWhenEmpty) {
		this.labels = labels;
		this.displayWhenEmpty = displayWhenEmpty;
		setup();
	}

	private void setup() {
		if (displayWhenEmpty) {
			setStyle(UI.STYLE_BORDERS_FADED);
		}
		setHgap(3);
		setVgap(3);

		WeakReference<LabelDisplayBox> that = new WeakReference<LabelDisplayBox>(this);
		labels.addListener(new ListChangeListener<TurboLabel>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TurboLabel> arg0) {
				that.get().populateWithLabels();
			}
		});
		populateWithLabels();
	}

	public LabelDisplayBox setLabels(ObservableList<TurboLabel> labels) {
		this.labels = labels;
		populateWithLabels();
		return this;
	}
	
	private void populateWithLabels() {
		getChildren().clear();
		
		if (displayWhenEmpty && labels.size() == 0) {
			
			Label noLabels = new Label("Labels");
			noLabels.setStyle(UI.STYLE_FADED + "-fx-padding: 5;");
			getChildren().add(noLabels);

			return;
		}
		
		for (TurboLabel label : labels) {
			Label labelText = new Label(label.getName());
			labelText.setStyle(getStyleFor(label));
			label.nameProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(
						ObservableValue<? extends String> stringProperty,
						String oldValue, String newValue) {
					labelText.setText(newValue);//TODO:
				}
			});
			getChildren().add(labelText);
		}
	}

	private String getStyleFor(TurboLabel label) {
		String colour = label.getColour();
		String style = "-fx-background-color: #"
				+ colour
				+ "; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 20; -fx-padding: 5;";
		return style;
	}
}
