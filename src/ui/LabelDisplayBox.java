package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import model.TurboLabel;

public class LabelDisplayBox extends FlowPane {

	private ObservableList<TurboLabel> labels;
	private boolean displayWhenEmpty;
	private ArrayList<Object> changeListeners = new ArrayList<Object>();
	
	public LabelDisplayBox() {
		this(FXCollections.observableArrayList(), false);
	}

	public LabelDisplayBox(ObservableList<TurboLabel> labels, boolean displayWhenEmpty) {
		this.labels = labels;
		this.displayWhenEmpty = displayWhenEmpty;
		setup();
	}
	
	private ListChangeListener<TurboLabel> createLabelsChangeListener(){
		WeakReference<LabelDisplayBox> that = new WeakReference<LabelDisplayBox>(this);
		ListChangeListener<TurboLabel> listener = new ListChangeListener<TurboLabel>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TurboLabel> arg0) {
				if(that.get() != null){
					that.get().populateWithLabels();
				}
			}
		};
		changeListeners.add(listener);
		return listener;
	}

	private void setup() {
		if (displayWhenEmpty) {
			setStyle(UI.STYLE_BORDERS_FADED);
		}
		setHgap(3);
		setVgap(3);

		labels.addListener(new WeakListChangeListener<TurboLabel>(createLabelsChangeListener()));
		populateWithLabels();
	}

	public LabelDisplayBox setLabels(ObservableList<TurboLabel> labels) {
		this.labels = labels;
		populateWithLabels();
		return this;
	}
	
	private ChangeListener<String> createLabelNameListener(Label labelText){
		WeakReference<Label> labelTextRef = new WeakReference<Label>(labelText);
		ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				Label labelText = labelTextRef.get();
				if(labelText != null){
					labelText.setText(newValue);
				}
			}
		};
		changeListeners.add(listener);
		return listener;
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
			label.nameProperty().addListener(new WeakChangeListener<String>(createLabelNameListener(labelText)));
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
